package com.sprint.findex.service;

import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.request.IndexDataUpdateRequest;
import com.sprint.findex.dto.response.IndexDataDto;

public interface IndexDataService {
    IndexDataDto create(IndexDataCreateRequest request);
    IndexDataDto update(Long id, IndexDataUpdateRequest request);
    void delete(Long id);
}
