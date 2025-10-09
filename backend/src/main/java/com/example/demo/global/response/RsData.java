package com.example.demo.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "RsData", description = "공통 응답 포맷")
public class RsData<T> {
  @Schema(description = "결과 코드", example = "200")
  private String code;
  @Schema(description = "결과 메시지", example = "요청에 성공했습니다.")
  private String message;
  private T data;

  public static <T> RsData<T> of(String code, String message) {
    return new RsData<>(code, message, null);
  }

  public static <T> RsData<T> of(String code, String message, T data) {
    return new RsData<>(code, message, data);
  }

  @JsonIgnore
  public int getStatusCode() {
    return Integer.parseInt(code);
  }
}
