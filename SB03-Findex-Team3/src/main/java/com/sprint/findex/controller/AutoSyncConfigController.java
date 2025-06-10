package com.sprint.findex.controller;

import com.sprint.findex.dto.request.AutoSyncConfigQueryParams;
import com.sprint.findex.dto.request.AutoSyncConfigUpdateRequest;
import com.sprint.findex.dto.response.AutoSyncConfigDto;
import com.sprint.findex.dto.response.CursorPageResponseAutoSyncConfigDto;
import com.sprint.findex.service.AutoSyncConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auto-sync-configs")
public class AutoSyncConfigController {

    private final AutoSyncConfigService autoSyncConfigService;

    @PatchMapping("/{id}")
    public ResponseEntity<AutoSyncConfigDto> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody AutoSyncConfigUpdateRequest request
            ) {
        AutoSyncConfigDto result = autoSyncConfigService.updateOrCreate(id, request);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping
    public ResponseEntity<CursorPageResponseAutoSyncConfigDto> findByCursor(
            @ModelAttribute AutoSyncConfigQueryParams params
    ) {
        log.debug("[커서 조회] sortField={}, cursor={}, idAfter={}, direction={}",
                params.sortField(), params.cursor(), params.idAfter(), params.sortDirection());

        CursorPageResponseAutoSyncConfigDto resultList = autoSyncConfigService.findByCursor(params);
        log.debug("[커서 조회 완료] 결과 수: {}", resultList.content().size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultList);
    }

//    @GetMapping
//    public  ResponseEntity<List<AutoSyncConfigDto>> findAllByParams(
//            @ModelAttribute AutoSyncConfigSearchParams params
//    ) {
//        List<AutoSyncConfigDto> resultList = autoSyncConfigService.findAllByParams(params);
//
//        log.debug("[조건 조회 완료] 결과 수: {}", resultList.size());
//
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(resultList);
//    }
}

