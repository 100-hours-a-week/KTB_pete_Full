package com.example.community.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    // 전역 OpenAPI 메타 + 안내문
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("커뮤니티 API")
                        .description("""
                                모든 응답은 isSuccess / code / message / result 구조를 따릅니다.
                                실패 응답은 공통으로 400 / 401 / 404 / 500 이 노출되며,
                                API별 409 등은 해당 엔드포인트에서 별도로 명시합니다.
                                """)
                        .version("1.0.0"))
                .servers(List.of(new Server().url("http://localhost:8080").description("로컬")));
    }

    // 한 화면 통합: 모든 컨트롤러를 하나의 문서로 스캔
    @Bean
    public GroupedOpenApi allApi(OpenApiCustomizer globalErrorCustomizer) {
        return GroupedOpenApi.builder()
                .group("all")
                .packagesToScan("com.example.community.controller")
                .pathsToMatch("/**")
                // 전역 실패 응답 커스터마이저를 이 문서에 적용
                .addOpenApiCustomizer(globalErrorCustomizer)
                .build();
    }

    // 공통 실패 응답(400/401/404/500) 자동 부착 (인라인 스키마)
    @Bean
    public OpenApiCustomizer globalErrorCustomizer() {
        return openApi -> {
            Schema<?> failSchema = new ObjectSchema()
                    .addProperty("isSuccess", new BooleanSchema()._default(false).example(false))
                    .addProperty("code", new IntegerSchema().example(400))
                    .addProperty("message", new StringSchema().example("잘못된 요청입니다."))
                    // result는 제네릭이므로 구체 타입 고정하지 않음
                    .addProperty("result", new Schema<>().nullable(true).example(null));

            Content jsonFail = new Content().addMediaType(
                    "application/json",
                    new MediaType().schema(failSchema)
            );

            if (openApi.getPaths() == null) return;

            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(op -> {
                        ApiResponses res = op.getResponses();
                        res.addApiResponse("400", new ApiResponse().description("잘못된 요청").content(jsonFail));
                        res.addApiResponse("401", new ApiResponse().description("인증 필요").content(jsonFail));
                        res.addApiResponse("404", new ApiResponse().description("리소스 없음").content(jsonFail));
                        res.addApiResponse("500", new ApiResponse().description("서버 오류").content(jsonFail));
                    })
            );
        };
    }
}
