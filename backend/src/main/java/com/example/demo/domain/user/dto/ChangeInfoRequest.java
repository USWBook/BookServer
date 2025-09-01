package com.example.demo.domain.user.dto;



public record ChangeInfoRequest(

    String majorName,

    Integer grade,

    Integer semester
) {
}
