package com.sprint.findex.controller.api;


import com.sprint.findex.dto.dashboard.IndexChartDto;
import com.sprint.findex.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex.dto.dashboard.RankedIndexPerformanceDto;
import com.sprint.findex.entity.Period;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
public interface IndexDataApi {

    @Operation(summary = "지수 차트 조회", description = "지수의 차트 데이터를 조회합니다.", responses = {
        @ApiResponse(responseCode = "200", description = "차트 데이터 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 기간 유형 등)"),
        @ApiResponse(responseCode = "404", description = "지수 정보를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")})
    ResponseEntity<IndexChartDto> getChartData(
        @PathVariable("id") Long indexInfoId,
        @RequestParam(value = "periodType", defaultValue = "DAILY") Period period);

    @Operation(summary = "지수 성과 랭킹 조회", description = "지수의 성과 분석 랭킹을 조회합니다.", responses = {
        @ApiResponse(responseCode = "200", description = "성과 랭킹 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않은 기간 유형 등)"),
        @ApiResponse(responseCode = "500", description = "서버 오류")})
    ResponseEntity<List<RankedIndexPerformanceDto>> getPerformanceRank(
        @RequestParam(required = false) Long indexInfoId,
        @RequestParam(value = "periodType", defaultValue = "DAILY") Period period,
        @RequestParam(defaultValue = "10") int limit
    );

    @Operation(summary = "관심 지수 성과 조회", description = "즐겨찾기로 등록된 지수들의 성과를 조회합니다.", responses = {
        @ApiResponse(responseCode = "200", description = "관심 지수 성과 조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")})
    ResponseEntity<List<IndexPerformanceDto>> getFavoriteIndexPerformances(
        @RequestParam(value = "periodType", defaultValue = "DAILY") Period period);

}
