package com.sprint.findex.service;

import com.sprint.findex.dto.request.IndexInfoCreateCommand;
import com.sprint.findex.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex.dto.response.IndexInfoDto;
import com.sprint.findex.entity.IndexInfo;

public interface IndexInfoService {

    IndexInfoDto createIndexInfo(IndexInfoCreateCommand command);

    IndexInfo createIndexInfoFromApi(IndexInfoCreateCommand command);

    IndexInfoDto updateIndexInfo(Long id, IndexInfoUpdateRequest updateDto);

    void deleteIndexInfo(Long id);


}
