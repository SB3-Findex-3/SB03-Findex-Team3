package com.sprint.findex.service.basic;

import com.sprint.findex.dto.response.AutoSyncConfigDto;
import com.sprint.findex.entity.AutoSyncConfig;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.mapper.AutoSyncConfigMapper;
import com.sprint.findex.repository.AutoSyncConfigRepository;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.service.AutoSyncConfigService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BasicAutoSyncConfigService implements AutoSyncConfigService {

    private final AutoSyncConfigRepository autoSyncConfigRepository;
    private final IndexInfoRepository indexInfoRepository;
    private final AutoSyncConfigMapper autoSyncConfigMapper;

    @Override
    public AutoSyncConfigDto updateOrCreateConfig(Long id, boolean enabled) {
        return autoSyncConfigRepository.findById(id)
                .map(config -> {
                    config.setEnabled(enabled); // 기존 설정 업데이트
                    AutoSyncConfig updatedConfig = autoSyncConfigRepository.save(config);

                    return autoSyncConfigMapper.toDto(updatedConfig);
                })
                .orElseGet(() -> {
                    IndexInfo indexInfo = indexInfoRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("IndexInfo with id " + id + " not found"));

                    AutoSyncConfig newAutoSyncConfig = AutoSyncConfig.ofIndexInfo(indexInfo);
                    newAutoSyncConfig.setEnabled(enabled);
                    autoSyncConfigRepository.save(newAutoSyncConfig);

                    AutoSyncConfigDto newAutoSyncConfigDto = autoSyncConfigMapper.toDto(newAutoSyncConfig);

                    return newAutoSyncConfigDto;
                });
    }
}

