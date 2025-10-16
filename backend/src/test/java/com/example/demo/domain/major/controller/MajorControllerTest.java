package com.example.demo.domain.major.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 후 DB 변경사항을 롤백하기 위해 추가
@ActiveProfiles("test") //  테스트용 DB 설정을 사용하기 위해 추가
public class MajorControllerTest {

    @Autowired private MockMvc mockMvc;

    //  @MockBean으로 만들었던 MajorService대신 실제 Service Bean을 사용

    @Test
    @DisplayName("모든 전공 목록 조회 API 테스트")
    public void getAllMajorsApiTest() throws Exception {
        // given
        // given 절은 필요 없다.
        // @SpringBootTest는 InitTestData에 의해 실제 DB(H2)에 데이터가 들어있는 상태에서 시작

        // when & then
        mockMvc.perform(get("/api/major/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("전공 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                // InitTestData에 의해 생성된 실제 데이터로 검증합니다.
                // 예를 들어, InitTestData가 "컴퓨터공학과"를 생성했다면 아래와 같이 검증합니다.
                .andExpect(jsonPath("$.data[0].name").value("컴퓨터공학과"))
                .andDo(print());
    }
}