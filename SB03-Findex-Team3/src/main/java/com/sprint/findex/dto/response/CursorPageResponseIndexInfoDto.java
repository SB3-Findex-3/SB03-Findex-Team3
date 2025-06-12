package com.sprint.findex.dto.response;

import java.util.List;

public record CursorPageResponseIndexInfoDto(
    List<IndexInfoDto> content,
    String nextCursor,
    String nextIdAfter,
    int size,
    Long totalElements,
    boolean hasNext
) { }
