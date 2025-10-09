// global/exception/GlobalExceptionHandler.java
package com.example.demo.global.exception;

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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final HttpServletRequest request;

    /**
     * @CookieValue 어노테이션에서 required=true로 설정된 쿠키가 없을 때 발생하는 예외를 처리합니다.
     * 500 Internal Server Error 대신, 명확한 400 Bad Request를 응답합니다.
     */
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<RsData<Object>> handleMissingRequestCookieException(MissingRequestCookieException ex) {
        String cookieName = ex.getCookieName();
        String errorMessage = String.format("필수 쿠키 '%s'가 요청에 포함되지 않았습니다.", cookieName);

        RsData<Object> response = new RsData<>("400", errorMessage);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Auth 예외
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<RsData<Void>> handleAuthException(AuthException e) {
        return ResponseEntity
                //.status(HttpStatus.CONFLICT)
                .status(e.getStatusCode())
                .body(new RsData<>(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(CustomJwtException.class)
    public ResponseEntity<RsData<Void>> handleJwtException(CustomJwtException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(new RsData<>(e.getCode(), e.getMessage()));
    }

    // 도메인 커스텀 예외
    @ExceptionHandler(BookException.class)
    public ResponseEntity<RsData<Void>> handleBookException(BookException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(new RsData<>(e.getCode(), e.getMessage()));
    }

    // @Valid 검증 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RsData<>("400", message.isBlank() ? "잘못된 요청입니다." : message));
    }

    // 인증 / 인가 예외
    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class})
    public ResponseEntity<RsData<Void>> handleAuthorizationExceptions(Exception e) {
        if (e instanceof AuthenticationException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RsData<>("401", "로그인이 필요합니다."));
        }
        if (e instanceof AccessDeniedException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new RsData<>("403", "권한이 없습니다."));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new RsData<>("403", "접근이 제한되었습니다."));
    }

    // JPA 엔티티 조회 실패 (fallback)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<RsData<Void>> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new RsData<>("404", "해당 데이터를 찾을 수 없습니다."));
    }

    // NoResourceFoundException을 잡아서 404 응답을 반환
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RsData<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        //  log.warn("Static resource not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new RsData<>("404", "요청하신 리소스를 찾을 수 없습니다."));
    }

    // 서버 내부 오류
    @ExceptionHandler({LazyInitializationException.class, RuntimeException.class, Exception.class})
    public ResponseEntity<RsData<Void>> handleServerError(Exception e) {
        log.error("🔥 500 Internal Server Error", e);
        String method = request.getMethod();
        String path = request.getRequestURI();
        String paramJson = extractParams(request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RsData<>("500", "서버 오류가 발생했습니다."));
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
