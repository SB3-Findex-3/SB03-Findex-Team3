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

    @Schema(
        description = "지수 ID",
        example = "3"
    )
    private Long id;

    @Schema(
        description = "지수 분류",
        example = "KRX시리즈"
    )
    private String indexClassification;

    @Schema(
        description = "지수 이름",
        example = "KRX 300 헬스케어"
    )
    private String indexName;
}
