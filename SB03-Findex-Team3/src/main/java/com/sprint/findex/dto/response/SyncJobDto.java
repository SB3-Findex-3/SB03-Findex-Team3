package com.sprint.findex.dto.response;

import com.sprint.findex.entity.SyncJobResult;
import com.sprint.findex.entity.SyncJobType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record SyncJobDto(
    Long id,
    SyncJobType jobType,
    Long indexInfoId,
    LocalDate targetDate,
    String worker,
    OffsetDateTime jobTime,
    SyncJobResult result
) {

}