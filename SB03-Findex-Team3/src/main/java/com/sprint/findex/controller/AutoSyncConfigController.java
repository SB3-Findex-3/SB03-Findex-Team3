package com.sprint.findex.controller;

import com.sprint.findex.dto.request.AutoSyncConfigUpdateRequest;
import com.sprint.findex.dto.request.AutoSyncQueryParams;
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
@RequestMapping("/api/auto-sync-configs")
@RequiredArgsConstructor
public class AutoSyncConfigController {

    private final AutoSyncConfigService autoSyncConfigService;

    @PatchMapping("/{id}")
    public ResponseEntity<AutoSyncConfigDto> update(
        @PathVariable("id") Long id,
        @Valid @RequestBody AutoSyncConfigUpdateRequest request
    ) {
        boolean enabled = request.enabled();
        AutoSyncConfigDto result = autoSyncConfigService.updateOrCreateConfig(id, enabled);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping
    public ResponseEntity<CursorPageResponseAutoSyncConfigDto> findByCursor(@ModelAttribute AutoSyncQueryParams params) {
        log.debug("📌 [자동연동 커서 조회] sortField={}, cursor={}, idAfter={}, direction={}, enabled={}, indexInfoId={}",
            params.sortField(), params.cursor(), params.idAfter(), params.sortDirection(), params.enabled(), params.indexInfoId());

        CursorPageResponseAutoSyncConfigDto result = autoSyncConfigService.findByCursor(params);
        log.debug("✅ [자동연동 커서 조회 완료] 결과 수: {}", result.content().size());

        return ResponseEntity.ok(result);
    }
}
