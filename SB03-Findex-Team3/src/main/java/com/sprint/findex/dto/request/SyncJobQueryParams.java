package com.sprint.findex.dto.request;

import com.sprint.findex.entity.SyncJobResult;
import com.sprint.findex.entity.SyncJobType;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record SyncJobQueryParams(
    SyncJobType jobType,
    Long indexInfoId,
    LocalDate baseDateFrom,
    LocalDate baseDateTo,
    String worker,
    OffsetDateTime jobTimeFrom,
    OffsetDateTime jobTimeTo,
    SyncJobResult status,
    String idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    int size
) { }
