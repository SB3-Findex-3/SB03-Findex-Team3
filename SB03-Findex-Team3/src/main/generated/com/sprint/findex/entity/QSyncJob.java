package com.sprint.findex.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSyncJob is a Querydsl query type for SyncJob
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSyncJob extends EntityPathBase<SyncJob> {

    private static final long serialVersionUID = 1182416498L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSyncJob syncJob = new QSyncJob("syncJob");

    public final com.sprint.findex.entity.base.QBaseEntity _super = new com.sprint.findex.entity.base.QBaseEntity(this);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QIndexInfo indexInfo;

    public final DateTimePath<java.time.OffsetDateTime> jobTime = createDateTime("jobTime", java.time.OffsetDateTime.class);

    public final EnumPath<SyncJobType> jobType = createEnum("jobType", SyncJobType.class);

    public final EnumPath<SyncJobResult> result = createEnum("result", SyncJobResult.class);

    public final DatePath<java.time.LocalDate> targetDate = createDate("targetDate", java.time.LocalDate.class);

    public final StringPath worker = createString("worker");

    public QSyncJob(String variable) {
        this(SyncJob.class, forVariable(variable), INITS);
    }

    public QSyncJob(Path<? extends SyncJob> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSyncJob(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSyncJob(PathMetadata metadata, PathInits inits) {
        this(SyncJob.class, metadata, inits);
    }

    public QSyncJob(Class<? extends SyncJob> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.indexInfo = inits.isInitialized("indexInfo") ? new QIndexInfo(forProperty("indexInfo")) : null;
    }

}

