package com.sprint.findex.entity;

import static org.hibernate.annotations.OnDeleteAction.CASCADE;

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
import org.hibernate.annotations.OnDelete;

//@Entity
//@Table(name = "sync_job")
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class SyncJob extends BaseEntity {
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "job_type", nullable = false, length = 20)
//    private SyncJobType jobType;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "index_info_id")
//    private IndexInfo indexInfo;
//
//    @Column(name = "target_date", nullable = true)
//    private LocalDate targetDate;
//
//    @Column(name = "worker", nullable = false, length = 15)
//    private String worker;
//
//    @Column(name = "job_time", nullable = false)
//    private OffsetDateTime jobTime;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "result", nullable = false, length = 10)
//    private SyncJobResult result;

@Entity
@Table(name = "sync_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SyncJob extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_info_id", nullable = false)
    @OnDelete(action = CASCADE)
    private IndexInfo indexInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 10)
    private SyncJobType jobType;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "worker", nullable = false, length = 15)
    private String worker;

    @Column(name = "job_time", nullable = false)
    private OffsetDateTime jobTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 10)
    private SyncJobResult result;

    public SyncJob(
        SyncJobType jobType,
        IndexInfo indexInfo,
        LocalDate targetDate,
        String worker,
        OffsetDateTime jobTime,
        SyncJobResult result
    ) {
        this.jobType = jobType;
        this.indexInfo = indexInfo;
        this.targetDate = targetDate;
        this.worker = worker;
        this.jobTime = jobTime;
        this.result = result;

    }
    public static SyncJob fail(IndexInfo indexInfo, LocalDate targetDate, String worker) {
        return new SyncJob(
            SyncJobType.INDEX_DATA,
            indexInfo,
            targetDate,
            worker,
            OffsetDateTime.now(),  // 현재 시간으로 기록
            SyncJobResult.FAILED   // 실패 상태로 지정
        );
    }
}
