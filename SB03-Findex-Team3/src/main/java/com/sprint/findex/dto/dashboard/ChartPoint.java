package com.sprint.findex.dto.dashboard;

import java.math.BigDecimal;

public record ChartPoint(
    String date,
    BigDecimal value
) {
}
