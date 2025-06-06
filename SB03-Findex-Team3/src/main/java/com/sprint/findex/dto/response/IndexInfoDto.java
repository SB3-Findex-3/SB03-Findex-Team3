package com.sprint.findex.dto.response;

import com.sprint.findex.entity.SourceType;
import java.time.LocalDate;

public record IndexInfoDto(
    Long id,
    String indexClassification,
    String indexName,
    int employedItemsCount,
    LocalDate basePointInTime,
    int baseIndex,
    SourceType sourceType,
    Boolean favorite
) { }
