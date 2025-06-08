package com.sprint.findex.dto.response;

import com.sprint.findex.entity.SyncJob;
import com.sprint.findex.entity.SyncJobResult;
import com.sprint.findex.entity.SyncJobType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Builder;


@Builder
public record SyncJobDto(
    Long id,
    SyncJobType jobType,          // ex: INDEX_DATA
    Long indexInfoId,             // ex: 1
    LocalDate targetDate,         // ex: 2023-01-01
    String worker,                // ex: "192.168.0.1" or "system"
    OffsetDateTime jobTime,        // ex: 2023-01-01T12:00:00
    SyncJobResult result
) {

    private static SyncJobDto fromSyncJob(SyncJob syncJob, LocalDate targetDate) {
        return SyncJobDto.builder()
            .id(syncJob.getId())
            .jobType(syncJob.getJobType())
            .indexInfoId(syncJob.getIndexInfo().getId())
            .worker(syncJob.getWorker())
            .jobTime(syncJob.getJobTime())
            .result(syncJob.getJobResult())
            .targetDate(targetDate)
            .build();
    }

    public static SyncJobDto fromIndexInfo(SyncJob syncJob) {
        return fromSyncJob(syncJob, null);
    }

    public static SyncJobDto fromIndexData(SyncJob syncJob) {
        return fromSyncJob(syncJob, syncJob.getTargetDate());
    }
}