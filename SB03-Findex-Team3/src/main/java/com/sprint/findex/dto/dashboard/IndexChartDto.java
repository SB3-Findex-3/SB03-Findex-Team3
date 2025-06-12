package com.sprint.findex.dto.dashboard;

import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.Period;
import java.util.List;
import lombok.Builder;

@Builder
public record IndexChartDto(
    Long indexInfoId,
    String indexClassification,
    String indexName,
    Period period,
    List<ChartPoint> dataPoints,
    List<ChartPoint> ma5DataPoints,
    List<ChartPoint> ma20DataPoints
) {

    public static IndexChartDto from(IndexInfo indexInfo, Period period, List<ChartPoint> dataPoints,
        List<ChartPoint> ma5DataPoints, List<ChartPoint> ma20DataPoints) {
        return IndexChartDto.builder()
            .indexInfoId(indexInfo.getId())
            .indexClassification(indexInfo.getIndexClassification())
            .indexName(indexInfo.getIndexName())
            .period(period)
            .dataPoints(dataPoints)
            .ma5DataPoints(ma5DataPoints)
            .ma20DataPoints(ma20DataPoints)
            .build();
    }
}
