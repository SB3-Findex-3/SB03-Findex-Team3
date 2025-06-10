package com.sprint.findex.dto.response;

import java.util.List;

public record CursorPageResponseAutoSyncConfigDto(
    List<AutoSyncConfigDto> content,
    String nextCursor,
    String nextIdAfter,
    Integer size,
    Long totalElements,
    Boolean hasNext
) {
}
