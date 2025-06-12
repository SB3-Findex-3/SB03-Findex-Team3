package com.sprint.findex.mapper;

import com.sprint.findex.dto.request.SyncJobQueryParams;
import com.sprint.findex.entity.SyncJobResult;
import com.sprint.findex.entity.SyncJobType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
public class SyncJobQueryParamsMapper {
    public SyncJobQueryParams toDto(
        SyncJobType jobType,
        Long indexInfoId,
        LocalDate baseDataFrom,
        LocalDate baseDataTo,
        String worker,
        OffsetDateTime jobTimeFrom,
        OffsetDateTime jobTimeTo,
        SyncJobResult status,
        String idAfter,
        String cursor,
        String sortField,
        String sortDirection,
        int size
    ){
        return new SyncJobQueryParams(
            jobType,
            indexInfoId,
            baseDataFrom,
            baseDataTo,
            worker,
            jobTimeFrom,
            jobTimeTo,
            status,
            idAfter,
            cursor,
            sortField,
            sortDirection,
            size
        );
    }
}
