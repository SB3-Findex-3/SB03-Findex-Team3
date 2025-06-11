package com.sprint.findex.controller;

import com.sprint.findex.dto.dashboard.IndexChartDto;
import com.sprint.findex.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex.dto.dashboard.RankedIndexPerformanceDto;
import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.request.IndexDataQueryParams;
import com.sprint.findex.dto.request.IndexDataUpdateRequest;
import com.sprint.findex.dto.response.CursorPageResponseIndexData;
import com.sprint.findex.dto.response.IndexDataCsvExporter;
import com.sprint.findex.dto.response.IndexDataDto;
import com.sprint.findex.entity.Period;
import com.sprint.findex.repository.IndexDataRepository;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.service.IndexDataService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
@Slf4j
public class IndexDataController {

    private final IndexDataService indexDataService;
    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;

    @PostMapping
    public ResponseEntity<IndexDataDto> create(@RequestBody IndexDataCreateRequest request) {
        IndexDataDto indexData = indexDataService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(indexData);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IndexDataDto> update(@PathVariable Long id,
        @RequestBody IndexDataUpdateRequest request){
        IndexDataDto updatedIndexData = indexDataService.update(id, request);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedIndexData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        indexDataService.delete(id);
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    @GetMapping
    public ResponseEntity<CursorPageResponseIndexData<IndexDataDto>> findByCursor(@ModelAttribute IndexDataQueryParams params) {
        log.debug("📌 [커서 조회] sortField={}, cursor={}, idAfter={}, direction={}",
            params.sortField(), params.cursor(), params.idAfter(), params.sortDirection());

        CursorPageResponseIndexData<IndexDataDto> result = indexDataService.findByCursor(params);
        log.debug("✅ 커서 조회 완료] 결과 수: {}", result.content().size());
        return ResponseEntity.ok(result);
    }


    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(@ModelAttribute IndexDataQueryParams params) {
        log.debug("🟡 [CSV Export 요청] {}", params);

        List<IndexDataDto> data = indexDataService.findAllByConditions(params);
        String csv = IndexDataCsvExporter.toCsv(data);
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);

        String fileName = buildExportFileName(params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        return ResponseEntity.ok()
            .headers(headers)
            .body(csvBytes);
    }


    private String buildExportFileName(IndexDataQueryParams params) {
        StringBuilder name = new StringBuilder("index-data");

        if (params.indexInfoId() != null) {
            name.append("_").append(params.indexInfoId());
        }

        if (params.startDate() != null || params.endDate() != null) {
            name.append("_");
            if (params.startDate() != null) {
                name.append(params.startDate());
            }
            name.append("~");
            if (params.endDate() != null) {
                name.append(params.endDate());
            }
        }

        name.append(".csv");
        return name.toString();
    }

    @GetMapping("/{id}/chart")
    public ResponseEntity<IndexChartDto> getChartData(
        @PathVariable("id") Long indexInfoId,
        @RequestParam(value = "periodType", defaultValue = "DAILY") Period period) {

        IndexChartDto chartData = indexDataService.getIndexChart(indexInfoId, period);
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/performance/favorite")
    public ResponseEntity<List<IndexPerformanceDto>> getFavoriteIndexPerformances(
        @RequestParam(value = "periodType", defaultValue = "DAILY") Period period ) {
        List<IndexPerformanceDto> result = indexDataService.getFavoriteIndexPerformances(period);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/performance/rank")
    public ResponseEntity<List<RankedIndexPerformanceDto>> getPerformanceRank(
        @RequestParam(required = false) Long indexInfoId,
        @RequestParam(value = "periodType", defaultValue = "DAILY") Period period,
        @RequestParam(defaultValue = "10") int limit
    ) {
        List<RankedIndexPerformanceDto> result = indexDataService.getIndexPerformanceRank(indexInfoId, period, limit);
        return ResponseEntity.ok(result);
    }


//    @GetMapping("/debug/favorite-data")
//    public ResponseEntity<String> debugFavoriteData() {
//        List<IndexInfo> favorites = indexInfoRepository.findByFavoriteTrue();
//        StringBuilder debug = new StringBuilder();
//
//        debug.append("=== 즐겨찾기 지수 디버그 ===\n");
//        debug.append("총 즐겨찾기 지수: ").append(favorites.size()).append("\n\n");
//
//        for (IndexInfo info : favorites) {
//            debug.append("지수: ").append(info.getIndexName()).append(" (ID: ").append(info.getId()).append(")\n");
//
//            // 모든 데이터 조회
//            List<IndexData> allData = indexDataRepository
//                .findByIndexInfoIdAndBaseDateLessThanEqualOrderByBaseDateDesc(
//                    info.getId(), LocalDate.now());
//
//            debug.append("  총 데이터 개수: ").append(allData.size()).append("\n");
//
//            if (!allData.isEmpty()) {
//                debug.append("  최신 3개 데이터:\n");
//                for (int i = 0; i < Math.min(3, allData.size()); i++) {
//                    IndexData data = allData.get(i);
//                    debug.append("    ").append(i+1).append(". 날짜: ").append(data.getBaseDate())
//                        .append(", 종가: ").append(data.getClosingPrice()).append("\n");
//                }
//            }
//            debug.append("\n");
//        }
//
//        return ResponseEntity.ok(debug.toString());
//    }
//
//    @GetMapping("/debug/period-test")
//    public ResponseEntity<String> debugPeriodTest(@RequestParam(defaultValue = "DAILY") Period period) {
//        List<IndexInfo> favorites = indexInfoRepository.findByFavoriteTrue();
//        StringBuilder debug = new StringBuilder();
//
//        debug.append("=== Period 테스트: ").append(period).append(" ===\n");
//        debug.append("현재 날짜: ").append(LocalDate.now()).append("\n\n");
//
//        for (IndexInfo info : favorites) {
//            debug.append("지수: ").append(info.getIndexName()).append(" (ID: ").append(info.getId()).append(")\n");
//
//            // 현재 데이터 조회
//            Optional<IndexData> currentOpt = indexDataRepository.findTopByIndexInfoIdOrderByBaseDateDesc(info.getId());
//            if (currentOpt.isPresent()) {
//                IndexData current = currentOpt.get();
//                debug.append("  현재 데이터: ").append(current.getBaseDate()).append(" - ").append(current.getClosingPrice()).append("\n");
//
//                // Period별 기준일 계산
//                LocalDate baseDate = calculateBaseDateFromCurrent(current.getBaseDate(), period);
//                debug.append("  기준일 (").append(period).append("): ").append(baseDate).append("\n");
//
//                // 기준일 이전 데이터들 조회
//                List<IndexData> beforeData = indexDataRepository
//                    .findByIndexInfoIdAndBaseDateLessThanEqualOrderByBaseDateDesc(info.getId(), baseDate);
//
//                debug.append("  기준일 이전 데이터 개수: ").append(beforeData.size()).append("\n");
//                if (!beforeData.isEmpty()) {
//                    debug.append("  기준일 이전 최신 데이터: ").append(beforeData.get(0).getBaseDate())
//                        .append(" - ").append(beforeData.get(0).getClosingPrice()).append("\n");
//                }
//            }
//            debug.append("\n");
//        }
//
//        return ResponseEntity.ok(debug.toString());
//    }
//
//    // 디버그용 기준일 계산 메서드
//    private LocalDate calculateBaseDateFromCurrent(LocalDate currentDate, Period periodType) {
//        return switch (periodType) {
//            case DAILY -> currentDate.minusDays(1);
//            case WEEKLY -> currentDate.minusWeeks(1);
//            case MONTHLY -> currentDate.minusMonths(1);
//            case QUARTERLY -> currentDate.minusMonths(3);
//            case YEARLY -> currentDate.minusYears(1);
//        };
//    }

}
