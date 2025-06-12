package com.sprint.findex.dto.response.cursor;

import java.util.List;

public record CursorPageResponseIndexData<T>(
    List<T> content,
    String nextCursor,
    String nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {}