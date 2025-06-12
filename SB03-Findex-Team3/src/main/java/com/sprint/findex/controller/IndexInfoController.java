package com.sprint.findex.controller;

import com.sprint.findex.controller.api.IndexInfoApi;
import com.sprint.findex.dto.request.IndexInfoCreateCommand;
import com.sprint.findex.dto.request.IndexInfoCreateRequest;
import com.sprint.findex.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex.dto.response.IndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoSearchDto;
import com.sprint.findex.dto.response.IndexInfoSummaryDto;
import com.sprint.findex.dto.response.cursor.CursorPageResponseIndexInfoDto;
import com.sprint.findex.mapper.IndexInfoSearchMapper;
import com.sprint.findex.service.IndexInfoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/index-infos")
public class IndexInfoController implements IndexInfoApi {

    private final IndexInfoService indexInfoService;
    private final IndexInfoSearchMapper indexInfoSearchMapper;

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Object> getIndexInfo(@PathVariable("id") Long id) {
        IndexInfoDto indexInfo = indexInfoService.findById(id);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(indexInfo);
    }

    @Override
    @GetMapping
    public ResponseEntity<Object> getIndexInfoList(
        @RequestParam(required = false) String indexClassification,
        @RequestParam(required = false) String indexName,
        @RequestParam(required = false) Boolean favorite,
        @RequestParam(required = false) String idAfter,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "indexClassification", required = false) String sortField,
        @RequestParam(defaultValue = "asc", required = false) String sortDirection,
        @RequestParam(defaultValue = "20", required = false) int size
    ) {
        IndexInfoSearchDto searchDto = indexInfoSearchMapper.toDto(
            indexClassification,
            indexName,
            favorite,
            idAfter,
            cursor,
            sortField,
            sortDirection,
            size
        );

        CursorPageResponseIndexInfoDto response = indexInfoService.findIndexInfoByCursor(searchDto);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    @GetMapping("/summaries")
    public ResponseEntity<Object> getIndexInfoSummaries() {
        List<IndexInfoSummaryDto> indexInfoSummary = indexInfoService.findIndexInfoSummary();

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(indexInfoSummary);
    }

    @PostMapping
    public ResponseEntity<IndexInfoDto> createIndexInfo(@Valid @RequestBody IndexInfoCreateRequest request) {
        IndexInfoCreateCommand command = IndexInfoCreateCommand.fromUser(request);
        IndexInfoDto indexInfoDto = indexInfoService.createIndexInfo(command);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(indexInfoDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IndexInfoDto> updateIndexInfo(@PathVariable Long id,
        @Valid @RequestBody IndexInfoUpdateRequest request) {
        IndexInfoDto updatedIndex = indexInfoService.updateIndexInfo(id, request);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedIndex);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndexInfo(@PathVariable Long id) {
        indexInfoService.deleteIndexInfo(id);

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }
}
