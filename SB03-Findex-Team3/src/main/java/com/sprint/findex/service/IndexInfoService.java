package com.sprint.findex.service;

import com.sprint.findex.dto.IndexInfoSearchDto;
import com.sprint.findex.dto.response.CursorPageResponseIndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoSummaryDto;
import java.util.List;

public interface IndexInfoService {

    // Index Info 생성

    // Index Info 조회
    IndexInfoDto findById(Long id);

    // Index Info 목록 조회
    CursorPageResponseIndexInfoDto findIndexInfoByCursor(IndexInfoSearchDto searchDto);

    // Index Info 요약 목록 조회
    List<IndexInfoSummaryDto> findIndexInfoSummary();

    // Index Info 수정

    // Index Info 삭제
}
