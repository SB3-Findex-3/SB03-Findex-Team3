package com.sprint.findex.controller.api;

import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.request.IndexDataQueryParams;
import com.sprint.findex.dto.request.IndexDataUpdateRequest;
import com.sprint.findex.dto.response.cursor.CursorPageResponseIndexData;
import com.sprint.findex.dto.response.IndexDataDto;
import com.sprint.findex.dto.response.ErrorResponse;
import com.sprint.findex.dto.dashboard.IndexChartDto;
import com.sprint.findex.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex.dto.dashboard.RankedIndexPerformanceDto;
import com.sprint.findex.entity.Period;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
public interface IndexDataApi {

    @Operation(
        summary = "지수 데이터 목록 조회",
        description = "지수 데이터 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "지수 데이터 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = CursorPageResponseIndexData.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 필터 값 등)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    ResponseEntity<CursorPageResponseIndexData<IndexDataDto>> findByCursor(
        @Parameter(description = "지수 데이터 조회 매개변수") IndexDataQueryParams params
    );

    @Operation(
        summary = "지수 데이터 등록",
        description = "새로운 지수 데이터를 등록합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "지수 데이터 생성 성공",
            content = @Content(schema = @Schema(implementation = IndexDataDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 데이터 값 등)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "참조하는 지수 정보를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    ResponseEntity<IndexDataDto> create(
        @Parameter(description = "지수 데이터 생성 요청", required = true)
        @RequestBody IndexDataCreateRequest request
    );

    @Operation(
        summary = "지수 데이터 수정",
        description = "기존 지수 데이터를 수정합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "지수 데이터 수정 성공",
            content = @Content(schema = @Schema(implementation = IndexDataDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 데이터 값 등)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "수정할 지수 데이터를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    ResponseEntity<IndexDataDto> update(
        @Parameter(description = "지수 데이터 ID", required = true) @PathVariable Long id,
        @Parameter(description = "지수 데이터 수정 요청", required = true)
        @RequestBody IndexDataUpdateRequest request
    );

    @Operation(
        summary = "지수 데이터 삭제",
        description = "지수 데이터를 삭제합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "지수 데이터 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "삭제할 지수 데이터를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
        @Parameter(description = "지수 데이터 ID", required = true) @PathVariable Long id
    );

    @Operation(
        summary = "지수 데이터 CSV export",
        description = "지수 데이터를 CSV 파일로 export합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "CSV 파일 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 필터 값 등)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/export/csv")
    ResponseEntity<byte[]> exportCsv(
        @Parameter(description = "CSV export용 지수 데이터 조회 매개변수") IndexDataQueryParams params
    );

    @Operation(
        summary = "지수 차트 조회",
        description = "지수의 차트 데이터를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "차트 데이터 조회 성공",
            content = @Content(schema = @Schema(implementation = IndexChartDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 기간 유형 등)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "지수 정보를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/chart")
    ResponseEntity<IndexChartDto> getChartData(
        @Parameter(description = "지수 정보 ID", required = true) @PathVariable("id") Long indexInfoId,
        @Parameter(description = "기간 유형", example = "DAILY")
        @RequestParam(value = "period", defaultValue = "DAILY") Period period
    );

    @Operation(
        summary = "지수 성과 랭킹 조회",
        description = "지수의 성과 분석 랭킹을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성과 랭킹 조회 성공",
            content = @Content(schema = @Schema(implementation = RankedIndexPerformanceDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 기간 유형 등)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/performance/rank")
    ResponseEntity<List<RankedIndexPerformanceDto>> getPerformanceRank(
        @Parameter(description = "지수 정보 ID") @RequestParam(required = false) Long indexInfoId,
        @Parameter(description = "기간 유형", example = "DAILY")
        @RequestParam(value = "period", defaultValue = "DAILY") Period period,
        @Parameter(description = "조회 개수", example = "10")
        @RequestParam(defaultValue = "10") int limit
    );

    @Operation(
        summary = "관심 지수 성과 조회",
        description = "즐겨찾기로 등록된 지수들의 성과를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "관심 지수 성과 조회 성공",
            content = @Content(schema = @Schema(implementation = IndexPerformanceDto.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/performance/favorite")
    ResponseEntity<List<IndexPerformanceDto>> getFavoriteIndexPerformances(
        @Parameter(description = "기간 유형", example = "DAILY")
        @RequestParam(value = "period", defaultValue = "DAILY") Period period
    );
}

