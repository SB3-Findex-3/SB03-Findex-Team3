package com.sprint.findex.entity;

import com.sprint.findex.dto.request.IndexInfoCreateCommand;
import com.sprint.findex.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "index_info",
    uniqueConstraints = @UniqueConstraint(columnNames = {"index_classification", "index_name"}))
@Getter
@Entity
@Builder
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

    public void  updateSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
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

    public static IndexInfo create(IndexInfoCreateCommand command) {
        return IndexInfo.builder()
                .indexClassification(command.indexClassification())
                .indexName(command.indexName())
                .employedItemsCount(command.employedItemsCount())
                .basePointInTime(command.basePointInTime())
                .baseIndex(command.baseIndex())
                .favorite(command.favorite())
                .sourceType(command.sourceType())
                .build();
    }

}