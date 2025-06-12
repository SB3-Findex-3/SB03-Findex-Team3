package com.sprint.findex.mapper;

import com.sprint.findex.dto.response.IndexInfoSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class IndexInfoSearchMapper {
    public IndexInfoSearchDto toDto(
        String indexClassification,
        String indexName,
        Boolean favorite,
        String idAfter,
        String cursor,
        String sortField,
        String sortDirection,
        Integer size) {

        return new IndexInfoSearchDto(
            indexClassification,
            indexName,
            favorite,
            idAfter,
            cursor,
            sortField,
            sortDirection,
            size
        );
    }
}
