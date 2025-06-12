package com.sprint.findex.dto.dashboard;

public record RankedIndexPerformanceDto(
    IndexPerformanceDto performance,
    int rank
) {

}
