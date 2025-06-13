package com.sprint.findex.entity;

import static org.hibernate.annotations.OnDeleteAction.CASCADE;

import com.sprint.findex.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;

//@Getter
//@Setter
//@ToString
//@NoArgsConstructor
//@AllArgsConstructor
//@Entity
//@Table(name = "auto_sync")
//@Builder
//public class AutoSyncConfig extends BaseEntity {
//
//    @OneToOne
//    @JoinColumn(name = "index_info_id", nullable = false)
//    private IndexInfo indexInfo;
//
//    @Column(name = "index_classification", nullable = false, length = 240)
//    private String indexClassification;
//
//    @Column(name = "index_name", nullable = false, length = 240)
//    private String indexName;
//
//    @Column(name = "enabled", nullable = false)
//    private boolean enabled;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "auto_sync")
@Builder
public class AutoSyncConfig extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "index_info_id", nullable = false)
    @OnDelete(action = CASCADE)
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
