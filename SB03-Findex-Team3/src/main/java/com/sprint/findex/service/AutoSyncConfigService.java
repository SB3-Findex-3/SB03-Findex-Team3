package com.sprint.findex.service;

import com.sprint.findex.dto.request.AutoSyncConfigUpdateRequest;
import com.sprint.findex.dto.request.AutoSyncQueryParams;
import com.sprint.findex.dto.response.AutoSyncConfigDto;
import com.sprint.findex.dto.response.CursorPageResponseAutoSyncConfigDto;

public interface AutoSyncConfigService {

    AutoSyncConfigDto updateOrCreate(Long id, AutoSyncConfigUpdateRequest request);

    CursorPageResponseAutoSyncConfigDto findByCursor(AutoSyncQueryParams params);
}
