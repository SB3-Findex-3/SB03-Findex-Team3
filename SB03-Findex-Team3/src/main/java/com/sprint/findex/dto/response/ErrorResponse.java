package com.sprint.findex.dto.response;

import com.sprint.findex.global.exception.Errors;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Schema(description = "에러 응답")
public class ErrorResponse {

    @Schema(description = "에러 발생 시간", example = "2025-03-06T05:39:06.152068Z")
    private ZonedDateTime timestamp;

    @Schema(description = "HTTP 상태 코드", example = "400")
    private Integer status;

    @Schema(description = "에러 메시지", example = "잘못된 요청입니다.")
    private String message;

    @Schema(description = "에러 상세 내용", example = "부서 코드는 필수입니다.")
    private String details;

    // timestamp를 현재 시간으로 자동 설정하는 생성자
    public ErrorResponse(Integer status, String message, String details) {
        this.timestamp = ZonedDateTime.now();
        this.status = status;
        this.message = message;
        this.details = details;
    }

    // 간단한 에러 응답용
    public ErrorResponse(Integer status, String message) {
        this.timestamp = ZonedDateTime.now();
        this.status = status;
        this.message = message;
        this.details = null;
    }

    public static ErrorResponse of(Errors error) {
        return ErrorResponse.builder()
            .timestamp(ZonedDateTime.now())
            .status(error.getStatus())
            .message(error.getMessage())
            .details(error.getDetail())
            .build();
    }

}