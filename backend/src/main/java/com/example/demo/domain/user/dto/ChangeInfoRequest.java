package com.example.demo.domain.user.dto;


import com.example.demo.domain.user.entity.Grade;
import com.example.demo.domain.user.entity.Semester;

import java.util.UUID;

public record ChangeInfoRequest(

        String name,

    UUID majorId,

        Integer grade,

        Integer semester
) {
}
