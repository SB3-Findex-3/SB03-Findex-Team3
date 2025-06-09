package com.sprint.findex.dto.request;

import com.sprint.findex.entity.IndexInfo;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record IndexInfoCreateRequest(
        String indexClassification,
        String indexName,
        Integer employedItemsCount,
        LocalDate basePointInTime,
        BigDecimal baseIndex,
        boolean favorite

) {
    public static IndexInfoCreateRequest of(IndexInfo indexInfo) {
        return IndexInfoCreateRequest.builder()
            .indexClassification(indexInfo.getIndexClassification())
            .indexName(indexInfo.getIndexName())
            .employedItemsCount(indexInfo.getEmployedItemsCount())
            .basePointInTime(indexInfo.getBasePointInTime())
            .baseIndex(indexInfo.getBaseIndex())
            .favorite(indexInfo.isFavorite())
            .build();
    }
}
