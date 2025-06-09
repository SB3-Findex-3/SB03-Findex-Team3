package com.sprint.findex.dto.request;

import com.sprint.findex.entity.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record IndexInfoCreateCommand(
    String indexClassification,
    String indexName,
    Integer employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    boolean favorite,
    SourceType sourceType
) {
    public static IndexInfoCreateCommand fromUser(IndexInfoCreateRequest request) {
        return IndexInfoCreateCommand.builder()
            .indexClassification(request.indexClassification())
            .indexName(request.indexName())
            .employedItemsCount(request.employedItemsCount() != null ? request.employedItemsCount() : 0)
            .basePointInTime(request.basePointInTime())
            .baseIndex(request.baseIndex() != null ? request.baseIndex() : BigDecimal.valueOf(1000.00))
            .favorite(request.favorite())
            .sourceType(SourceType.USER)
            .build();
    }

    public static IndexInfoCreateCommand fromApi(IndexInfoCreateRequest request) {
        return IndexInfoCreateCommand.builder()
            .indexClassification(request.indexClassification())
            .indexName(request.indexName())
            .employedItemsCount(request.employedItemsCount())
            .basePointInTime(request.basePointInTime())
            .baseIndex(request.baseIndex())
            .favorite(request.favorite())
            .sourceType(SourceType.OPEN_API)
            .build();
    }
}
