package com.sprint.findex.controller;

import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.request.IndexDataQueryParams;
import com.sprint.findex.dto.request.IndexDataUpdateRequest;
import com.sprint.findex.dto.response.CursorPageResponseIndexData;
import com.sprint.findex.dto.response.IndexDataCsvExporter;
import com.sprint.findex.dto.response.IndexDataDto;
import com.sprint.findex.service.IndexDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
@Slf4j
public class IndexDataController {

    private final IndexDataService indexDataService;

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
        log.debug("[커서 조회] sortField={}, cursor={}, idAfter={}, direction={}",
            params.sortField(), params.cursor(), params.idAfter(), params.sortDirection());

        CursorPageResponseIndexData<IndexDataDto> result = indexDataService.findByCursor(params);
        log.debug("커서 조회 완료] 결과 수: {}", result.content().size());
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

}
