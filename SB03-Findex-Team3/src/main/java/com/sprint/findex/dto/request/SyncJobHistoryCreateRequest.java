package com.sprint.findex.dto.request;

import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.SyncJobType;
import java.time.LocalDate;

public record SyncJobHistoryCreateRequest(
    SyncJobType jobType,
    IndexInfo indexInfo,
    LocalDate targetDate,
    String worker
) {

}
