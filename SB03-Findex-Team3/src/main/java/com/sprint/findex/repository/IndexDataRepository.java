package com.sprint.findex.repository;

import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IndexDataRepository extends JpaRepository<IndexData, Long>,
    JpaSpecificationExecutor<IndexData> {
    List<IndexData> findByIndexInfo_IdAndBaseDateBetween(
        Long indexInfoId,
        LocalDate baseDateFrom,
        LocalDate baseDateTo
    );

    boolean existsByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);
}
