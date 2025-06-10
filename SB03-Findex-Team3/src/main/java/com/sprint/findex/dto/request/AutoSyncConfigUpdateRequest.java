package com.sprint.findex.dto.request;

import jakarta.validation.constraints.NotNull;

public record AutoSyncConfigUpdateRequest(
        @NotNull boolean enabled
        ) {
}