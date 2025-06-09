package com.sprint.findex.dto.response;

import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record IndexInfoDto(
    Long id,
    String indexClassification,
    String indexName,
    int employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    SourceType sourceType,
    Boolean favorite
) {
    public static IndexInfoDto toDto(IndexInfo indexInfo) {
        return IndexInfoDto.builder()
            .id(indexInfo.getId())
            .indexClassification(indexInfo.getIndexClassification())
            .indexName(indexInfo.getIndexName())
            .employedItemsCount(indexInfo.getEmployedItemsCount())
            .basePointInTime(indexInfo.getBasePointInTime())
            .baseIndex(indexInfo.getBaseIndex())
            .sourceType(indexInfo.getSourceType())
            .favorite(indexInfo.isFavorite())
            .build();
    }


}
