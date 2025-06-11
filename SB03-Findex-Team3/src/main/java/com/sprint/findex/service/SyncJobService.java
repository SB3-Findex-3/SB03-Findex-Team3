package com.sprint.findex.service;

import com.sprint.findex.dto.request.IndexDataSyncRequest;
import com.sprint.findex.dto.response.SyncJobDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SyncJobService {
    Mono<List<SyncJobDto>> fetchAndSaveIndexData(IndexDataSyncRequest request, String workerIp);

    Mono<List<SyncJobDto>> fetchAllIndexInfo(String workerIp);

}
