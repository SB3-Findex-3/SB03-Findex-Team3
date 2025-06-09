package com.sprint.findex.entity;

import com.sprint.findex.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sync_job")
@Getter
@Entity
public class SyncJobHistory extends BaseEntity {


    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 15)
    private SyncJobType jobType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_info_id", nullable = false)
    private IndexInfo indexInfo;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "worker", length = 15, nullable = false)
    private String worker;

    @Column(name = "job_time", nullable = false)
    private OffsetDateTime jobTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 10, nullable = false)
    private SyncJobResult jobResult;

    public SyncJobHistory(SyncJobType jobType, IndexInfo indexInfo, LocalDate targetDate,
        String worker, OffsetDateTime jobTime, SyncJobResult jobResult) {
        this.jobType = jobType;
        this.indexInfo = indexInfo;
        this.targetDate = targetDate;
        this.worker = worker;
        this.jobTime = jobTime;
        this.jobResult = jobResult;
    }

    public static SyncJobHistory create(SyncJobType jobType, IndexInfo indexInfo, LocalDate targetDate,
        String worker, OffsetDateTime jobTime, SyncJobResult jobResult) {
        return new SyncJobHistory(jobType, indexInfo, targetDate, worker, jobTime, jobResult);
    }
}
