package com.sprint.findex.service;

import com.sprint.findex.dto.request.IndexInfoCreateCommand;
import com.sprint.findex.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex.dto.response.IndexInfoDto;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.dto.response.IndexInfoSearchDto;
import com.sprint.findex.dto.response.cursor.CursorPageResponseIndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoSummaryDto;
import java.util.List;


public interface IndexInfoService {

    IndexInfoDto createIndexInfo(IndexInfoCreateCommand command);

    IndexInfo createIndexInfoFromApi(IndexInfoCreateCommand command);

    IndexInfoDto updateIndexInfo(Long id, IndexInfoUpdateRequest updateDto);

    void deleteIndexInfo(Long id);

    IndexInfoDto findById(Long id);

    CursorPageResponseIndexInfoDto findIndexInfoByCursor(IndexInfoSearchDto searchDto);

    List<IndexInfoSummaryDto> findIndexInfoSummary();
}
