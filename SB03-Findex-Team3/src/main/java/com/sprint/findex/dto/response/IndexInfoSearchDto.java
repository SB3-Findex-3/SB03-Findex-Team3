package com.sprint.findex.dto.response;

public record IndexInfoSearchDto(
    String indexClassification,
    String indexName,
    Boolean favorite,
    String idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size
) { }
