package com.sprint.findex.repository;

import com.sprint.findex.entity.IndexData;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexDataRepository extends JpaRepository<IndexData, Long> {
    List<IndexData> findByIndexInfo_IdAndBaseDateBetween(
        Long indexInfoId,
        LocalDate baseDateFrom,
        LocalDate baseDateTo
    );
}
