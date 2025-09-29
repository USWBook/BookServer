package com.example.demo.domain.user.dto;


import java.util.UUID;

public record ChangeInfoRequest(

        String name,

    UUID majorId,

    Integer grade,

    Integer semester
) {
}
