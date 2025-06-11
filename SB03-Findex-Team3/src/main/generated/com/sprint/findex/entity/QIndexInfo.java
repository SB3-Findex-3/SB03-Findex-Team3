package com.sprint.findex.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QIndexInfo is a Querydsl query type for IndexInfo
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIndexInfo extends EntityPathBase<IndexInfo> {

    private static final long serialVersionUID = 1148040176L;

    public static final QIndexInfo indexInfo = new QIndexInfo("indexInfo");

    public final com.sprint.findex.entity.base.QBaseEntity _super = new com.sprint.findex.entity.base.QBaseEntity(this);

    public final NumberPath<java.math.BigDecimal> baseIndex = createNumber("baseIndex", java.math.BigDecimal.class);

    public final DatePath<java.time.LocalDate> basePointInTime = createDate("basePointInTime", java.time.LocalDate.class);

    public final NumberPath<Integer> employedItemsCount = createNumber("employedItemsCount", Integer.class);

    public final BooleanPath favorite = createBoolean("favorite");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath indexClassification = createString("indexClassification");

    public final StringPath indexName = createString("indexName");

    public final EnumPath<SourceType> sourceType = createEnum("sourceType", SourceType.class);

    public QIndexInfo(String variable) {
        super(IndexInfo.class, forVariable(variable));
    }

    public QIndexInfo(Path<? extends IndexInfo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIndexInfo(PathMetadata metadata) {
        super(IndexInfo.class, metadata);
    }

}

