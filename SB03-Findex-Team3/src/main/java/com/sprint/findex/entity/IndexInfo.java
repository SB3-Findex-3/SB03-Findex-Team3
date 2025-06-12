package com.sprint.findex.entity;

import com.sprint.findex.dto.request.IndexInfoCreateCommand;
import com.sprint.findex.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "index_info",
    uniqueConstraints = @UniqueConstraint(columnNames = {"index_classification", "index_name"}))
@Getter
@Entity
public class IndexInfo extends BaseEntity {

    @Column(name = "index_classification", length = 240, unique = true, nullable = false)
    private String indexClassification;

    @Column(name = "index_name", length = 240, unique = true, nullable = false)
    private String indexName;

    @Column(name = "employed_items_count", nullable = false)
    private int employedItemsCount;

    @Column(name = "base_point_in_time", nullable = false)
    private LocalDate basePointInTime;

    @Column(name = "base_index", nullable = false)
    private BigDecimal baseIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 10, nullable = false)
    private SourceType sourceType;

    @Column(name = "favorite", nullable = false)
    private boolean favorite;

    public IndexInfo(String indexClassification, String indexName, int employedItemsCount,
        LocalDate basePointInTime, BigDecimal baseIndex, SourceType sourceType, boolean favorite) {
        this.indexClassification = indexClassification;
        this.indexName = indexName;
        this.employedItemsCount = employedItemsCount;
        this.basePointInTime = basePointInTime;
        this.baseIndex = baseIndex;
        this.sourceType = sourceType;
        this.favorite = favorite;
    }

    public static IndexInfo create(IndexInfoCreateCommand command) {
        return new IndexInfo(
            command.indexClassification(),
            command.indexName(),
            command.employedItemsCount(),
            command.basePointInTime(),
            command.baseIndex(),
            command.sourceType(),
            command.favorite()
        );
    }

    public void updateIndexClassification(String indexClassification) {
        this.indexClassification = indexClassification;
    }

    public void  updateIndexName(String indexName) {
        this.indexName = indexName;
    }

    public void  updateEmployedItemsCount(int employedItemsCount) {
        this.employedItemsCount = employedItemsCount;
    }

    public void  updateBasePointInTime(LocalDate basePointInTime) {
        this.basePointInTime = basePointInTime;
    }

    public void  updateBaseIndex(BigDecimal baseIndex) {
        this.baseIndex = baseIndex;
    }

    public void  updateFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public String toString() {
        return "IndexInfo{" +
            "indexClassification='" + indexClassification + '\'' +
            ", indexName='" + indexName + '\'' +
            ", employedItemsCount=" + employedItemsCount +
            ", basePointInTime=" + basePointInTime +
            ", baseIndex=" + baseIndex +
            ", sourceType=" + sourceType +
            ", favorite=" + favorite +
            "} " + super.toString();
    }
}