package com.sprint.findex.dto.response.cursor;

import com.sprint.findex.dto.response.SyncJobDto;
import java.util.List;

public record CursorPageResponseSyncJobDto(
    List<SyncJobDto> content,
    String nextCursor,
    String nextIdAfter,
    int size,
    Long totalElements,
    boolean hasNext
) {

}
