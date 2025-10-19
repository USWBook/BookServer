package com.example.demo.domain.admin.controller;

import com.example.demo.domain.admin.dto.request.BanRequestByAdmin;
import com.example.demo.domain.admin.dto.request.DeletePostRequestByAdmin;
import com.example.demo.domain.admin.dto.response.ReportResponse;
import com.example.demo.domain.post.service.PostService;
import com.example.demo.domain.report.service.ReportService;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final PostService postService;
    private final UserService userService;
    private final ReportService reportService;

    //  신고된 게시물/채팅 목록 조회
    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public RsData<List<ReportResponse>> getReportedPosts() {
        List<ReportResponse> reportListResponses = reportService.reportList();
        return RsData.of("200", "신고 목록 조회 성공",reportListResponses);
    }

    //  관리자에 의한 게시물 삭제
    @DeleteMapping("/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public RsData<Void> deletePost(@RequestBody DeletePostRequestByAdmin deletePostRequestByAdmin) {
        postService.deletePostByAdmin(deletePostRequestByAdmin.postId());
        return RsData.of("200", "게시물이 삭제되었습니다.");
    }

    // 게시물/채팅 작성자 밴
    @PostMapping("/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public RsData<Void> banUser(@RequestBody BanRequestByAdmin banRequest) {
        userService.banUser(banRequest.userName());
        return RsData.of("200", "해당 유저를 정지 처리했습니다.");
    }
}
