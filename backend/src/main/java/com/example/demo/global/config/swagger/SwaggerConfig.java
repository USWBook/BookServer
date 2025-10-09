package com.example.demo.global.config.swagger;

import com.example.demo.global.annotation.swagger.*;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.global.response.RsData;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // 1. API 문서의 기본 정보 설정
        Info info = new Info()
                .title("USW-Book API Documentation")
                .version("v0.0.1")
                .description("수원대학교 중고 서적 거래 플랫폼 API 명세서입니다.");

        // 2. JWT 인증 스키마 설정
        // "Authorization" 헤더에 "Bearer {token}" 형식으로 토큰을 전달해야 함을 명시
        String jwtSchemeName = "jwtAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")
                        .bearerFormat("JWT")); // 토큰 형식은 JWT

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    // 스웨거 어노테이션 커스텀
    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            ApiSuccessResponse successResponse = handlerMethod.getMethodAnnotation(ApiSuccessResponse.class);
            ApiErrorResponses errorResponses = handlerMethod.getMethodAnnotation(ApiErrorResponses.class);

            if (successResponse != null) {
                handleSuccessResponse(operation, successResponse);
            }
            if (errorResponses != null) {
                Arrays.stream(errorResponses.value())
                        .forEach(error -> handleErrorResponse(operation, error));
            }

            ApiUnauthorizedResponse unauthorized = handlerMethod.getMethodAnnotation(ApiUnauthorizedResponse.class);
            ApiForbiddenResponse forbidden = handlerMethod.getMethodAnnotation(ApiForbiddenResponse.class);

            if (unauthorized != null) {
                handleUnauthorizedResponse(operation);
            }
            if (forbidden != null) {
                handleForbiddenResponse(operation);
            }
            return operation;
        };
    }

    private void handleSuccessResponse(Operation operation, ApiSuccessResponse successInfo) {
        String responseCode = successInfo.responseCode();
        ApiResponses responses = operation.getResponses();
        ApiResponse apiResponse = responses.computeIfAbsent(responseCode, key -> new ApiResponse());
        apiResponse.setDescription(successInfo.description());

        // 새로운 스키마를 처음부터 직접 생성합니다.
        Schema<?> newSchema = new Schema<>();
        newSchema.addProperty("code", new Schema<String>().type("string").example(responseCode));
        newSchema.addProperty("message", new Schema<String>().type("string").example(successInfo.message()));

        Class<?> dataType = successInfo.dataType();
        if (dataType != Void.class) {
            Schema<?> dataSchema;

                // UUID 같은 기본 타입인지 확인
                if (dataType == UUID.class) {
                    dataSchema = new Schema<>().type("string").format("uuid");
                } else {
                    dataSchema = new Schema<>().$ref("#/components/schemas/" + dataType.getSimpleName());
                }

            newSchema.addProperty("data", dataSchema);
        }

        apiResponse.setContent(new Content().addMediaType("application/json", new MediaType().schema(newSchema)));
    }


    private void handleErrorResponse(Operation operation, ApiErrorResponse error) {
        ApiResponses responses = operation.getResponses();
        String responseCode = error.responseCode();

        Example example = new Example().value(error.exampleValue());
        MediaType mediaType = new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/RsData"))
                .addExamples(error.exampleName(), example);

        ApiResponse apiResponse = responses.computeIfAbsent(responseCode, key -> new ApiResponse());
        apiResponse.setDescription((apiResponse.getDescription() == null ? "" : apiResponse.getDescription() + "<br>") + error.description());
        apiResponse.setContent(new Content().addMediaType("application/json", mediaType));
    }

    private void handleUnauthorizedResponse(Operation operation) {
        ApiResponses responses = operation.getResponses();

        ApiResponse apiResponse = new ApiResponse()
                .description("인증 실패 (로그인 필요)")
                .content(new Content().addMediaType("application/json",
                        new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/RsData"))
                                .example("{\"code\": \"401\", \"message\": \"로그인 해야합니다.\", \"data\": null}")
                ));

        responses.addApiResponse("401", apiResponse);
    }

    private void handleForbiddenResponse(Operation operation) {
        ApiResponses responses = operation.getResponses();

        ApiResponse apiResponse = new ApiResponse()
                .description("접근 권한 없음")
                .content(new Content().addMediaType("application/json",
                        new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/RsData"))
                                .example("{\"code\": \"403\", \"message\": \"접근 권한이 없습니다.\", \"data\": null}")
                ));

        responses.addApiResponse("403", apiResponse);
    }
}
