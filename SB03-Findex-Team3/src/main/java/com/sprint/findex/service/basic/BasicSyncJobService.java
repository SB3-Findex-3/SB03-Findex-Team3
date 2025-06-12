package com.sprint.findex.service.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.dto.response.ResponseSyncJobCursorDto;
import com.sprint.findex.dto.request.IndexDataSyncRequest;
import com.sprint.findex.dto.request.SyncJobQueryParams;
import com.sprint.findex.dto.response.CursorPageResponseSyncJobDto;
import com.sprint.findex.dto.response.SyncJobDto;
import com.sprint.findex.entity.*;
import com.sprint.findex.global.dto.ApiResponse;
import com.sprint.findex.global.dto.MarketIndexResponse;
import com.sprint.findex.mapper.SyncJobMapper;
import com.sprint.findex.repository.AutoSyncConfigRepository;
import com.sprint.findex.repository.IndexDataRepository;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.repository.SyncJobRepository;
import com.sprint.findex.service.SyncJobService;
import com.sprint.findex.specification.SyncJobSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BasicSyncJobService implements SyncJobService {

    private final WebClient marketIndexWebClient;
    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;
    private final SyncJobRepository syncJobRepository;
    private final SyncJobMapper syncJobMapper;
    private final ObjectMapper objectMapper;
    private final AutoSyncConfigRepository autoSyncConfigRepository;

    @Value("${api.data.service-key}")
    private String serviceKey;

    @Value("${api.data.base-url}")
    private String baseUrl;

    private static final String SYSTEM_WORKER = "system";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public Mono<List<SyncJobDto>> fetchAndSaveIndexData(IndexDataSyncRequest request, String workerIp) {
        validateRequest(request);
        workerIp = (workerIp == null || workerIp.isBlank()) ? SYSTEM_WORKER : workerIp;

        List<Mono<List<SyncJobDto>>> jobMonos = new ArrayList<>();
        for (Long indexInfoId : request.indexInfoIds()) {
            final String finalWorkerIp = workerIp;
            jobMonos.add(
                fetchIndexInfo(indexInfoId)
                    .flatMap(indexInfo -> fetchMarketIndexData(request, indexInfo)
                        .flatMap(items -> processItems(items, indexInfo, request, finalWorkerIp))
                    )
                    .onErrorResume(e -> handleError(e, request, indexInfoId, finalWorkerIp).map(List::of))
            );
        }

        return Flux.merge(jobMonos)
            .flatMap(Flux::fromIterable)
            .collectList();
    }

    private void validateRequest(IndexDataSyncRequest request) {
        if (request.baseDateFrom() == null || request.baseDateTo() == null) {
            throw new IllegalArgumentException("Start date and end date must be provided.");
        }
        if (request.baseDateFrom().isAfter(request.baseDateTo())) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
    }

    private Mono<IndexInfo> fetchIndexInfo(Long indexInfoId) {
        return Mono.fromSupplier(() -> indexInfoRepository.findById(indexInfoId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid IndexInfo ID: " + indexInfoId)));
    }

    private Mono<List<MarketIndexResponse.MarketIndexData>> fetchMarketIndexData(IndexDataSyncRequest request, IndexInfo indexInfo) {
        String url = createMarketIndexUrl(request, indexInfo);
        log.debug("📡 API URI: {}", url);

        return marketIndexWebClient.get()
            .uri(URI.create(url))
            .retrieve()
            .bodyToMono(MarketIndexResponse.class)
            .map(response -> response.getResponse().getBody().getItems().getItem())
            .doOnError(e -> log.error("Failed to fetch market data for index: {}", indexInfo.getIndexName()));
    }

    private String createMarketIndexUrl(IndexDataSyncRequest request, IndexInfo indexInfo) {
        return String.format(
            "%s/getStockMarketIndex?serviceKey=%s&resultType=json&pageNo=1&numOfRows=1000&beginBasDt=%s&endBasDt=%s&idxNm=%s",
            baseUrl,
            serviceKey,
            request.baseDateFrom().format(DATE_FORMATTER),
            request.baseDateTo().format(DATE_FORMATTER),
            URLEncoder.encode(indexInfo.getIndexName(), StandardCharsets.UTF_8)
        );
    }

    private Mono<List<SyncJobDto>> processItems(
        List<MarketIndexResponse.MarketIndexData> items,
        IndexInfo indexInfo,
        IndexDataSyncRequest request,
        String workerIp
    ) {
        List<Mono<SyncJobDto>> jobMonos = new ArrayList<>();
        for (MarketIndexResponse.MarketIndexData item : items) {
            LocalDate baseDate = parseBaseDate(item.getBasDt());
            jobMonos.add(saveIfNotExists(indexInfo, baseDate, item, workerIp));
        }

        return Flux.merge(jobMonos).collectList();
    }


    private LocalDate parseBaseDate(String basDt) {
        return LocalDate.parse(basDt, DATE_FORMATTER);
    }

    private Mono<SyncJobDto> saveIfNotExists(
        IndexInfo indexInfo,
        LocalDate baseDate,
        MarketIndexResponse.MarketIndexData item,
        String workerIp
    ) {
        return Mono.fromSupplier(() -> {
            boolean exists = indexDataRepository.existsByIndexInfoAndBaseDate(indexInfo, baseDate);

            if (!exists) {
                IndexData data = new IndexData(
                    indexInfo, baseDate, SourceType.OPEN_API,
                    item.getMkp(), item.getClpr(), item.getHipr(), item.getLopr(),
                    item.getVs(), item.getFltRt(), item.getTrqu(), item.getTrPrc(), item.getLstgMrktTotAmt()
                );
                indexDataRepository.save(data);
            } else {
                log.info("⚠️ IndexData already exists for index={}, date={}", indexInfo.getIndexName(), baseDate);
            }

            SyncJob job = new SyncJob(SyncJobType.INDEX_DATA, indexInfo, baseDate, workerIp, OffsetDateTime.now(), SyncJobResult.SUCCESS);
            syncJobRepository.save(job);
            return toDto(job);
        });
    }

    private SyncJobDto toDto(SyncJob job) {
        return new SyncJobDto(
            job.getId(),
            job.getJobType(),
            job.getIndexInfo().getId(),
            job.getTargetDate(),
            job.getWorker(),
            job.getJobTime(),
            job.getResult()
        );
    }

    private Mono<SyncJobDto> handleError(Throwable e, IndexDataSyncRequest request, Long indexInfoId, String workerIp) {
        log.error("동기화 실패: indexInfoId={}, 기간={}~{}", indexInfoId, request.baseDateFrom(), request.baseDateTo(), e);

        // 실제 IndexInfo 조회 시도
        IndexInfo indexInfo = null;
        try {
            indexInfo = indexInfoRepository.findById(indexInfoId).orElse(null);
        } catch (Exception ex) {
            log.error("Failed to fetch IndexInfo for error handling", ex);
        }

        // 조회 실패 시 임시 객체 생성
        if (indexInfo == null) {
            indexInfo = new IndexInfo(
                "Unknown",
                "Unknown",
                0,
                LocalDate.now(),
                BigDecimal.valueOf(0),
                SourceType.OPEN_API,
                false
            );
            // ID 설정이 필요하면 여기서 설정
        }
        SyncJob job = new SyncJob(SyncJobType.INDEX_DATA, indexInfo, request.baseDateFrom(), workerIp, OffsetDateTime.now(), SyncJobResult.FAILED);
        syncJobRepository.save(job);

        return Mono.just(toDto(job));
    }

    @Override
    public Mono<List<SyncJobDto>> fetchAllIndexInfo(String workerIp){

        String settingWorkerIp = (workerIp == null || workerIp.isBlank()) ? SYSTEM_WORKER : workerIp;

        return fetchAllIndexInfosFromApi()
            .map(items -> processIndexInfoSync(items, settingWorkerIp))
            .doOnSuccess(syncJobs ->
                log.info("지수 정보 연동 성공"))
            .doOnError(error ->
                log.error("지수 정보 연동 실패", error))

            // (.onErrorResume 이게 뭔지 확인해보기)
            .onErrorResume(error ->
                handleIndexInfoSyncError(error, settingWorkerIp));
    }

    private Mono<List<ApiResponse.StockIndexItem>> fetchAllIndexInfosFromApi() {

        String url = String.format("%s/getStockMarketIndex?serviceKey=%s&resultType=json&pageNo=%d&numOfRows=%d",
            baseUrl, serviceKey, 1, 200);

        return callApi(url)
            .flatMap(firstResponse -> {
                List<ApiResponse.StockIndexItem> allItems = new ArrayList<>();

                if (firstResponse.getBody() != null && firstResponse.getBody().getItems() != null) {
                    allItems.addAll(firstResponse.getBody().getItems().getItem());
                }

                return Mono.just(allItems);
            });
    }

    private Mono<ApiResponse> callApi(String url) {
        try{
            URI uri = new URI(url);
            log.info("API 호출: {}", url.replaceAll("serviceKey=[^&]*", "serviceKey=****"));

            return marketIndexWebClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .retry(2)
                .doOnNext(response -> System.out.println("API 호출 성공!"))
                .doOnError(error -> System.out.println("API 호출 실패: " + error.getMessage()));

        }catch (URISyntaxException e) {
            log.error("URI 변환 실패: {}", url, e);
            return Mono.error(new RuntimeException("URI 변환 실패: " + e.getMessage()));
        }
    }

    private List<SyncJobDto> processIndexInfoSync(List<ApiResponse.StockIndexItem> items, String workerIp){

        Set<String> processedKeys = new HashSet<>();
        List<ApiResponse.StockIndexItem> uniqueItems = new ArrayList<>();

        for (ApiResponse.StockIndexItem item : items) {
            String key = item.getIndexClassification() + "|" + item.getIndexName();
            if (processedKeys.add(key)) {
                uniqueItems.add(item);
            }
        }

        List<IndexInfo> allIndexInfo = indexInfoRepository.findAll();
        List<SyncJobDto> syncJobs = new ArrayList<>();

        for (ApiResponse.StockIndexItem item: uniqueItems){
            try {
                IndexInfo existing = findExisting(item);
                IndexInfo indexInfo;

                if (existing != null) {
                    updateExisting(existing, item);

                    if (hasChanged(existing, item)) {
                        indexInfo = updateExisting(existing, item);
                        indexInfoRepository.save(indexInfo);
                    }
                    else {
                        indexInfo = existing;
                    }

                } else {
                    indexInfo = createNewIndexInfo(item);
                    allIndexInfo.add(indexInfo);
                    indexInfoRepository.save(indexInfo);
                }

                SyncJob newSyncJob = createSyncJob(indexInfo, workerIp);
                SyncJob savedSyncJob = syncJobRepository.save(newSyncJob);
                SyncJobDto syncJobDto = syncJobMapper.toDto(savedSyncJob);
                syncJobs.add(syncJobDto);

            }catch (Exception e){
                log.error("불러온 지수 정보 처리 실패", e);

                SyncJobDto failed = createFailedSyncJobDto(item, workerIp);
                syncJobs.add(failed);
            }
        }

        return syncJobs;
    }

    private IndexInfo findExisting(ApiResponse.StockIndexItem item) {
        return indexInfoRepository
            .findByIndexClassificationAndIndexName(item.getIndexClassification(), item.getIndexName())
            .orElse(null);
    }

    //info 저장 시 autosync도 저장
    private IndexInfo createNewIndexInfo(ApiResponse.StockIndexItem item) {
        IndexInfo newIndexInfo = new IndexInfo(
                item.getIndexClassification(),
                item.getIndexName(),
                parseInteger(item.getEmployedItemsCount()),
                parseDate(item.getBasePointTime()),
                parseBigDecimal(item.getBaseIndex()),
                SourceType.OPEN_API,
                false
        );

        indexInfoRepository.save(newIndexInfo);

        AutoSyncConfig config = AutoSyncConfig.ofIndexInfo(newIndexInfo);
        config.setEnabled(false);
        autoSyncConfigRepository.save(config);

        return newIndexInfo;
    }

    private SyncJob createSyncJob(IndexInfo indexInfo, String workerIp){

        return new SyncJob(
            SyncJobType.INDEX_INFO,
            indexInfo,
            null,
            workerIp,
            OffsetDateTime.now(),
            SyncJobResult.SUCCESS
        );
    }

    private SyncJobDto createFailedSyncJobDto(ApiResponse.StockIndexItem item, String workerIp) {

        return new SyncJobDto(
            null,
            SyncJobType.INDEX_INFO,
            null,
            null,
            workerIp,
            OffsetDateTime.now(),
            SyncJobResult.FAILED
        );
    }

    private Mono<List<SyncJobDto>> handleIndexInfoSyncError(Throwable error, String workerIp) {
        log.error("지수 정보 동기화 실패", error);

        SyncJob failedJob = new SyncJob(
            SyncJobType.INDEX_INFO,
            null,
            null,
            workerIp,
            OffsetDateTime.now(),
            SyncJobResult.FAILED
        );
        syncJobRepository.save(failedJob);

        SyncJobDto failedJobDto = new SyncJobDto(
            failedJob.getId(),
            failedJob.getJobType(),
            null,
            failedJob.getTargetDate(),
            failedJob.getWorker(),
            failedJob.getJobTime(),
            failedJob.getResult()
        );

        return Mono.just(List.of(failedJobDto));
    }

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            log.warn("기준시점이 비어있습니다. 현재 날짜를 사용합니다.");
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}, 현재 날짜 사용", dateString);
            return LocalDate.now();
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty() || "-".equals(value.trim())) {
            return null;
        }
        try {
            // 콤마, 공백 제거 후 변환
            return new BigDecimal(value.replaceAll(",", "").trim());
        } catch (NumberFormatException e) {
            log.warn("BigDecimal 파싱 실패: {}, null 반환", value);
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty() || "-".equals(value.trim())) {
            return 0;
        }
        try {
            return Integer.parseInt(value.replaceAll(",", "").trim());
        } catch (NumberFormatException e) {
            log.warn("정수 파싱 실패: {}, 기본값 0 사용", value);
            return 0;
        }
    }

    private boolean hasChanged(IndexInfo existing, ApiResponse.StockIndexItem item){
        return !existing.getIndexClassification().equals(item.getIndexClassification()) ||
            !existing.getIndexName().equals(item.getIndexName()) ||
            !Objects.equals(existing.getEmployedItemsCount(), parseInteger(item.getEmployedItemsCount())) ||
            !Objects.equals(existing.getBaseIndex(), parseBigDecimal(item.getBaseIndex())) ||
            !existing.getBasePointInTime().equals(parseDate(item.getBasePointTime()));
    }

    private IndexInfo updateExisting(IndexInfo existing, ApiResponse.StockIndexItem item) {
        existing.updateIndexClassification(item.getIndexClassification());
        existing.updateIndexName(item.getIndexName());
        existing.updateEmployedItemsCount(parseInteger(item.getEmployedItemsCount()));
        existing.updateBaseIndex(parseBigDecimal(item.getBaseIndex()));
        existing.updateBasePointInTime(parseDate(item.getBasePointTime()));
        return existing;
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseSyncJobDto findSyncJobByCursor(SyncJobQueryParams params){

        ResponseSyncJobCursorDto responseSyncJobCursorDto = null;
        if (params.cursor() != null){
            responseSyncJobCursorDto = parseCurser(params.cursor());
            log.info("IndexInfoService: 지수 목록 조회를 위해 커서 디코딩 완료, 디코딩 된 커서: {}", responseSyncJobCursorDto);
        }

        Specification<SyncJob> spec = SyncJobSpecifications.withFilters(responseSyncJobCursorDto, params);
        Specification<SyncJob> countSpec = SyncJobSpecifications.withFilters(null, params);

        Sort sort = createSort(params.sortField(), params.sortDirection());
        Pageable pageable = PageRequest.of(0, params.size(), sort);

        Slice<SyncJob> slice = syncJobRepository.findAll(spec, pageable);

        Long totalElements = syncJobRepository.count(countSpec);

        return convertToResponse(slice, params, totalElements);
    }

    private Sort createSort(String sortField, String sortDirection){
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)?
            Sort.Direction.DESC : Sort.Direction.ASC;

        return Sort.by(direction, sortField).and(Sort.by("id").ascending());
    }

    private CursorPageResponseSyncJobDto convertToResponse(Slice<SyncJob> slice, SyncJobQueryParams params, Long totalElements){

        List<SyncJobDto> content = slice.getContent().stream()
            .map(syncJobMapper::toDto)
            .toList();

        String nextCursor = null;
        String nextIdAfter = null;

        if (slice.hasNext() && !content.isEmpty()){
            SyncJobDto lastSyncJob = content.get(content.size() - 1);
            List<String> syncJobCursor = generateNextCursor(lastSyncJob, params.sortField());
            nextCursor = syncJobCursor.get(0);
            nextIdAfter = syncJobCursor.get(1);
        }

        return new CursorPageResponseSyncJobDto(
            content,
            nextCursor,
            nextIdAfter,
            params.size(),
            totalElements,
            slice.hasNext()
        );
    }

    private List<String> generateNextCursor(SyncJobDto syncJobDto, String sortField){

        List<String> encodedSyncJobCursors = new ArrayList<>();

        try {
            ResponseSyncJobCursorDto cursorDto = switch (sortField) {
                case "targetDate" ->
                    new ResponseSyncJobCursorDto(
                        syncJobDto.id(),
                        syncJobDto.targetDate(),
                        null
                    );

                case "jobTime" ->
                    new ResponseSyncJobCursorDto(
                        syncJobDto.id(),
                        null,
                        syncJobDto.jobTime()
                    );

                default -> {
                    log.warn("알 수 없는 정렬 필드: {}", sortField);
                    yield new ResponseSyncJobCursorDto(
                        syncJobDto.id(),
                        null,
                        syncJobDto.jobTime() != null ? syncJobDto.jobTime() : null
                    );
                }
            };

            String jsonCursor  = objectMapper.writeValueAsString(cursorDto);
            String jsonIdAfter  = objectMapper.writeValueAsString(cursorDto.id());

            String encodedCursor = Base64.getEncoder().encodeToString(jsonCursor.getBytes());
            String encodedCursorIdAfter = Base64.getEncoder().encodeToString(jsonIdAfter.getBytes());

            encodedSyncJobCursors.add(encodedCursor);
            encodedSyncJobCursors.add(encodedCursorIdAfter);

            return encodedSyncJobCursors;

        } catch (Exception e) {
            log.error("커서 생성 실패: sortField={}, syncJobDto={}", sortField, syncJobDto, e);
            throw new RuntimeException("커서 생성에 실패했습니다.", e);
        }
    }

    private ResponseSyncJobCursorDto parseCurser(String cursor){
        try{
            byte[] decodedBytes = Base64.getDecoder().decode(cursor);
            String dateString = new String(decodedBytes);

            return objectMapper.readValue(dateString, ResponseSyncJobCursorDto.class);
        }
        catch (Exception e){
            log.error("SyncJobService: 입력커서: {} 디코딩 실패 ", cursor);
            throw new IllegalArgumentException(e);
        }
    }
}
