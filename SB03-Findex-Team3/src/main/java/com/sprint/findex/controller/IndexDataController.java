package com.sprint.findex.controller;

import com.sprint.findex.dto.IndexDataDto;
import com.sprint.findex.service.IndexDataService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {

    private final IndexDataService indexDataService;

    @GetMapping
    public ResponseEntity<List<IndexDataDto>> findAll(
        @RequestParam Long indexInfoId,
        @RequestParam LocalDate baseDateFrom,
        @RequestParam LocalDate baseDateTo
    ) {
        List<IndexDataDto> result = indexDataService.findAll(indexInfoId, baseDateFrom, baseDateTo);
        return ResponseEntity.ok(result);
    }
}
