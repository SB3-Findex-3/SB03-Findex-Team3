package com.sprint.findex.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ResponseSyncJobCursorDto(
    Long id,
    LocalDate targetDate,
    OffsetDateTime jobTime
) {}