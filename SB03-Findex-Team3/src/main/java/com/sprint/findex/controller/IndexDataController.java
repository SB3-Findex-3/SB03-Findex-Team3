package com.sprint.findex.controller;

import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.request.IndexDataUpdateRequest;
import com.sprint.findex.dto.response.IndexDataDto;
import com.sprint.findex.service.IndexDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
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

}
