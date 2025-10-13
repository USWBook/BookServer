// global/exception/GlobalExceptionHandler.java
package com.example.demo.global.exception;

import com.example.demo.domain.mail.exception.TooManyMailRequestException;
import com.example.demo.global.response.RsData;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.LazyInitializationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.converter.HttpMessageNotReadableException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final HttpServletRequest request;

    // 많은 요청이 들어올경우
    @ExceptionHandler(TooManyMailRequestException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS) //  429 상태 코드
    public RsData<String> handleTooManyMailRequestException(TooManyMailRequestException ex) {
        return RsData.of("429", ex.getMessage());
    }

    /**
     * @RequestBody에 데이터가 없을 때 발생하는 예외를 처리합니다.
     * 값이 하나도 없을떄 NULL일떄
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RsData<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("HttpMessageNotReadableException: {}", ex.getMessage());
        return RsData.of("400-1", "요청 본문(Request Body)이 비어있거나 형식이 잘못되었습니다.");
    }

    /**
     * @RequestParam에 데이터가 없을 때 발생하는 예외를 처리합니다.
     * 파라미터가 빠진경우
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RsData<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.error("MissingServletRequestParameterException: {}", ex.getMessage());
        // 어떤 파라미터가 누락되었는지 동적으로 메시지를 생성해줍니다.
        String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", ex.getParameterName());
        return RsData.of("400-2", message);
    }

    /**
     * @PathVariable 값의 타입이 일치하지 않을 경우 발생하는 예외를 처리합니다.
     * ex) /api/posts/{postId} 에서 postId가 UUID 형식이 아닐 경우
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400 에러로 응답 상태 코드 설정
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = String.format("'%s' 파라미터의 형식이 잘못되었습니다. 올바른 형식은 '%s' 입니다.",
                ex.getName(), Objects.requireNonNull(ex.getRequiredType()).getSimpleName());

        ErrorResponse errorResponse = new ErrorResponse("400", message);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 간단한 에러 응답 DTO
    public record ErrorResponse(String code, String message) {}

    /**
     * @CookieValue 어노테이션에서 required=true로 설정된 쿠키가 없을 때 발생하는 예외를 처리합니다.
     * 500 Internal Server Error 대신, 명확한 400 Bad Request를 응답합니다.
     */
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<RsData<Object>> handleMissingRequestCookieException(MissingRequestCookieException ex) {
        String cookieName = ex.getCookieName();
        String errorMessage = String.format("필수 쿠키 '%s'가 요청에 포함되지 않았습니다.", cookieName);

        RsData<Object> response = RsData.of("400", errorMessage);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Auth 예외
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<RsData<Void>> handleAuthException(AuthException e) {
        return ResponseEntity
                //.status(HttpStatus.CONFLICT)
                .status(e.getStatusCode())
                .body(RsData.of(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(CustomJwtException.class)
    public ResponseEntity<RsData<Void>> handleJwtException(CustomJwtException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(RsData.of(e.getCode(), e.getMessage()));
    }

    // 도메인 커스텀 예외
    @ExceptionHandler(BookException.class)
    public ResponseEntity<RsData<Void>> handleBookException(BookException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(RsData.of(e.getCode(), e.getMessage()));
    }

    // @Valid 검증 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(RsData.of("400", message.isBlank() ? "잘못된 요청입니다." : message));
    }

    // 인증 / 인가 예외
    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class})
    public ResponseEntity<RsData<Void>> handleAuthorizationExceptions(Exception e) {
        if (e instanceof AuthenticationException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(RsData.of("401", "로그인이 필요합니다."));
        }
        if (e instanceof AccessDeniedException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(RsData.of("403", "권한이 없습니다."));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(RsData.of("403", "접근이 제한되었습니다."));
    }

    @ExceptionHandler(EnumException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RsData<?> handleInvalidSemesterException(EnumException e) {
        log.warn("잘못된 이넘 값 요청: {}", e.getMessage());
        return RsData.of("400", e.getMessage());
    }

    // JPA 엔티티 조회 실패 (fallback)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<RsData<Void>> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(RsData.of("404", "해당 데이터를 찾을 수 없습니다."));
    }

    // NoResourceFoundException을 잡아서 404 응답을 반환
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RsData<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        //  log.warn("Static resource not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(RsData.of("404", "요청하신 리소스를 찾을 수 없습니다."));
    }

    // 서버 내부 오류
    @ExceptionHandler({LazyInitializationException.class, RuntimeException.class, Exception.class})
    public ResponseEntity<RsData<Void>> handleServerError(Exception e) {
        log.error("🔥 500 Internal Server Error", e);
        String method = request.getMethod();
        String path = request.getRequestURI();
        String paramJson = extractParams(request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RsData.of("500", "서버 오류가 발생했습니다."));
    }

    private String extractParams(HttpServletRequest req) {
        String contentType = req.getContentType() == null ? "" : req.getContentType();
        if (contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            String json = (String) req.getAttribute("cachedRequestBody");
            return (json != null) ? StringUtils.abbreviate(json, 300) : "(빈 JSON 바디)";
        }
        if ("GET".equalsIgnoreCase(req.getMethod())) {
            String qs = req.getQueryString();
            return (qs != null && !qs.isBlank()) ? qs : "(쿼리스트링 없음)";
        }
        Map<String, String[]> map = req.getParameterMap();
        if (!map.isEmpty()) {
            return map.entrySet().stream()
                    .map(e -> e.getKey() + ":" + String.join(",", e.getValue()))
                    .collect(Collectors.joining("\n"));
        }
        return "(파라미터 없음)";
    }
}
