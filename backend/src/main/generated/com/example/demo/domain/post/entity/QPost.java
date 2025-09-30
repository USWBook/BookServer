package com.example.demo.domain.post.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPost is a Querydsl query type for Post
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPost extends EntityPathBase<Post> {

    private static final long serialVersionUID = 1717244621L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPost post = new QPost("post");

    public final ListPath<PostComment, QPostComment> comments = this.<PostComment, QPostComment>createList("comments", PostComment.class, QPostComment.class, PathInits.DIRECT2);

    public final StringPath content = createString("content");

    public final StringPath courseName = createString("courseName");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> grade = createNumber("grade", Integer.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    public final com.example.demo.domain.major.entity.QMajor major;

    public final DateTimePath<java.time.LocalDateTime> modifiedAt = createDateTime("modifiedAt", java.time.LocalDateTime.class);

    public final StringPath postImage = createString("postImage");

    public final StringPath postName = createString("postName");

    public final NumberPath<Integer> postPrice = createNumber("postPrice", Integer.class);

    public final StringPath professor = createString("professor");

    public final com.example.demo.domain.user.entity.QUser seller;

    public final NumberPath<Integer> semester = createNumber("semester", Integer.class);

    public final EnumPath<com.example.demo.domain.post.enums.PostStatus> status = createEnum("status", com.example.demo.domain.post.enums.PostStatus.class);

    public final StringPath title = createString("title");

    public QPost(String variable) {
        this(Post.class, forVariable(variable), INITS);
    }

    public QPost(Path<? extends Post> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPost(PathMetadata metadata, PathInits inits) {
        this(Post.class, metadata, inits);
    }

    public QPost(Class<? extends Post> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.major = inits.isInitialized("major") ? new com.example.demo.domain.major.entity.QMajor(forProperty("major")) : null;
        this.seller = inits.isInitialized("seller") ? new com.example.demo.domain.user.entity.QUser(forProperty("seller"), inits.get("seller")) : null;
    }

}

