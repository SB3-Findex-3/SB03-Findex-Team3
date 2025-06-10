package com.sprint.findex.service;

import com.sprint.findex.dto.request.AutoSyncQueryParams;
import com.sprint.findex.dto.response.CursorPageResponseAutoSyncConfigDto;

public interface AutoSyncService {
    CursorPageResponseAutoSyncConfigDto findByCursor(AutoSyncQueryParams params);
}
