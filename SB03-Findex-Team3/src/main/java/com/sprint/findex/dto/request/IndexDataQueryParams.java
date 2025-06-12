package com.sprint.findex.dto.request;

import java.time.LocalDate;

public record IndexDataQueryParams(
    Long indexInfoId,
    LocalDate startDate,
    LocalDate endDate,
    String sortField,
    String sortDirection,
    String cursor,
    String idAfter,
    Integer size
) {

    public IndexDataQueryParams withoutCursor() {
        return new IndexDataQueryParams(
            this.indexInfoId(),
            this.startDate(),
            this.endDate(),
            null,
            null,
            this.sortField(),
            this.sortDirection(),
            this.size()
        );
    }

}