package com.sprint.findex.dto;

public record IndexInfoSearchDto(
    String indexClassification,
    String indexName,
    Boolean favorite,
    Long idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size
) {

     // 커서나 idAfter가 있는지 확인 (첫 페이지가 아닌지)
    public boolean hasCursorInfo() {
        return idAfter != null || (cursor != null && !cursor.trim().isEmpty());
    }

     // 필터 조건이 있는지 확인
    public boolean hasFilters() {
        return indexClassification != null || indexName != null || favorite != null;
    }


     //검색 조건 요약 문자열 생성 (로깅용)

    public String toSummary() {
        return String.format(
            "SearchCondition{classification='%s', name='%s', favorite=%s, idAfter=%d, sort='%s %s', size=%d}",
            indexClassification, indexName, favorite, idAfter, sortField, sortDirection, size
        );
    }
}