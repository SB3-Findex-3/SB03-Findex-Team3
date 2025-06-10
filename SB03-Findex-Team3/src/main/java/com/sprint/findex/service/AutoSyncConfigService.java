package com.sprint.findex.service;

import com.sprint.findex.dto.response.AutoSyncConfigDto;

public interface AutoSyncConfigService {

    AutoSyncConfigDto updateOrCreateConfig(Long id, boolean enabled);
}
