package com.example.demo.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentSignUpRequest {

    @NotBlank(message = "학번은 필수 입력 항목입니다.")
    private String studentCode;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$",
            message = "비밀번호는 8자 이상이며 영문자와 숫자를 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
    private String confirmPassword;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @NotBlank(message = "전공은 필수 입력 항목입니다.")
    private String major;

    @NotNull(message = "학년은 필수 입력 항목입니다.")
    private Integer grade; // enum {1,2,3,4}

    @NotNull(message = "학기는 필수 입력 항목입니다.")
    private Integer semester; // enum {1,2}

    @NotBlank(message = "이메일 인증 코드는 필수 입력 항목입니다.")
    private String authcode;

    @NotBlank(message = "학교는 필수 입력 항목입니다.")
    private String school;
}
