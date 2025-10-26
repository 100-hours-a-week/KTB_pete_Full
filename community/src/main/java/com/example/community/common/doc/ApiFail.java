package com.example.community.common.doc;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiFail", description = "실패 응답")
public class ApiFail {
    @Schema(example = "false")
    public boolean isSuccess;

    @Schema(example = "400")
    public int code;

    @Schema(example = "title: must not be blank")
    public String message;

    @Schema(nullable = true, example = "null")
    public Object result;
}
