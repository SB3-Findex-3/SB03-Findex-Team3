package com.sprint.findex.dto.dashboard;

import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import java.math.BigDecimal;
import java.math.RoundingMode;

public record IndexPerformanceDto(
    Long indexInfoId,
    String indexClassification,
    String indexName,
    BigDecimal versus,
    BigDecimal fluctuationRate,
    BigDecimal currentPrice,
    BigDecimal beforePrice
) {
    public static IndexPerformanceDto of(IndexInfo info, IndexData current, IndexData before) {
        if (current == null || before == null) { return null; }

        // 0으로 나누기 방지
        if (before.getClosingPrice().compareTo(BigDecimal.ZERO) == 0) {
            return null; // 또는 적절한 기본값 설정
        }

        BigDecimal versus = current.getClosingPrice().subtract(before.getClosingPrice());
        BigDecimal fluctuationRate = versus.divide(before.getClosingPrice(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        return new IndexPerformanceDto(
            info.getId(),
            info.getIndexClassification(),
            info.getIndexName(),
            versus,
            fluctuationRate,
            current.getClosingPrice(),
            before.getClosingPrice()
        );
    }
}
