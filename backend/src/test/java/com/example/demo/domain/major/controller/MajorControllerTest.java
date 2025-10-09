package com.example.demo.domain.major.controller;

import com.example.demo.domain.major.response.MajorResponse;
import com.example.demo.domain.major.service.MajorService;
import com.example.demo.global.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 시큐리티콘피그는 제외하도록 하여 의존성주입 안받도록함
@WebMvcTest(controllers = MajorController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
        })
public class MajorControllerTest {

    @Autowired
    private MockMvc mockMvc; // HTTP 요청을 시뮬레이션하는 객체

    @Autowired
    private ObjectMapper objectMapper; // 객체를 JSON으로 변환

    @MockBean
    private MajorService majorService;

    @Test
    @DisplayName("모든 전공 목록 조회 API 테스트")
    public void getAllMajorsApiTest() throws Exception {
        // given
        UUID uuid = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        List<MajorResponse> mockResponse = Arrays.asList(
                new MajorResponse(uuid, "컴퓨터학부"),
                new MajorResponse(uuid2, "소프트웨어학부")
        );
        given(majorService.getAllMajors()).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/major/list"))
                .andExpect(status().isOk()) // 상태 코드가 200인지 검증
                .andExpect(jsonPath("$.code").value("200")) // 응답 JSON의 code 필드 값 검증
                .andExpect(jsonPath("$.message").value("전체 학과 조회 성공"))
                .andExpect(jsonPath("$.data").isArray()) // data 필드가 배열인지 검증
                .andExpect(jsonPath("$.data[0].name").value("컴퓨터학부"))
                .andExpect(jsonPath("$.data[1].name").value("소프트웨어학부"))
                .andDo(print()); // 요청/응답 전체 내용 출력
    }
}

