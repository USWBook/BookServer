package com.example.demo.domain.post.repository;

import com.example.demo.domain.post.dto.request.PostSearchCondition;
import com.example.demo.domain.post.dto.response.PostListResponse;
import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.post.enums.PostStatus;
import com.example.demo.domain.user.enums.Grade;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.example.demo.domain.post.entity.QPost.post;
import static com.example.demo.domain.post.entity.QPostComment.postComment;

// 동적 쿼리 검색 구현 클래스
@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostListResponse> search(PostSearchCondition condition, Pageable pageable) {
        // Post 엔티티와 댓글 수를 함께 조회
        List<Tuple> results = queryFactory
                .select(
                        post,
                        postComment.id.count().as("commentCount")
                )
                .from(post)
                .leftJoin(post.comments, postComment)
                .where(
                        gradeEq(condition.getGrade()),
                        statusEq(condition.getStatus()),
                        bookNameContains(condition.getBookName()),
                        classNameContains(condition.getClassName())
                )
                .groupBy(post.id)
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // PostListResponse로 변환
        List<PostListResponse> content = results.stream()
                .map(tuple -> {
                    Post p = tuple.get(post);
                    Long commentCount = tuple.get(postComment.id.count());
                    return new PostListResponse(
                            p.getId(),
                            p.getTitle(),
                            p.getFirstImageUrl(), // 첫 번째 이미지만 사용
                            p.getPostPrice(),
                            p.getLikeCount(),
                            commentCount,
                            p.getGrade(),
                            p.getSemester(),
                            p.getStatus(),
                            p.getCreatedAt()
                    );
                })
                .toList();

        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        gradeEq(condition.getGrade()),
                        statusEq(condition.getStatus()),
                        bookNameContains(condition.getBookName()),
                        classNameContains(condition.getClassName())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression gradeEq(Integer grade) {
        if (grade == null) {
            return null;
        }
        // Integer로 받은 값을 Grade enum으로 변환한 뒤, 쿼리 조건으로 사용합니다.
        return post.grade.eq(Grade.fromValue(grade));
    }

    private BooleanExpression statusEq(PostStatus status) {
        return status != null ? post.status.eq(status) : null;
    }

    private BooleanExpression bookNameContains(String bookName) {
        if (!StringUtils.hasText(bookName)) {
            return null;
        }
        return post.postName.containsIgnoreCase(bookName);
    }

    private BooleanExpression classNameContains(String className) {
        if (!StringUtils.hasText(className)) {
            return null;
        }
        return post.courseName.containsIgnoreCase(className);
    }
}