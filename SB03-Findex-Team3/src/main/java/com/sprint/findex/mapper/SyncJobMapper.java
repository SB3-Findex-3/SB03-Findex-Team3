package com.sprint.findex.mapper;

import com.sprint.findex.dto.response.SyncJobDto;
import com.sprint.findex.entity.SyncJob;
import org.springframework.stereotype.Component;

@Component
public class SyncJobMapper {

    public SyncJobDto toDto(SyncJob syncJob){
        return new SyncJobDto(
            syncJob.getId(),
            syncJob.getJobType(),
            syncJob.getIndexInfo().getId(),
            syncJob.getTargetDate(),
            syncJob.getWorker(),
            syncJob.getJobTime(),
            syncJob.getResult()
        );
    }
}
