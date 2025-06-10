package com.sprint.findex.controller;

import com.sprint.findex.dto.request.AutoSyncConfigUpdateRequest;
import com.sprint.findex.dto.response.AutoSyncConfigDto;
import com.sprint.findex.service.AutoSyncConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        boolean enabled = request.enabled();
        AutoSyncConfigDto result = autoSyncConfigService.updateOrCreateConfig(id, enabled);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}

