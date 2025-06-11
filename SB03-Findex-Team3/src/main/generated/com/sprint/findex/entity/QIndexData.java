package com.sprint.findex.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QIndexData is a Querydsl query type for IndexData
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIndexData extends EntityPathBase<IndexData> {

    private static final long serialVersionUID = 1147879148L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIndexData indexData = new QIndexData("indexData");

    public final com.sprint.findex.entity.base.QBaseEntity _super = new com.sprint.findex.entity.base.QBaseEntity(this);

    public final DatePath<java.time.LocalDate> baseDate = createDate("baseDate", java.time.LocalDate.class);

    public final NumberPath<java.math.BigDecimal> closingPrice = createNumber("closingPrice", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> fluctuationRate = createNumber("fluctuationRate", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> highPrice = createNumber("highPrice", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QIndexInfo indexInfo;

    public final NumberPath<java.math.BigDecimal> lowPrice = createNumber("lowPrice", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> marketPrice = createNumber("marketPrice", java.math.BigDecimal.class);

    public final NumberPath<Long> marketTotalAmount = createNumber("marketTotalAmount", Long.class);

    public final EnumPath<SourceType> sourceType = createEnum("sourceType", SourceType.class);

    public final NumberPath<Long> tradingPrice = createNumber("tradingPrice", Long.class);

    public final NumberPath<Long> tradingQuantity = createNumber("tradingQuantity", Long.class);

    public final NumberPath<java.math.BigDecimal> versus = createNumber("versus", java.math.BigDecimal.class);

    public QIndexData(String variable) {
        this(IndexData.class, forVariable(variable), INITS);
    }

    public QIndexData(Path<? extends IndexData> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QIndexData(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QIndexData(PathMetadata metadata, PathInits inits) {
        this(IndexData.class, metadata, inits);
    }

    public QIndexData(Class<? extends IndexData> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.indexInfo = inits.isInitialized("indexInfo") ? new QIndexInfo(forProperty("indexInfo")) : null;
    }

}

