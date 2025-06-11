package com.sprint.findex.entity;

import com.sprint.findex.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "auto_sync")
@Builder
public class AutoSyncConfig extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "index_info_id", nullable = false)
    private IndexInfo indexInfo;

    @Column(name = "index_classification", nullable = false, length = 240)
    private String indexClassification;

    @Column(name = "index_name", nullable = false, length = 240)
    private String indexName;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    public AutoSyncConfig(IndexInfo indexInfo) {
        this.indexInfo = indexInfo;
        this.indexClassification = indexInfo.getIndexClassification();
        this.indexName = indexInfo.getIndexName();
        this.enabled = false;
    }

    public static AutoSyncConfig ofIndexInfo(IndexInfo indexInfo) {
        return AutoSyncConfig.builder()
            .indexInfo(indexInfo)
            .indexClassification(indexInfo.getIndexClassification())
            .indexName(indexInfo.getIndexName())
            .enabled(false)
            .build();
    }
}
