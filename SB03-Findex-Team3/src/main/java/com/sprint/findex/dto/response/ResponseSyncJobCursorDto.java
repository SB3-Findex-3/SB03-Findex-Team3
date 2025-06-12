package com.sprint.findex.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ResponseSyncJobCursorDto(
    Long id,
    LocalDate targetDate,
    OffsetDateTime jobTime
) {}