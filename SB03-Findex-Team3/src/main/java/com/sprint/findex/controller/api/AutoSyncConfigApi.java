package com.sprint.findex.controller.api;

import com.sprint.findex.dto.request.AutoSyncQueryParams;
import com.sprint.findex.dto.request.AutoSyncConfigUpdateRequest;
import com.sprint.findex.dto.response.AutoSyncConfigDto;
import com.sprint.findex.dto.response.cursor.CursorPageResponseAutoSyncConfigDto;
import com.sprint.findex.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "자동 연동 설정 관리", description = "자동 연동 설정 관리 API")
public interface AutoSyncConfigApi {

    @Operation(
        summary = "자동 연동 설정 목록 조회",
        description = "자동 연동 설정 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "자동 연동 설정 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = CursorPageResponseAutoSyncConfigDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 설정값)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    CursorPageResponseAutoSyncConfigDto findByCursor(
        @Parameter(description = "자동 연동 설정 조회 매개변수")
        @ModelAttribute AutoSyncQueryParams params
    );

    @Operation(
        summary = "자동 연동 설정 수정",
        description = "기존 자동 연동 설정을 수정합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "자동 연동 설정 수정 성공",
            content = @Content(schema = @Schema(implementation = AutoSyncConfigDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 설정값)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "수정할 자동 연동 설정을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    AutoSyncConfigDto updateEnabled(
        @Parameter(description = "자동 연동 설정 ID", required = true)
        @PathVariable Long id,
        @Parameter(description = "자동 연동 설정 수정 요청", required = true)
        @Valid @RequestBody AutoSyncConfigUpdateRequest request
    );
}
