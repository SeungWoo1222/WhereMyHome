package com.wheremyhome.domain.apartment;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QApartmentComplex is a Querydsl query type for ApartmentComplex
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QApartmentComplex extends EntityPathBase<ApartmentComplex> {

    private static final long serialVersionUID = -1961139333L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QApartmentComplex apartmentComplex = new QApartmentComplex("apartmentComplex");

    public final NumberPath<Short> builtYear = createNumber("builtYear", Short.class);

    public final StringPath complexName = createString("complexName");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath dongName = createString("dongName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.wheremyhome.domain.region.QRegion region;

    public QApartmentComplex(String variable) {
        this(ApartmentComplex.class, forVariable(variable), INITS);
    }

    public QApartmentComplex(Path<? extends ApartmentComplex> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QApartmentComplex(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QApartmentComplex(PathMetadata metadata, PathInits inits) {
        this(ApartmentComplex.class, metadata, inits);
    }

    public QApartmentComplex(Class<? extends ApartmentComplex> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.region = inits.isInitialized("region") ? new com.wheremyhome.domain.region.QRegion(forProperty("region")) : null;
    }

}

