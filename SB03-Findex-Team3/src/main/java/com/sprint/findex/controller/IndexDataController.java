package com.sprint.findex.controller;

import com.sprint.findex.controller.api.IndexDataApi;
import com.sprint.findex.dto.dashboard.IndexChartDto;
import com.sprint.findex.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex.dto.dashboard.RankedIndexPerformanceDto;
import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.request.IndexDataQueryParams;
import com.sprint.findex.dto.request.IndexDataUpdateRequest;
import com.sprint.findex.dto.response.cursor.CursorPageResponseIndexData;
import com.sprint.findex.dto.response.IndexDataCsvExporter;
import com.sprint.findex.dto.response.IndexDataDto;
import com.sprint.findex.entity.Period;
import com.sprint.findex.service.IndexDataService;
import jakarta.validation.Valid;
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
public class IndexDataController implements IndexDataApi {

    private final IndexDataService indexDataService;

    @PostMapping
    public ResponseEntity<IndexDataDto> create(@Valid @RequestBody IndexDataCreateRequest request) {
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
        log.debug("[IndexDataController] 커서 조회 결과: sortField={}, cursor={}, idAfter={}, direction={}",
            params.sortField(), params.cursor(), params.idAfter(), params.sortDirection());

        CursorPageResponseIndexData<IndexDataDto> result = indexDataService.findByCursor(params);
        log.debug("[IndexDataController] 조회 된 결과 수: {}", result.content().size());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(result);
    }


    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(@ModelAttribute IndexDataQueryParams params) {
        log.debug("[IndexDataController] CSV Export 요청: {}", params);

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

        return ResponseEntity
            .ok(chartData);
    }

    @GetMapping("/performance/favorite")
    public ResponseEntity<List<IndexPerformanceDto>> getFavoriteIndexPerformances(
        @RequestParam(value = "periodType", defaultValue = "DAILY") Period period ) {
        List<IndexPerformanceDto> result = indexDataService.getFavoriteIndexPerformances(period);

        return ResponseEntity
            .ok(result);
    }

    @GetMapping("/performance/rank")
    public ResponseEntity<List<RankedIndexPerformanceDto>> getPerformanceRank(
        @RequestParam(required = false) Long indexInfoId,
        @RequestParam(value = "periodType", defaultValue = "DAILY") Period period,
        @RequestParam(defaultValue = "10") int limit
    ) {
        List<RankedIndexPerformanceDto> result = indexDataService.getIndexPerformanceRank(indexInfoId, period, limit);

        return ResponseEntity
            .ok(result);
    }

}
