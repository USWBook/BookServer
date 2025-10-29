package com.example.demo.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -25341789L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final StringPath email = createString("email");

    public final EnumPath<com.example.demo.domain.user.enums.Grade> grade = createEnum("grade", com.example.demo.domain.user.enums.Grade.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final com.example.demo.domain.major.entity.QMajor major;

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final EnumPath<com.example.demo.domain.user.role.Role> role = createEnum("role", com.example.demo.domain.user.role.Role.class);

    public final EnumPath<com.example.demo.domain.user.enums.Semester> semester = createEnum("semester", com.example.demo.domain.user.enums.Semester.class);

    public final EnumPath<com.example.demo.domain.user.enums.UserStatus> status = createEnum("status", com.example.demo.domain.user.enums.UserStatus.class);

    public final StringPath studentId = createString("studentId");

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.major = inits.isInitialized("major") ? new com.example.demo.domain.major.entity.QMajor(forProperty("major")) : null;
    }

}

