package com.sprint.findex.mapper;

import com.sprint.findex.dto.response.SyncJobHistoryDto;
import com.sprint.findex.entity.SyncJobHistory;
import java.time.LocalDate;

public class SyncJobHistoryMapper {

    public static SyncJobHistoryDto toDto(SyncJobHistory syncJobHistory) {
        LocalDate targetDate = syncJobHistory.getTargetDate();
        if(targetDate == null) {
            return new SyncJobHistoryDto(
                syncJobHistory.getId(),
                syncJobHistory.getJobType(),
                syncJobHistory.getIndexInfo().getId(),
                null,
                syncJobHistory.getWorker(),
                syncJobHistory.getJobTime(),
                syncJobHistory.getJobResult()
            );
        }else {
            return new SyncJobHistoryDto(
                syncJobHistory.getId(),
                syncJobHistory.getJobType(),
                syncJobHistory.getIndexInfo().getId(),
                syncJobHistory.getTargetDate(),
                syncJobHistory.getWorker(),
                syncJobHistory.getJobTime(),
                syncJobHistory.getJobResult()
            );
        }
    }

}
