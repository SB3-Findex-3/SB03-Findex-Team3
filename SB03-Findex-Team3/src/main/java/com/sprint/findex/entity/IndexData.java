package com.sprint.findex.entity;

import com.sprint.findex.dto.request.IndexDataCreateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "index_data",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"index_info_id", "base_date"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class IndexData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "index_info_id", nullable = false)
    private IndexInfo indexInfo;

    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "market_price", precision = 10, scale = 2)
    private BigDecimal marketPrice;

    @Column(name = "closing_price", precision = 10, scale = 2)
    private BigDecimal closingPrice;

    @Column(name = "high_price", precision = 10, scale = 2)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 10, scale = 2)
    private BigDecimal lowPrice;

    @Column(name = "versus", precision = 10, scale = 2)
    private BigDecimal versus;

    @Column(name = "fluctuation_rate", precision = 10, scale = 2)
    private BigDecimal fluctuationRate;

    @Column(name = "trading_quantity")
    private Long tradingQuantity;

    @Column(name = "trading_price")
    private Long tradingPrice;

    @Column(name = "market_total_amount")
    private Long marketTotalAmount;

    public IndexData(IndexInfo indexInfo, LocalDate baseDate, SourceType sourceType,
        BigDecimal marketPrice, BigDecimal closingPrice, BigDecimal highPrice, BigDecimal lowPrice,
        BigDecimal versus, BigDecimal fluctuationRate, Long tradingQuantity, Long tradingPrice,
        Long marketTotalAmount) {
        this.indexInfo = indexInfo;
        this.baseDate = baseDate;
        this.sourceType = sourceType;
        this.marketPrice = marketPrice;
        this.closingPrice = closingPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.versus = versus;
        this.fluctuationRate = fluctuationRate;
        this.tradingQuantity = tradingQuantity;
        this.tradingPrice = tradingPrice;
        this.marketTotalAmount = marketTotalAmount;
    }

    public static IndexData from(IndexInfo indexInfo, IndexDataCreateRequest request, SourceType sourceType) {
        return new IndexData(indexInfo, request.baseDate(), sourceType,
            request.marketPrice(), request.closingPrice(), request.highPrice(),
            request.lowPrice(), request.versus(), request.fluctuationRate(), request.tradingQuantity(),
            request.tradingPrice(), request.marketTotalAmount());
    }
}
