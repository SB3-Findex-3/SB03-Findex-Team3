package com.sprint.findex.dto.request;

public record AutoSyncConfigQueryParams(
        Long indexInfoId,
        Boolean enabled,
        String idAfter,
        String cursor,
        String sortField, // indexInfo.indexName, enabled
        String sortDirection,
        Integer size
) {
}
