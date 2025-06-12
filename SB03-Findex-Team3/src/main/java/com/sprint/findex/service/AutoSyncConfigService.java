package com.sprint.findex.service;

import com.sprint.findex.dto.request.AutoSyncConfigUpdateRequest;
import com.sprint.findex.dto.request.AutoSyncQueryParams;
import com.sprint.findex.dto.response.AutoSyncConfigDto;
import com.sprint.findex.dto.response.cursor.CursorPageResponseAutoSyncConfigDto;

public interface AutoSyncConfigService {

    AutoSyncConfigDto updateEnabled(Long indexInfoId, boolean enabled);

    CursorPageResponseAutoSyncConfigDto findByCursor(AutoSyncQueryParams params);
}
