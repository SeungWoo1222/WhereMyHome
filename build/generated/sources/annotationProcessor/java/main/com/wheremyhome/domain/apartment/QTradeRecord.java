package com.wheremyhome.domain.apartment;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTradeRecord is a Querydsl query type for TradeRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTradeRecord extends EntityPathBase<TradeRecord> {

    private static final long serialVersionUID = -33878440L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTradeRecord tradeRecord = new QTradeRecord("tradeRecord");

    public final NumberPath<java.math.BigDecimal> area = createNumber("area", java.math.BigDecimal.class);

    public final QApartmentComplex complex;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Short> floor = createNumber("floor", Short.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final DatePath<java.time.LocalDate> tradeDate = createDate("tradeDate", java.time.LocalDate.class);

    public QTradeRecord(String variable) {
        this(TradeRecord.class, forVariable(variable), INITS);
    }

    public QTradeRecord(Path<? extends TradeRecord> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTradeRecord(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTradeRecord(PathMetadata metadata, PathInits inits) {
        this(TradeRecord.class, metadata, inits);
    }

    public QTradeRecord(Class<? extends TradeRecord> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.complex = inits.isInitialized("complex") ? new QApartmentComplex(forProperty("complex"), inits.get("complex")) : null;
    }

}

