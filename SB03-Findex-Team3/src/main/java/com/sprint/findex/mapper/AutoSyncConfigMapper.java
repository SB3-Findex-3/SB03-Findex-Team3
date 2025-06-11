package com.sprint.findex.mapper;

import com.sprint.findex.dto.response.AutoSyncConfigDto;
import com.sprint.findex.entity.AutoSyncConfig;
import org.springframework.stereotype.Component;

@Component
public class AutoSyncConfigMapper {

    public AutoSyncConfigDto toDto(AutoSyncConfig entity) {
        return new AutoSyncConfigDto(
            entity.getId(),
            entity.getIndexInfo().getId(),
            entity.getIndexInfo().getIndexClassification(),
            entity.getIndexInfo().getIndexName(),
            entity.isEnabled()
        );
    }
}