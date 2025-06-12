package com.sprint.findex.service;

import com.sprint.findex.dto.request.IndexInfoCreateCommand;
import com.sprint.findex.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex.dto.response.CursorPageResponseIndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoSearchDto;
import com.sprint.findex.dto.response.IndexInfoSummaryDto;

import java.util.List;


public interface IndexInfoService {

    // Index Info 생성
    IndexInfoDto createIndexInfo(IndexInfoCreateCommand command);

    // Index Info 업데이트
    IndexInfoDto updateIndexInfo(Long id, IndexInfoUpdateRequest updateDto);

    // Index Info 삭제
    void deleteIndexInfo(Long id);

    // Index Info 조회
    IndexInfoDto findById(Long id);

    // Index Info 목록 조회
    CursorPageResponseIndexInfoDto findIndexInfoByCursor(IndexInfoSearchDto searchDto);

    // Index Info 요약 목록 조회
    List<IndexInfoSummaryDto> findIndexInfoSummary();
}
