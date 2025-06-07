package com.sprint.findex.dto.response;

import com.sprint.findex.entity.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexDataDto(
    Long id,
    LocalDate baseDate,
    SourceType sourceType,
    BigDecimal closingPrice,
    BigDecimal marketPrice,
    BigDecimal highPrice,
    BigDecimal lowPrice,
    BigDecimal versus,
    BigDecimal fluctuationRate,
    Long tradingQuantity,
    Long tradingPrice,
    Long marketTotalAmount
) {

}