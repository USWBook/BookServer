package com.example.demo.domain.major.response;

import com.example.demo.global.response.RsData;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "전공 리스트 조회 성공 응답 DTO")
public class MajorListResponse extends RsData<List<MajorResponse>> {

    public MajorListResponse(String code, String message, List<MajorResponse> data) {
        super(code, message, data);
    }

    // 성공 응답을 쉽게 만들기 위한 정적 팩토리 메서드
    public static MajorListResponse of(String code, String message, List<MajorResponse> data) {
        return new MajorListResponse(code, message, data);
    }
}
