package com.sprint.findex.entity;

import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.request.IndexDataUpdateRequest;
import com.sprint.findex.entity.base.BaseEntity;
import com.sprint.findex.global.dto.MarketIndexResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
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
public class IndexData extends BaseEntity {

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

    public void update(IndexDataUpdateRequest request) {
        if (request.marketPrice() != null && !request.marketPrice().equals(this.marketPrice)) {
            this.marketPrice = request.marketPrice();
        }
        if (request.closingPrice() != null && !request.closingPrice().equals(this.closingPrice)) {
            this.closingPrice = request.closingPrice();
        }
        if (request.highPrice() != null && !request.highPrice().equals(this.highPrice)) {
            this.highPrice = request.highPrice();
        }
        if (request.lowPrice() != null && !request.lowPrice().equals(this.lowPrice)) {
            this.lowPrice = request.lowPrice();
        }
        if (request.versus() != null && !request.versus().equals(this.versus)) {
            this.versus = request.versus();
        }
        if (request.fluctuationRate() != null && !request.fluctuationRate().equals(this.fluctuationRate)) {
            this.fluctuationRate = request.fluctuationRate();
        }
        if (request.tradingQuantity() != null && !request.tradingQuantity().equals(this.tradingQuantity)) {
            this.tradingQuantity = request.tradingQuantity();
        }
        if (request.tradingPrice() != null && !request.tradingPrice().equals(this.tradingPrice)) {
            this.tradingPrice = request.tradingPrice();
        }
        if (request.marketTotalAmount() != null && !request.marketTotalAmount()
            .equals(this.marketTotalAmount)) {
            this.marketTotalAmount = request.marketTotalAmount();
        }
    }
    public void updateFromApi(MarketIndexResponse.MarketIndexData item) {
        this.marketPrice = item.getMkp();
        this.closingPrice = item.getClpr();
        this.highPrice = item.getHipr();
        this.lowPrice = item.getLopr();
        this.versus = item.getVs();
        this.fluctuationRate = item.getFltRt();
        this.tradingQuantity = item.getTrqu();
        this.tradingPrice = item.getTrPrc();
        this.marketTotalAmount = item.getLstgMrktTotAmt();
    }
}
