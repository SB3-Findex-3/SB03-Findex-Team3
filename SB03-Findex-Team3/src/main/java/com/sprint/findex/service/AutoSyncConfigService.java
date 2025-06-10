package com.sprint.findex.service;

import com.sprint.findex.dto.request.AutoSyncQueryParams;
import com.sprint.findex.dto.response.AutoSyncConfigDto;
import com.sprint.findex.dto.response.CursorPageResponseAutoSyncConfigDto;

public interface AutoSyncConfigService {

    AutoSyncConfigDto updateOrCreateConfig(Long id, boolean enabled);

    CursorPageResponseAutoSyncConfigDto findByCursor(AutoSyncQueryParams params);
}
