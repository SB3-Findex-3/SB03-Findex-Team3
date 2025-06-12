package com.sprint.findex.dto.request;

public record AutoSyncQueryParams(
    Long indexInfoId,
    Boolean enabled,
    String idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size
) {}
