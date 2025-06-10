package com.sprint.findex.mapper;

import com.sprint.findex.dto.response.AutoSyncConfigDto;
import com.sprint.findex.entity.AutoSyncConfig;
import org.springframework.stereotype.Component;

@Component
public class AutoSyncConfigMapper {

    public AutoSyncConfigDto toDto(AutoSyncConfig autoSyncConfig) {

        return new AutoSyncConfigDto(
                autoSyncConfig.getId(),
                autoSyncConfig.getIndexInfo().getId(),
                autoSyncConfig.getIndexClassification(),
                autoSyncConfig.getIndexName(),
                autoSyncConfig.isEnabled());
    }
}
