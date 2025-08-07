package com.example.demo.domain.major.response;

import com.example.demo.domain.major.entity.Major;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MajorResponse {
    private String name;

    public static MajorResponse from(Major major) {
        return new MajorResponse(major.getName());
    }
}

