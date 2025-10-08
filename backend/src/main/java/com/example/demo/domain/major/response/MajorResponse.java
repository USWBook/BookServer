package com.example.demo.domain.major.response;

import com.example.demo.domain.major.entity.Major;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Schema(description = "전공 정보 응답 DTO")
public class MajorResponse {
    @Schema(description = "전공 식별값", example = "ad9acc20-7bc6-4c05-8e5f-a540157fabce")
    private UUID id;
    @Schema(description = "전공 이름", example = "기계공학과")
    private String name;

    public static MajorResponse from(Major major) {
        return new MajorResponse(major.getId(), major.getName());
    }
}