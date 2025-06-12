package com.sprint.findex.dto.response.cursor;

import com.sprint.findex.dto.response.AutoSyncConfigDto;
import java.util.List;

public record CursorPageResponseAutoSyncConfigDto(
    List<AutoSyncConfigDto> content,
    String nextCursor,
    String nextIdAfter,
    int size,
    Long totalElements,
    boolean hasNext
) {
}
