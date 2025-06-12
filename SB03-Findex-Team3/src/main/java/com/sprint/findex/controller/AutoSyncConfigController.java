package com.sprint.findex.controller;

import com.sprint.findex.dto.request.AutoSyncConfigUpdateRequest;
import com.sprint.findex.dto.request.AutoSyncQueryParams;
import com.sprint.findex.dto.response.AutoSyncConfigDto;
import com.sprint.findex.dto.response.cursor.CursorPageResponseAutoSyncConfigDto;
import com.sprint.findex.service.AutoSyncConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auto-sync-configs")
@RequiredArgsConstructor
public class AutoSyncConfigController {

    private final AutoSyncConfigService autoSyncConfigService;

    @GetMapping
    public CursorPageResponseAutoSyncConfigDto findByCursor(@ModelAttribute AutoSyncQueryParams params) {
        log.debug("[AutoSyncConfigController] 자동연동 커서 조회: {}", params);
        CursorPageResponseAutoSyncConfigDto result = autoSyncConfigService.findByCursor(params);
        log.debug("[AutoSyncConfigController] 자동연동 커서 조회 완료, 결과 수: {}", result.content().size());
        return result;
    }

    @PatchMapping("/{id}")
    public AutoSyncConfigDto update(
        @PathVariable Long id,
        @Valid @RequestBody AutoSyncConfigUpdateRequest request
    ) {
        return autoSyncConfigService.updateOrCreate(id, request);
    }
}
