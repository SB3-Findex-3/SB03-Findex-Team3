package com.sprint.findex.dto.response;

public record AutoSyncConfigDto(
    Long id,
    Long indexInfoId,
    String indexClassification,
    String indexName,
    boolean enabled
) {
}