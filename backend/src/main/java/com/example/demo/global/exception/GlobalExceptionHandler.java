// global/exception/GlobalExceptionHandler.java
package com.example.demo.global.exception;

import com.example.demo.domain.mail.exception.TooManyMailRequestException;
import com.example.demo.global.response.RsData;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =================================================================================================================
    //  4xx Client Error
    // =================================================================================================================

    /**
     * @Valid, @Validated 에 의한 유효성 검증 실패 시 (主に @RequestBody, @ModelAttribute)
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RsData<?> handleBindException(BindException ex) {
        // 여러 에러 중 첫 번째 에러 정보를 가져옴
        FieldError firstError = ex.getBindingResult().getFieldError();
        if (firstError == null) {
            return RsData.of("400-1", "입력값이 유효하지 않습니다.");
        }

        String errorMessage;
        //  에러 코드 중에 "typeMismatch"가 포함되어 있는지 확인
        if (Arrays.asList(Objects.requireNonNull(firstError.getCodes())).contains("typeMismatch")) {
            //  타입 변환 실패 에러인 경우, 직접 만든 사용자 친화적인 메시지 사용
            errorMessage = String.format("'%s' 필드에 유효하지 않은 형식의 값이 입력되었습니다.", firstError.getField());
        } else {
            //  그 외 @NotNull, @Email 등의 유효성 검증 에러인 경우, 어노테이션에 정의된 메시지 사용
            errorMessage = firstError.getDefaultMessage();
        }

        return RsData.of("400-1", errorMessage);
    }

    /**
     * @RequestParam 필수 파라미터 누락 시
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RsData<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", ex.getParameterName());
        return RsData.of("400-2", message);
    }

    /**
     * @RequestBody JSON 파싱 실패 또는 필수 본문 누락 시
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RsData<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return RsData.of("400-3", "요청 본문(Request Body)의 형식이 잘못되었거나 비어있습니다.");
    }

    /**
     * @PathVariable, @RequestParam 등 타입 변환 실패 시
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RsData<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = String.format("'%s' 파라미터의 타입이 잘못되었습니다. '%s' 타입이어야 합니다.",
                ex.getName(), Objects.requireNonNull(ex.getRequiredType()).getSimpleName());
        return RsData.of("400-4", message);
    }

    /**
     * 필수 쿠키 누락 시
     */
    @ExceptionHandler(MissingRequestCookieException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RsData<?> handleMissingRequestCookieException(MissingRequestCookieException ex) {
        String errorMessage = String.format("필수 쿠키 '%s'가 요청에 포함되지 않았습니다.", ex.getCookieName());
        return RsData.of("400-5", errorMessage);
    }

    /**
     * 커스텀 Enum 유효성 검증 실패 시
     */
    @ExceptionHandler(EnumException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RsData<?> handleEnumException(EnumException e) {
        return RsData.of("400-6", e.getMessage());
    }

    /**
     * 너무 잦은 요청 시 (커스텀 예외)
     */
    @ExceptionHandler(TooManyMailRequestException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public RsData<?> handleTooManyMailRequestException(TooManyMailRequestException ex) {
        return RsData.of("429", ex.getMessage());
    }

    /**
     * 인증 실패 (로그인 필요)
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public RsData<?> handleAuthenticationException(AuthenticationException e) {
        return RsData.of("401", "인증에 실패했습니다. 로그인이 필요합니다.");
    }

    /**
     * 인가 실패 (권한 없음)
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public RsData<?> handleAccessDeniedException(AccessDeniedException e) {
        return RsData.of("403", "접근 권한이 없습니다.");
    }

    /**
     * JPA 엔티티 조회 실패
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RsData<?> handleEntityNotFoundException(EntityNotFoundException e) {
        return RsData.of("404", "해당 데이터를 찾을 수 없습니다.");
    }

    // =================================================================================================================
    //  동적 상태 코드 처리가 필요한 커스텀 예외 (ResponseEntity 유지)
    // =================================================================================================================

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<RsData<?>> handleAuthException(AuthException e) {
        return ResponseEntity.status(e.getStatusCode()).body(RsData.of(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(CustomJwtException.class)
    public ResponseEntity<RsData<?>> handleJwtException(CustomJwtException e) {
        return ResponseEntity.status(e.getStatusCode()).body(RsData.of(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(BookException.class)
    public ResponseEntity<RsData<?>> handleBookException(BookException e) {
        return ResponseEntity.status(e.getStatusCode()).body(RsData.of(e.getCode(), e.getMessage()));
    }

    // =================================================================================================================
    // ⭐️ 5xx Server Error
    // =================================================================================================================

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RsData<?> handleAllUncaughtException(Exception e, HttpServletRequest request) { // 필요한 곳에서만 request를 파라미터로 받음
        log.error("🔥 500 Internal Server Error 발생: {}", e.getMessage(), e);
        log.error("Request: {} {}", request.getMethod(), request.getRequestURI()); // 요청 정보 로깅
        return RsData.of("500", "서버 내부 오류가 발생했습니다.");
    }
}