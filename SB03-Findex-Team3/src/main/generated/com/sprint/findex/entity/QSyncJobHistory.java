package com.sprint.findex.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSyncJobHistory is a Querydsl query type for SyncJobHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSyncJobHistory extends EntityPathBase<SyncJobHistory> {

    private static final long serialVersionUID = -1042122302L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSyncJobHistory syncJobHistory = new QSyncJobHistory("syncJobHistory");

    public final com.sprint.findex.entity.base.QBaseEntity _super = new com.sprint.findex.entity.base.QBaseEntity(this);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final QIndexInfo indexInfo;

    public final EnumPath<SyncJobResult> jobResult = createEnum("jobResult", SyncJobResult.class);

    public final DateTimePath<java.time.OffsetDateTime> jobTime = createDateTime("jobTime", java.time.OffsetDateTime.class);

    public final EnumPath<SyncJobType> jobType = createEnum("jobType", SyncJobType.class);

    public final DatePath<java.time.LocalDate> targetDate = createDate("targetDate", java.time.LocalDate.class);

    public final StringPath worker = createString("worker");

    public QSyncJobHistory(String variable) {
        this(SyncJobHistory.class, forVariable(variable), INITS);
    }

    public QSyncJobHistory(Path<? extends SyncJobHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSyncJobHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSyncJobHistory(PathMetadata metadata, PathInits inits) {
        this(SyncJobHistory.class, metadata, inits);
    }

    public QSyncJobHistory(Class<? extends SyncJobHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.indexInfo = inits.isInitialized("indexInfo") ? new QIndexInfo(forProperty("indexInfo")) : null;
    }

}

