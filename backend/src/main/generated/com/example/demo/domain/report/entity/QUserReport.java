package com.example.demo.domain.report.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserReport is a Querydsl query type for UserReport
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserReport extends EntityPathBase<UserReport> {

    private static final long serialVersionUID = 828932320L;

    public static final QUserReport userReport = new QUserReport("userReport");

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final EnumPath<com.example.demo.domain.report.enums.ReportReason> reason = createEnum("reason", com.example.demo.domain.report.enums.ReportReason.class);

    public final DateTimePath<java.time.LocalDateTime> reportedAt = createDateTime("reportedAt", java.time.LocalDateTime.class);

    public final StringPath reporterName = createString("reporterName");

    public final ComparablePath<java.util.UUID> reportThingId = createComparable("reportThingId", java.util.UUID.class);

    public final EnumPath<com.example.demo.domain.report.enums.ReportType> reportType = createEnum("reportType", com.example.demo.domain.report.enums.ReportType.class);

    public QUserReport(String variable) {
        super(UserReport.class, forVariable(variable));
    }

    public QUserReport(Path<? extends UserReport> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserReport(PathMetadata metadata) {
        super(UserReport.class, metadata);
    }

}

