package com.example.demo.domain.user.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {

    private String name;
    private String majorName;
    private String email;
    private int grade;
    private int semester;


}
