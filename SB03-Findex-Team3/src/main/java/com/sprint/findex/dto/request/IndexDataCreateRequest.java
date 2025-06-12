package com.sprint.findex.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexDataCreateRequest(
    @NotNull Long indexInfoId,
    @NotNull LocalDate baseDate,
    @NotNull BigDecimal marketPrice,
    @NotNull BigDecimal closingPrice,
    @NotNull BigDecimal highPrice,
    @NotNull BigDecimal lowPrice,
    @NotNull BigDecimal versus,
    @NotNull BigDecimal fluctuationRate,
    @NotNull Long tradingQuantity,
    @NotNull Long tradingPrice,
    @NotNull Long marketTotalAmount
) {

}
