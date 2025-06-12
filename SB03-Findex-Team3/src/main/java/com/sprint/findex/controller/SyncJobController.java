package com.sprint.findex.controller;

import com.sprint.findex.controller.api.SyncJobApi;
import com.sprint.findex.dto.request.IndexDataSyncRequest;
import com.sprint.findex.dto.request.SyncJobQueryParams;
import com.sprint.findex.dto.response.SyncJobDto;
import com.sprint.findex.dto.response.cursor.CursorPageResponseSyncJobDto;
import com.sprint.findex.entity.SyncJobResult;
import com.sprint.findex.entity.SyncJobType;
import com.sprint.findex.global.util.IpUtil;
import com.sprint.findex.mapper.SyncJobQueryParamsMapper;
import com.sprint.findex.service.SyncJobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController implements SyncJobApi {

    private final SyncJobService syncJobService;
    private final SyncJobQueryParamsMapper syncJobQueryParamsMapper;

    @PostMapping("index-infos")
    public ResponseEntity<List<SyncJobDto>> syncIndexInfoAsync(HttpServletRequest httpRequest) {
        String clientIp = IpUtil.getClientIp(httpRequest);
        Mono<List<SyncJobDto>> result = syncJobService.fetchAllIndexInfo(clientIp);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(result.block());
    }

    @PostMapping("index-data")
    public ResponseEntity<List<SyncJobDto>> syncIndexInfos(
        @Valid @RequestBody IndexDataSyncRequest request, HttpServletRequest httpRequest) {

        String workerIp = IpUtil.getClientIp(httpRequest);
        Mono<List<SyncJobDto>> result = syncJobService.fetchAndSaveIndexData(request, workerIp);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(result.block());
    }

    @GetMapping
    public ResponseEntity<Object> getSyncJobList(
        @RequestParam(required = false) SyncJobType jobType,
        @RequestParam(required = false) Long indexInfoId,
        @RequestParam(required = false) LocalDate baseDateFrom,
        @RequestParam(required = false) LocalDate baseDateTo,
        @RequestParam(required = false) String worker,
        @RequestParam(required = false) OffsetDateTime jobTimeFrom,
        @RequestParam(required = false) OffsetDateTime jobTimeTo,
        @RequestParam(required = false) SyncJobResult status,
        @RequestParam(required = false) String idAfter,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "jobTime", required = false) String sortField,
        @RequestParam(defaultValue = "asc", required = false) String sortDirection,
        @RequestParam(defaultValue = "10", required = false) int size) {

        SyncJobQueryParams syncParams = syncJobQueryParamsMapper.toDto(
            jobType, indexInfoId, baseDateFrom, baseDateTo, worker,
            jobTimeFrom, jobTimeTo, status, idAfter, cursor, sortField,
            sortDirection, size
        );

        CursorPageResponseSyncJobDto response = syncJobService.findSyncJobByCursor(syncParams);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }
}