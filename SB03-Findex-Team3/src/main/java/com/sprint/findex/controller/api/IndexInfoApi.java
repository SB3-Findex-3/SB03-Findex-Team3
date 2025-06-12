package com.sprint.findex.controller.api;

import com.sprint.findex.dto.request.IndexInfoCreateRequest;
import com.sprint.findex.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex.dto.response.cursor.CursorPageResponseIndexInfoDto;
import com.sprint.findex.dto.response.ErrorResponse;
import com.sprint.findex.dto.response.IndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "지수 정보 API", description = "지수 정보 관리 API")
public interface IndexInfoApi {

    @Operation(summary = "지수 정보 등록", description = "새로운 지수 정보를 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "지수 정보 생성 성공",
            content = @Content(schema = @Schema(implementation = IndexInfoDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수값 누락 등)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<IndexInfoDto> createIndexInfo(
        @Parameter(description = "지수 정보 생성 요청", required = true)
        @Valid IndexInfoCreateRequest request
    );

    @Operation(summary = "지수 정보 수정", description = "기존 지수 정보를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "지수 정보 수정 성공",
            content = @Content(schema = @Schema(implementation = IndexInfoDto.class))),
        @ApiResponse(responseCode = "404", description = "수정할 지수 정보가 존재하지 않음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<IndexInfoDto> updateIndexInfo(
        @Parameter(description = "지수 정보 ID", required = true) Long id,
        @Parameter(description = "지수 정보 수정 요청", required = true)
        @Valid IndexInfoUpdateRequest request
    );

    @Operation(summary = "지수 정보 삭제", description = "지수 정보를 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "지수 정보 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "삭제할 지수 정보가 존재하지 않음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> deleteIndexInfo(
        @Parameter(description = "지수 정보 ID", required = true) Long id
    );

    @Operation(summary = "지수 정보 조회", description =  "ID로 지수 정보를 조회합니다.")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "지수 정보 조회 성공",
                content = @Content(schema = @Schema(implementation = IndexInfoDto.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "조회할 지수 정보를 찾을 수 없음",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    ResponseEntity<Object> getIndexInfo(
        @Parameter(description = "지수 정보 ID", required = true) Long id
    );

    @Operation(
        summary = "지수 정보 목록 조회",
        description = "지수 정보 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "지수 정보 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = CursorPageResponseIndexInfoDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 필터 값 등)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    ResponseEntity<Object> getIndexInfoList(
        @Parameter(description = "지수 분류명", required = false) String indexClassification,
        @Parameter(description = "지수명", required = false) String indexName,
        @Parameter(description = "즐겨찾기 여부", required = false) Boolean favorite,
        @Parameter(description = "이전 페이지 마지막 요소 ID", required = false) String idAfter,
        @Parameter(description = "커서 (다음 페이지 시작점)", required = false) String cursor,
        @Parameter(description = "정렬 필드 (indexClassification, indexName, employedItemsCount)", required = false) String sortField,
        @Parameter(description = "정렬 방향 (asc, desc)", required = false) String sortDirection,
        @Parameter(description = "페이지 크기", required = false) int size
    );

    @Operation(
        summary = "지수 정보 요약 목록 조회",
        description = "지수 ID, 분류, 이름만 포함한 전체 지수 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "지수 정보 요약 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = IndexInfoSummaryDto.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    ResponseEntity<Object> getIndexInfoSummaries();

}
