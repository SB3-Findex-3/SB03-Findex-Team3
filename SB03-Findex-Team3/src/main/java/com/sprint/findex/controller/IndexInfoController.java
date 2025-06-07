package com.sprint.findex.controller;

import com.sprint.findex.dto.request.IndexInfoCreateCommand;
import com.sprint.findex.dto.request.IndexInfoCreateRequest;
import com.sprint.findex.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex.dto.response.IndexInfoDto;
import com.sprint.findex.service.IndexInfoService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

    private final IndexInfoService indexInfoService;


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
        return ResponseEntity.noContent().build();
    }
}
