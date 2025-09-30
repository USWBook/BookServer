package com.example.demo.domain.report.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserReport is a Querydsl query type for UserReport
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserReport extends EntityPathBase<UserReport> {

    private static final long serialVersionUID = 828932320L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserReport userReport = new QUserReport("userReport");

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final EnumPath<com.example.demo.domain.report.enums.ReportReason> reason = createEnum("reason", com.example.demo.domain.report.enums.ReportReason.class);

    public final com.example.demo.domain.user.entity.QUser reported;

    public final DateTimePath<java.time.LocalDateTime> reportedAt = createDateTime("reportedAt", java.time.LocalDateTime.class);

    public final com.example.demo.domain.user.entity.QUser reporter;

    public QUserReport(String variable) {
        this(UserReport.class, forVariable(variable), INITS);
    }

    public QUserReport(Path<? extends UserReport> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserReport(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserReport(PathMetadata metadata, PathInits inits) {
        this(UserReport.class, metadata, inits);
    }

    public QUserReport(Class<? extends UserReport> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.reported = inits.isInitialized("reported") ? new com.example.demo.domain.user.entity.QUser(forProperty("reported"), inits.get("reported")) : null;
        this.reporter = inits.isInitialized("reporter") ? new com.example.demo.domain.user.entity.QUser(forProperty("reporter"), inits.get("reporter")) : null;
    }

}

