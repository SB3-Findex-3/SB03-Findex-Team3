package com.sprint.findex.repository;

import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IndexDataRepository extends JpaRepository<IndexData, Long>,
    JpaSpecificationExecutor<IndexData> {
    boolean existsByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);
}
