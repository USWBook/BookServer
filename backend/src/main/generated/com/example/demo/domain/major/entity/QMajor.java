package com.example.demo.domain.major.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMajor is a Querydsl query type for Major
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMajor extends EntityPathBase<Major> {

    private static final long serialVersionUID = 804813075L;

    public static final QMajor major = new QMajor("major");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final DateTimePath<java.time.LocalDateTime> modifiedAt = createDateTime("modifiedAt", java.time.LocalDateTime.class);

    public final StringPath name = createString("name");

    public QMajor(String variable) {
        super(Major.class, forVariable(variable));
    }

    public QMajor(Path<? extends Major> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMajor(PathMetadata metadata) {
        super(Major.class, metadata);
    }

}

