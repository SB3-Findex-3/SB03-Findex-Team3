package com.sprint.findex.service;

import com.sprint.findex.dto.request.IndexDataSyncRequest;
import com.sprint.findex.dto.response.SyncJobDto;
import java.util.List;
import reactor.core.publisher.Mono;

public interface SyncJobService {
    Mono<List<SyncJobDto>> fetchAndSaveIndexData(IndexDataSyncRequest request, String workerIp);
}
