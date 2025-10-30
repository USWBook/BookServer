package com.example.demo.domain.purchase.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPurchaseHistory is a Querydsl query type for PurchaseHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPurchaseHistory extends EntityPathBase<PurchaseHistory> {

    private static final long serialVersionUID = -300045691L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPurchaseHistory purchaseHistory = new QPurchaseHistory("purchaseHistory");

    public final com.example.demo.domain.user.entity.QUser buyer;

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final com.example.demo.domain.post.entity.QPost post;

    public final DateTimePath<java.time.LocalDateTime> transactionDate = createDateTime("transactionDate", java.time.LocalDateTime.class);

    public QPurchaseHistory(String variable) {
        this(PurchaseHistory.class, forVariable(variable), INITS);
    }

    public QPurchaseHistory(Path<? extends PurchaseHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPurchaseHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPurchaseHistory(PathMetadata metadata, PathInits inits) {
        this(PurchaseHistory.class, metadata, inits);
    }

    public QPurchaseHistory(Class<? extends PurchaseHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.buyer = inits.isInitialized("buyer") ? new com.example.demo.domain.user.entity.QUser(forProperty("buyer"), inits.get("buyer")) : null;
        this.post = inits.isInitialized("post") ? new com.example.demo.domain.post.entity.QPost(forProperty("post"), inits.get("post")) : null;
    }

}

