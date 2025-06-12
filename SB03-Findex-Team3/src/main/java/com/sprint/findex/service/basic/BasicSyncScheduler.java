package com.sprint.findex.service.basic;

import com.sprint.findex.dto.request.IndexDataSyncRequest;
import com.sprint.findex.entity.AutoSyncConfig;
import com.sprint.findex.entity.SyncJob;
import com.sprint.findex.repository.AutoSyncConfigRepository;
import com.sprint.findex.repository.SyncJobRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BasicSyncScheduler {

    private final AutoSyncConfigRepository autoSyncConfigRepository;
    private final SyncJobRepository syncJobRepository;
    private final BasicSyncJobService basicSyncJobService;

    @Scheduled(cron = "${sync.scheduler.cron}")
    public void autoSyncIndexData() {
        log.info("[SyncSchedule] 지수 데이터 배치 자동연동 시작");

        List<AutoSyncConfig> enabledConfigs = autoSyncConfigRepository.findAll().stream()
            .filter(AutoSyncConfig::isEnabled)
            .toList();

        for (AutoSyncConfig config : enabledConfigs) {
            Long indexId = config.getIndexInfo().getId();
            OffsetDateTime lastJobTime = syncJobRepository.findTopByIndexInfoIdOrderByJobTimeDesc(indexId)
                .map(SyncJob::getJobTime)
                .orElse(OffsetDateTime.now().minusDays(28));
            LocalDate lastSyncedDate = lastJobTime.toLocalDate();

            log.info("[SyncSchedule] [indexId={}] 마지막 연동 날짜: {}", indexId, lastSyncedDate);
            LocalDate today = LocalDate.now();
            if (lastSyncedDate.isBefore(today)) {
                IndexDataSyncRequest request = new IndexDataSyncRequest(
                    List.of(indexId), lastSyncedDate.plusDays(1), today
                );
                basicSyncJobService.fetchAndSaveIndexData(request, null).subscribe();
            }
        }

        log.info("[SyncSchedule] 배치 지수 데이터 자동연동 완료");
    }
}