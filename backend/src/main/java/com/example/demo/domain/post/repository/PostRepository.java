package com.example.demo.domain.post.repository;
// 게시글 저장소
import com.example.demo.domain.post.dto.response.PostListResponse;
import com.example.demo.domain.post.entity.Post;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID>, PostRepositoryCustom {

    // 만약 댓글이 추가 된다면 댓글들도 페치조인 해야함
    // 만약 댓글에서 채팅을 열 수 있으려면 댓글 단사람의 식별값이 필요해서 페치조인 필요
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.comments c " +
            "LEFT JOIN FETCH c.user " +
            "WHERE p.id = :postId")
    Optional<Post> findByIdWithCommentsAndUsers(@Param("postId") UUID postId);

    // 비관적 쓰기 락(PESSIMISTIC_WRITE)을 사용하여 Post를 조회.
    // 이 메서드가 호출되면, 트랜잭션이 끝날 때까지 해당 Post 레코드에 다른 트랜잭션이 접근할 수 없다.
    // 동시에 수많은 사람들이 찜 하게 될경우 찜갯수 무결성때문에 추가
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findByIdWithPessimisticLock(@Param("id") UUID id);


    // 아래는 동적 쿼리 적용 전에 사용하던 메서드임
//    // 게시글 목록 조회 (페이징 + 댓글 개수) 판매중과 판매완료 분리해야함
//    // PostListResponse의 생성자를 직접 호출하여 DTO로 반환
//    @Query("SELECT new com.example.demo.domain.post.dto.response.PostListResponse(" +
//            "p.id, p.title, p.postPrice, p.likeCount, COUNT(c.id), p.grade, p.semester, p.status, p.createdAt) " +
//            "FROM Post p LEFT JOIN p.comments c GROUP BY p.id")
//    Page<PostListResponse> findAllWithCommentCount(Pageable pageable);
//

//
//    // 책 이름 검색
//    @Query("SELECT new com.example.demo.domain.post.dto.response.PostListResponse(" +
//            "p.id, p.title, p.postPrice, p.likeCount, COUNT(c.id), p.grade, p.semester, p.status, p.createdAt) " +
//            "FROM Post p LEFT JOIN p.comments c WHERE p.postName LIKE %:bookname% GROUP BY p.id")
//    Page<PostListResponse> findBookNameWithCommentCount(Pageable pageable, String bookname);
//
//    // 강의명 검색
//    @Query("SELECT new com.example.demo.domain.post.dto.response.PostListResponse(" +
//            "p.id, p.title, p.postPrice, p.likeCount, COUNT(c.id), p.grade, p.semester, p.status, p.createdAt) " +
//            "FROM Post p LEFT JOIN p.comments c WHERE p.courseName LIKE %:classname% GROUP BY p.id")
//    Page<PostListResponse> findClassNameWithCommentCount(Pageable pageable, String classname);


}