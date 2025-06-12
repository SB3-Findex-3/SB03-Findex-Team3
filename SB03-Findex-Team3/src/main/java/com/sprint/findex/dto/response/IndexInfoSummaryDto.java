package com.sprint.findex.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Schema(description = "지수 정보 요약")
public class IndexInfoSummaryDto {
    private Long id;
    private String indexClassification;
    private String indexName;
}
