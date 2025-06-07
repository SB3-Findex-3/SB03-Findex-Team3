package com.sprint.findex.service;

import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.request.IndexDataUpdateRequest;
import com.sprint.findex.dto.response.IndexDataDto;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface IndexDataService {
    @Transactional
    List<IndexDataDto> findAll(Long indexInfoId, LocalDate baseDateFrom, LocalDate baseDateTo);
    IndexDataDto create(IndexDataCreateRequest request);
    IndexDataDto update(Long id, IndexDataUpdateRequest request);
    void delete(Long id);
}
