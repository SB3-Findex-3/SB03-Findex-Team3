package com.sprint.findex.service;

import com.sprint.findex.dto.IndexDataDto;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface IndexDataService {
    @Transactional
    List<IndexDataDto> findAll(Long indexInfoId, LocalDate baseDateFrom, LocalDate baseDateTo);
}
