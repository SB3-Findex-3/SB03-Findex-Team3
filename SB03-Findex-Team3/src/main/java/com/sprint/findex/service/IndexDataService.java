package com.sprint.findex.service;

import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.request.IndexDataQueryParams;
import com.sprint.findex.dto.request.IndexDataUpdateRequest;
import com.sprint.findex.dto.response.CursorPageResponseIndexData;
import com.sprint.findex.dto.response.IndexDataDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface IndexDataService {

    IndexDataDto create(IndexDataCreateRequest request);

    IndexDataDto update(Long id, IndexDataUpdateRequest request);

    void delete(Long id);

    CursorPageResponseIndexData<IndexDataDto> findByCursor(IndexDataQueryParams params);

    List<IndexDataDto> findAllByConditions(IndexDataQueryParams params);

}
