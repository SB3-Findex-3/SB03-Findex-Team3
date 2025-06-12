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


}