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
    public static SyncJobHistoryCreateRequest fromIndexInfo(IndexInfo indexInfo, String worker) {
        return new SyncJobHistoryCreateRequest(SyncJobType.INDEX_INFO, indexInfo, null, worker);
    }

    public static SyncJobHistoryCreateRequest fromIndexData(IndexInfo indexInfo, String worker, LocalDate targetDate) {
        if(targetDate == null) {
            throw new IllegalArgumentException("INDEX_DATA 작업에서는 targetDate가 필요합니다.");
        }

        return new SyncJobHistoryCreateRequest(SyncJobType.INDEX_DATA, indexInfo, targetDate, worker);
    }
}
