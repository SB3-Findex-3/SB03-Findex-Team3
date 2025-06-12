package com.sprint.findex.controller.api;

import com.sprint.findex.dto.request.IndexDataSyncRequest;
import com.sprint.findex.dto.response.cursor.CursorPageResponseSyncJobDto;
import com.sprint.findex.dto.response.SyncJobDto;
import com.sprint.findex.entity.SyncJobResult;
import com.sprint.findex.entity.SyncJobType;
import com.sprint.findex.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Tag(name = "연동 작업 API", description = "연동 작업 관리 API")
@RequestMapping("/api/sync-jobs")
public interface SyncJobApi {

    @Operation(
        summary = "지수 정보 연동",
        description = "Open API를 통해 지수 정보를 연동합니다.",
        responses = {
            @ApiResponse(responseCode = "202", description = "연동 작업 생성 성공",
                content = @Content(schema = @Schema(implementation = SyncJobDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    @PostMapping("/index-infos")
    ResponseEntity<List<SyncJobDto>> syncIndexInfoAsync(HttpServletRequest httpRequest);

    @Operation(
        summary = "지수 데이터 연동",
        description = "Open API를 통해 지수 데이터를 연동합니다.",
        requestBody = @RequestBody(
            description = "지수 데이터 연동 요청",
            required = true,
            content = @Content(schema = @Schema(implementation = IndexDataSyncRequest.class))
        ),
        responses = {
            @ApiResponse(responseCode = "202", description = "연동 작업 생성 성공",
                content = @Content(schema = @Schema(implementation = SyncJobDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "지수 정보를 찾을 수 없음",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    @PostMapping("/index-data")
    ResponseEntity<List<SyncJobDto>> syncIndexInfos(
        @org.springframework.web.bind.annotation.RequestBody IndexDataSyncRequest request,
        HttpServletRequest httpRequest
    );

    @Operation(
        summary = "연동 작업 목록 조회",
        description = "연동 작업 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                content = @Content(schema = @Schema(implementation = CursorPageResponseSyncJobDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    @GetMapping
    ResponseEntity<Object> getSyncJobList(
        @Parameter(description = "작업 유형", example = "INDEX_INFO")
        @RequestParam(required = false) SyncJobType jobType,

        @Parameter(description = "지수 ID", example = "1")
        @RequestParam(required = false) Long indexInfoId,

        @Parameter(description = "시작 날짜", example = "2023-01-01")
        @RequestParam(required = false) LocalDate baseDateFrom,

        @Parameter(description = "종료 날짜", example = "2023-01-31")
        @RequestParam(required = false) LocalDate baseDateTo,

        @Parameter(description = "작업자 IP", example = "192.168.0.1")
        @RequestParam(required = false) String worker,

        @Parameter(description = "작업 시간 시작", example = "2023-01-01T00:00:00Z")
        @RequestParam(required = false) OffsetDateTime jobTimeFrom,

        @Parameter(description = "작업 시간 종료", example = "2023-01-31T23:59:59Z")
        @RequestParam(required = false) OffsetDateTime jobTimeTo,

        @Parameter(description = "작업 결과 상태", example = "SUCCESS")
        @RequestParam(required = false) SyncJobResult status,

        @Parameter(description = "커서 ID 이후", example = "101")
        @RequestParam(required = false) String idAfter,

        @Parameter(description = "커서", example = "abc123")
        @RequestParam(required = false) String cursor,

        @Parameter(description = "정렬 필드", example = "jobTime")
        @RequestParam(defaultValue = "jobTime", required = false) String sortField,

        @Parameter(description = "정렬 방향", example = "desc")
        @RequestParam(defaultValue = "desc", required = false) String sortDirection,

        @Parameter(description = "페이지 크기", example = "10")
        @RequestParam(defaultValue = "10", required = false) int size
    );
}
