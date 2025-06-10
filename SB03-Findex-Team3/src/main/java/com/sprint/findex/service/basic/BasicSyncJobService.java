package com.sprint.findex.service.basic;

import com.sprint.findex.dto.request.IndexDataSyncRequest;
import com.sprint.findex.dto.response.SyncJobDto;
import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.SourceType;
import com.sprint.findex.entity.SyncJob;
import com.sprint.findex.entity.SyncJobResult;
import com.sprint.findex.entity.SyncJobType;
import com.sprint.findex.global.dto.ApiResponse;
import com.sprint.findex.global.dto.MarketIndexResponse;
import com.sprint.findex.repository.IndexDataRepository;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.repository.SyncJobRepository;
import com.sprint.findex.service.SyncJobService;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BasicSyncJobService implements SyncJobService {

    private final WebClient marketIndexWebClient;
    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;
    private final SyncJobRepository syncJobRepository;

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

    // 1. 컨트롤러에 List<SyncJobDto>를 반환해주는 메서드

    @Override
    public Mono<List<SyncJobDto>> fetchAllIndexInfo(String workerIp){

        String settingWorkerIp = (workerIp == null || workerIp.isBlank()) ? SYSTEM_WORKER : workerIp;

        return fetchAllIndexInfosFromApi()
            .map(items -> processIndexInfoSync(items, settingWorkerIp))
            .doOnSuccess(syncJobs ->
                log.info("지수 정보 연동 성공"))
            .doOnError(error ->
                log.error("지수 정보 연동 실패", error))

            .onErrorResume(error ->
                handleIndexInfoSyncError(error, settingWorkerIp));
    }

    // 2.  데이터를 불러오고 응답 데이터의 지수 정보 부분을 리스트로 반환
    private Mono<List<ApiResponse.StockIndexItem>> fetchAllIndexInfosFromApi() {

        // 원래는 buildUrl 메서드로 요청 Url 생성하는 부분을 따로 뺐는데
        // 굳이? 싶어서 로직에 직접 넣음 -> 다시 빼도 됨
        String url = String.format("%s/getStockMarketIndex?serviceKey=%s&resultType=json&pageNo=%d&numOfRows=%d",
            baseUrl, serviceKey, 1, 200);

        // API 요청 결과 (json 응답)을 받아서 지수 정보만 추출한 뒤 List에 저장하는 로직
        return callApi(url)
            .flatMap(firstResponse -> {
                // StockIndexItem: 내가 받아올 지수 정보들만 필드로 선언해둔 클래스
                List<ApiResponse.StockIndexItem> allItems = new ArrayList<>();

                if (firstResponse.getBody() != null && firstResponse.getBody().getItems() != null) {
                    allItems.addAll(firstResponse.getBody().getItems().getItem());
                }

                return Mono.just(allItems);
            });
    }

    // 3. 메인 로직 (API 요청 로직)
    private Mono<ApiResponse> callApi(String url) {
        try{
            URI uri = new URI(url);
            log.info("API 호출: {}", url.replaceAll("serviceKey=[^&]*", "serviceKey=****"));

            return marketIndexWebClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)  // JSON 요청
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .retry(2)
                .doOnNext(response -> System.out.println("API 호출 성공!")) // JSON → ApiResponse 객체로 변환
                .doOnError(error -> System.out.println("API 호출 실패: " + error.getMessage()));

        }catch (URISyntaxException e) {
            log.error("URI 변환 실패: {}", url, e);
            return Mono.error(new RuntimeException("URI 변환 실패: " + e.getMessage()));
        }
    }

    // 4. 응답 받은 데이터를 활용해서 SyncJobDto 리스트 반환
    // List<ApiResponse.StockIndexItem> -> 지수 정보 데이터의 List
    // workerIp -> 작업자 ip
    private List<SyncJobDto> processIndexInfoSync(List<ApiResponse.StockIndexItem> items, String workerIp){

        // 중복 제거를 위한 set
        Set<String> processedKeys = new HashSet<>();
        List<ApiResponse.StockIndexItem> uniqueItems = new ArrayList<>();

        // <문제>
        // 데이터를 200개 요청 -> 하지만 응답 된 데이터는 200개보다 작음
        // 예상 결과: 응답된 데이터만 처리한다
        // 실제 결과: 응답된 데이터를 처리하고 (200 - 응답 데이터) 개수 만큼을 다시 처리함 (응답데이터의 1번부터 다시)
        // 처리 방법: 들어온 응답데이터의 IndexInfo 유니크 정보 (지수분류명, 지수명)을 보고
        // 이미 존재하는 데이터면 모든 값이 일치하는지 확인 -> 다르면 업데이트
        //  -> 같으면 아무것도 하지 말고 그냥 continue로 넘어가기
        // 이렇게 처리하자 처음 요청 때는 응답이 잘 반영되고
        // SyncJobDto 리스트 반환도 잘 됐지만
        // 그 후 연동은 다 SyncJobDto가 생성되지 않음

        // <해결>
        // 처음부터 중복을 없애버리도록 함
        // Set을 사용해서 유니크 키가 같은 데이터가 들어오는지 검증
        // 검증된 정보만 uniqueItems 리스트에 넣음
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

                // 동일한 데이터가 이미 존재할 때
                if (existing != null) {
                    indexInfo = updateExisting(existing, item);

                    // 기존 데이터와 비교해서 변경사항이 있는지 확인
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
                    IndexInfo savedInfo = indexInfoRepository.save(indexInfo);
                }

                SyncJob newSyncJob = createSyncJob(indexInfo, workerIp);
                SyncJob savedSyncJob = syncJobRepository.save(newSyncJob);
                SyncJobDto syncJobDto = createSyncJobDto(savedSyncJob);
                syncJobs.add(syncJobDto);

            }catch (Exception e){
                log.error("불러온 지수 정보 처리 실패", e);

                SyncJobDto failed = createFailedSyncJobDto(item, workerIp);
                syncJobs.add(failed);
            }
        }

        return syncJobs;
    }

    // 5. 값 일치, 객체 생성 등의 로직

    private IndexInfo findExisting(ApiResponse.StockIndexItem item) {
        return indexInfoRepository
            .findByIndexClassificationAndIndexName(item.getIndexClassification(), item.getIndexName())
            .orElse(null);
    }

    private IndexInfo createNewIndexInfo(ApiResponse.StockIndexItem item) {

        return new IndexInfo(
            item.getIndexClassification(),
            item.getIndexName(),
            parseInteger(item.getEmployedItemsCount()),
            parseDate(item.getBasePointTime()),
            parseBigDecimal(item.getBaseIndex()),
            SourceType.OPEN_API,
            false
        );
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

    // 연동 성공 SyncJob DTO
    private SyncJobDto createSyncJobDto(SyncJob syncJob) {

        return new SyncJobDto(
            syncJob.getId(),
            syncJob.getJobType(),
            syncJob.getIndexInfo().getId(),
            syncJob.getTargetDate(),
            syncJob.getWorker(),
            syncJob.getJobTime(),
            syncJob.getResult()
        );
    }

    // 연동 실패 DTO
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

    private IndexInfo updateExisting(IndexInfo existing, ApiResponse.StockIndexItem item) {
        existing.updateIndexClassification(item.getIndexClassification());
        existing.updateIndexName(item.getIndexName());
        existing.updateEmployedItemsCount(parseInteger(item.getEmployedItemsCount()));
        existing.updateBaseIndex(parseBigDecimal(item.getBaseIndex()));
        existing.updateBasePointInTime(parseDate(item.getBasePointTime()));
        return existing;
    }

    // 6. 정보 동기화 실패 시 반환해주는 에러 메소드

    private Mono<List<SyncJobDto>> handleIndexInfoSyncError(Throwable error, String workerIp) {
        log.error("지수 정보 동기화 실패", error);

        // 실패 SyncJob 생성
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

    // 7. 파싱 메서드

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
        log.error("❌ Sync failed: indexInfoId={}, range={}~{}", indexInfoId, request.baseDateFrom(), request.baseDateTo(), e);

        IndexInfo indexInfo = new IndexInfo("indexName", "description", 0, LocalDate.now(), BigDecimal.valueOf(0), SourceType.OPEN_API, true);

        SyncJob job = new SyncJob(SyncJobType.INDEX_DATA, indexInfo, request.baseDateFrom(), workerIp, OffsetDateTime.now(), SyncJobResult.FAILED);
        syncJobRepository.save(job);

        return Mono.just(toDto(job));
    }
}
