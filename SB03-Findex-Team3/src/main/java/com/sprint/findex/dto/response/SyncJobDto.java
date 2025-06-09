package com.sprint.findex.dto.response;

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

}