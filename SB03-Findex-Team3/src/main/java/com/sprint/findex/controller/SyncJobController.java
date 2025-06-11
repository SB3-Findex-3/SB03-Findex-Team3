package com.sprint.findex.controller;

import com.sprint.findex.dto.request.IndexDataSyncRequest;
import com.sprint.findex.dto.response.SyncJobDto;
import com.sprint.findex.global.util.IpUtil;
import com.sprint.findex.service.SyncJobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController {
    private final SyncJobService syncJobService;

    @PostMapping("index-data")
    public ResponseEntity<List<SyncJobDto>> syncIndexInfo(
        @Valid @RequestBody IndexDataSyncRequest request, HttpServletRequest httpRequest) {

        String workerIp = IpUtil.getClientIp(httpRequest);
        Mono<List<SyncJobDto>> result = syncJobService.fetchAndSaveIndexData(request, workerIp);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(result.block());

    }

    @PostMapping("index-infos")
    public ResponseEntity<List<SyncJobDto>> syncIndexInfoAsync(HttpServletRequest httpRequest) {
        String clientIp = IpUtil.getClientIp(httpRequest);
        Mono<List<SyncJobDto>> result = syncJobService.fetchAllIndexInfo(clientIp);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result.block());

    }
}