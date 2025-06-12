package com.sprint.findex.dto.response;

public record ResponseCursorDto(
    Long id,
    String indexClassification,
    String indexName,
    Integer employedItemsCount
) { }