package com.sprint.findex.dto.request;


public record AutoSyncConfigSearchParams(
        Long indexInfoId,
        Boolean enabled,
        Long lastId, // 커서 기반 페이징: 이전 페이지의 마지막 요소 ID
        String sortField, // 정렬 필드명: indexName 또는 enabled
        String sortDirection, // asc 또는 desc
        Integer size // 한 페이지에 가져올 개수
) {}

