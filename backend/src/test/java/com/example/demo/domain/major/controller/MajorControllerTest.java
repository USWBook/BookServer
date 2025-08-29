package com.example.demo.domain.major.controller;

import com.example.demo.domain.major.response.MajorResponse;
import com.example.demo.domain.major.service.MajorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(MajorController.class) // MajorController를 테스트 대상으로 지정
public class MajorControllerTest {

    @Autowired
    private MockMvc mockMvc; // HTTP 요청을 시뮬레이션하는 객체

    @Autowired
    private ObjectMapper objectMapper; // 객체를 JSON으로 변환

    @MockBean // 가짜(Mock) Service 빈을 스프링 컨텍스트에 등록
    private MajorService majorService;

    @Test
    @DisplayName("모든 전공 목록 조회 API 테스트")
    public void getAllMajorsApiTest() throws Exception {
        // given
        List<MajorResponse> mockResponse = Arrays.asList(
                new MajorResponse( "컴퓨터학부"),
                new MajorResponse( "소프트웨어학부")
        );
        given(majorService.getAllMajors()).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/major/all"))
                .andExpect(status().isOk()) // 상태 코드가 200인지 검증
                .andExpect(jsonPath("$.code").value("200")) // 응답 JSON의 code 필드 값 검증
                .andExpect(jsonPath("$.message").value("전체 학과 조회 성공"))
                .andExpect(jsonPath("$.data").isArray()) // data 필드가 배열인지 검증
                .andExpect(jsonPath("$.data[0].majorName").value("컴퓨터학부"))
                .andExpect(jsonPath("$.data[1].majorName").value("소프트웨어학부"))
                .andDo(print()); // 요청/응답 전체 내용 출력
    }
}