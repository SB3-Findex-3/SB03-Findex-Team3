package com.sprint.findex.entity;

import com.sprint.findex.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Entity
@Table(name = "auto_sync")
public class AutoSyncConfig extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "index_info_id", nullable = false)
    private IndexInfo indexInfo;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    public AutoSyncConfig(IndexInfo indexInfo, boolean enabled) {
        this.indexInfo = indexInfo;
        this.enabled = enabled;
    }
}
