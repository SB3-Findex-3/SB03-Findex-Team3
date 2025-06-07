package com.sprint.findex.controller;

import com.sprint.findex.controller.api.IndexInfoApi;
import com.sprint.findex.dto.IndexInfoSearchDto;
import com.sprint.findex.dto.request.IndexInfoCreateCommand;
import com.sprint.findex.dto.request.IndexInfoCreateRequest;
import com.sprint.findex.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex.dto.response.CursorPageResponseIndexInfoDto;
import com.sprint.findex.dto.response.ErrorResponse;
import com.sprint.findex.dto.response.IndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoSummaryDto;
import com.sprint.findex.mapper.IndexInfoSearchMapper;
import com.sprint.findex.service.IndexInfoService;
import com.sprint.findex.service.OpenApiService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/index-infos")
public class IndexInfoController implements IndexInfoApi {

    private final IndexInfoService indexInfoService;
    private final IndexInfoSearchMapper indexInfoSearchMapper;
    private final OpenApiService openApiService;

    // 단건 조회
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Object> getIndexInfo(@PathVariable("id") Long id) {
        try {
            IndexInfoDto indexInfo = indexInfoService.findById(id);
            return ResponseEntity.status(HttpStatus.OK).body(indexInfo);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(404, "조회할 지수 정보를 찾을 수 없음", "ID가 " + id + "인 지수 정보가 존재하지 않습니다.")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(500, "서버 오류")
            );
        }
    }

    // 목록 조회
    @Override
    @GetMapping
    public ResponseEntity<Object> getIndexInfoList(
        @RequestParam(required = false) String indexClassification,
        @RequestParam(required = false) String indexName,
        @RequestParam(required = false) Boolean favorite,
        @RequestParam(required = false) Long idAfter,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "indexClassification", required = false) String sortField,
        @RequestParam(defaultValue = "asc", required = false) String sortDirection,
        @RequestParam(defaultValue = "10", required = false) int size
    ) {
        try {
            IndexInfoSearchDto searchDto = indexInfoSearchMapper.toDto(
                indexClassification, indexName, favorite, idAfter, cursor, sortField, sortDirection, size
            );

            log.info("IndexInfoController: 지수 정보 목록 조회 요청 -> classification: {}, name: {}, favorite: {}, " +
                    "idAfter: {}, cursor: {}, sort: {} {}, size: {}",
                indexClassification, indexName, favorite, idAfter, cursor, sortField, sortDirection, size);

            CursorPageResponseIndexInfoDto response = indexInfoService.findIndexInfoByCursor(searchDto);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(400, "잘못된 요청 (유효하지 않은 필터 값 등)")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(500, "서버 오류")
            );
        }
    }

    // 요약 리스트
    @GetMapping("/summaries")
    public ResponseEntity<Object> getIndexInfoSummaries() {
        try {
            List<IndexInfoSummaryDto> indexInfoSummary = indexInfoService.findIndexInfoSummary();
            return ResponseEntity.status(HttpStatus.OK).body(indexInfoSummary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(500, "서버 오류")
            );
        }
    }

    // 생성
    @PostMapping
    public ResponseEntity<IndexInfoDto> createIndexInfo(@Valid @RequestBody IndexInfoCreateRequest request) {
        IndexInfoCreateCommand command = IndexInfoCreateCommand.fromUser(request);
        IndexInfoDto indexInfoDto = indexInfoService.createIndexInfo(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(indexInfoDto);
    }

    // 수정
    @PatchMapping("/{id}")
    public ResponseEntity<IndexInfoDto> updateIndexInfo(@PathVariable Long id,
        @Valid @RequestBody IndexInfoUpdateRequest request) {
        IndexInfoDto updatedIndex = indexInfoService.updateIndexInfo(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(updatedIndex);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndexInfo(@PathVariable Long id) {
        indexInfoService.deleteIndexInfo(id);
        return ResponseEntity.noContent().build();
    }
}
