package com.sprint.findex.service.basic;

import com.sprint.findex.dto.request.IndexDataSyncRequest;
import com.sprint.findex.dto.response.SyncJobDto;
import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.SourceType;
import com.sprint.findex.entity.SyncJob;
import com.sprint.findex.entity.SyncJobResult;
import com.sprint.findex.entity.SyncJobType;
import com.sprint.findex.global.dto.MarketIndexResponse;
import com.sprint.findex.repository.IndexDataRepository;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.repository.SyncJobRepository;
import com.sprint.findex.service.SyncJobService;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
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

        List<Mono<SyncJobDto>> jobMonos = new ArrayList<>();
        for (Long indexInfoId : request.indexInfoIds()) {
            final String finalWorkerIp = workerIp;
            jobMonos.add(
                fetchIndexInfo(indexInfoId)
                    .flatMap(indexInfo -> fetchMarketIndexData(request, indexInfo)
                        .flatMap(items -> processItems(items, indexInfo, request, finalWorkerIp))
                    )
                    .onErrorResume(e -> handleError(e, request, indexInfoId, finalWorkerIp))
            );
        }

        return Mono.zip(jobMonos, results -> {
            List<SyncJobDto> jobs = new ArrayList<>();
            for (Object result : results) {
                jobs.add((SyncJobDto) result);
            }
            return jobs;
        });
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

    private Mono<SyncJobDto> processItems(List<MarketIndexResponse.MarketIndexData> items, IndexInfo indexInfo, IndexDataSyncRequest request, String workerIp) {
        List<Mono<Void>> saveMonos = new ArrayList<>();
        for (MarketIndexResponse.MarketIndexData item : items) {
            LocalDate baseDate = parseBaseDate(item.getBasDt());
            saveMonos.add(saveIfNotExists(indexInfo, baseDate, item));
        }

        return Mono.when(saveMonos)
            .then(Mono.fromSupplier(() ->
                createSyncJobDto(indexInfo, request.baseDateFrom(), workerIp, SyncJobResult.SUCCESS)
            ));
    }


    private LocalDate parseBaseDate(String basDt) {
        return LocalDate.parse(basDt, DATE_FORMATTER);
    }

    private Mono<Void> saveIfNotExists(IndexInfo indexInfo, LocalDate baseDate, MarketIndexResponse.MarketIndexData item) {
        return Mono.defer(() -> {
            if (indexDataRepository.existsByIndexInfoAndBaseDate(indexInfo, baseDate)) {
                log.info("⚠️ IndexData already exists for index={}, date={}", indexInfo.getIndexName(), baseDate);
                return Mono.empty();
            }

            IndexData data = new IndexData(
                indexInfo, baseDate, SourceType.OPEN_API,
                item.getMkp(), item.getClpr(), item.getHipr(), item.getLopr(),
                item.getVs(), item.getFltRt(), item.getTrqu(), item.getTrPrc(), item.getLstgMrktTotAmt()
            );
            indexDataRepository.save(data);
            return Mono.empty();
        });
    }

    private SyncJobDto createSyncJobDto(IndexInfo indexInfo, LocalDate date, String workerIp, SyncJobResult result) {
        SyncJob job = new SyncJob(SyncJobType.INDEX_DATA, indexInfo, date, workerIp, OffsetDateTime.now(), result);
        syncJobRepository.save(job);
        return toDto(job);
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

        return Mono.just(createSyncJobDto(indexInfo, request.baseDateFrom(), workerIp, SyncJobResult.FAILED));
    }
}
