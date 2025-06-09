package com.sprint.findex.service;

import com.sprint.findex.dto.request.SyncJobHistoryCreateRequest;
import com.sprint.findex.entity.SyncJobHistory;
import com.sprint.findex.entity.SyncJobResult;

public interface SyncJobHistoryService {

    SyncJobHistory saveHistory(SyncJobHistoryCreateRequest syncJobHistoryCreateRequest);

    void updateResult(Long id, SyncJobResult result);

}
