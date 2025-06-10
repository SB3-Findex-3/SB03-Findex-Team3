package com.sprint.findex.service;

import com.sprint.findex.dto.IndexInfoSearchDto;
import com.sprint.findex.dto.request.IndexInfoCreateCommand;
import com.sprint.findex.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex.dto.response.CursorPageResponseIndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoSummaryDto;
import com.sprint.findex.entity.IndexInfo;

import java.util.List;


public interface IndexInfoService {

    // Index Info 생성
    IndexInfoDto createIndexInfo(IndexInfoCreateCommand command);

    // Open_API로 Index Info 생성
    IndexInfo createIndexInfoFromApi(IndexInfoCreateCommand command);

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
