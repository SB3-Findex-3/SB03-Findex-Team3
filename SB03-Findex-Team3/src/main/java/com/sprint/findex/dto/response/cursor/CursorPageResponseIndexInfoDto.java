package com.sprint.findex.dto.response.cursor;

import com.sprint.findex.dto.response.IndexInfoDto;
import java.util.List;

public record CursorPageResponseIndexInfoDto(
    List<IndexInfoDto> content,
    String nextCursor,
    String nextIdAfter,
    int size,
    Long totalElements,
    boolean hasNext
) { }
