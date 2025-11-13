package com.example.community.config;

import com.example.community.common.ErrorCode;
import com.example.community.common.doc.ApiFail;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("커뮤니티 API")
                        .description("""
                                모든 응답은 isSuccess / code / message / result 구조를 따릅니다.
                                실패 응답은 전역으로 400/401/403/404/500을 정의하고,
                                엔드포인트별 특수 코드(예: 409)나 커스텀 예시는 개별 API에만 둡니다.
                                """)
                        .version("1.0.0"))
                .servers(List.of(new Server().url("http://localhost:8080").description("로컬")));
    }

    @Bean
    public GroupedOpenApi allApi(OpenApiCustomizer globalErrorCustomizer) {
        return GroupedOpenApi.builder()
                .group("all")
                .packagesToScan("com.example.community.controller")
                .pathsToMatch("/**")
                .addOpenApiCustomizer(globalErrorCustomizer)
                .build();
    }

    @Bean
    public OpenApiCustomizer globalErrorCustomizer() {
        return openApi -> {
            // 1) ApiFail 스키마 등록
            Map<String, Schema> models = ModelConverters.getInstance().read(ApiFail.class);
            Schema<?> apiFailSchema = models.get("ApiFail");
            if (apiFailSchema != null) {
                Components comps = openApi.getComponents();
                if (comps == null) {
                    comps = new Components();
                    openApi.setComponents(comps);
                }
                comps.addSchemas("ApiFail", apiFailSchema);
            }

            if (openApi.getPaths() == null) return;

            // 2) 공통 실패 응답 추가
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(op -> {
                        ApiResponses res = op.getResponses();
                        addIfAbsent(res, "400", "검증 실패", ErrorCode.BAD_REQUEST);
                        addIfAbsent(res, "401", "인증 필요",  ErrorCode.UNAUTHORIZED);
                        addIfAbsent(res, "403", "권한 없음",  ErrorCode.FORBIDDEN);
                        addIfAbsent(res, "404", "리소스 없음", ErrorCode.MEMBER_NOT_FOUND); // 프로젝트에서 쓰는 404 코드
                        addIfAbsent(res, "500", "서버 오류",  ErrorCode.SERVER_ERROR);
                    })
            );
        };
    }


    // 해당 상태코드가 없을 때만 ApiFail 스키마 + 올바른 예시(JSON)로 응답을 추가
    private void addIfAbsent(ApiResponses res, String statusCode, String description, ErrorCode ec) {
        if (res.get(statusCode) != null) return;

        // 스키마는 $ref로, 예시는 각 코드/메시지에 맞춰 지정
        Schema<?> ref = new Schema<>().$ref("#/components/schemas/ApiFail");
        MediaType mt = new MediaType().schema(ref)
                .example(exampleJson(ec)); // <-- 여기서 예시를 정확히 지정

        Content content = new Content().addMediaType("application/json", mt);
        ApiResponse apiRes = new ApiResponse().description(description).content(content);
        res.addApiResponse(statusCode, apiRes);
    }

    // ErrorCode 기반 JSON 생성
    private String exampleJson(ErrorCode ec) {
        // isSuccess=false, code는 ErrorCode의 status, message는 ErrorCode의 message, result=null
        return """
            {
              "isSuccess": false,
              "code": %d,
              "message": "%s",
              "result": null
            }
            """.formatted(ec.getStatus(), escape(ec.getMessage()));
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
