package com.sprint.findex.repository;

import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndexDataRepository extends JpaRepository<IndexData, Long>,
    JpaSpecificationExecutor<IndexData> {
    List<IndexData> findByIndexInfo_IdAndBaseDateBetween(
        Long indexInfoId,
        LocalDate baseDateFrom,
        LocalDate baseDateTo
    );

    @Query("SELECT i FROM IndexData i " +
        "WHERE i.indexInfo.id = :indexInfoId " +
        "AND FUNCTION('DATE', i.baseDate) = :baseDate")
    Optional<IndexData> findByIndexInfoIdAndBaseDateOnlyDateMatch(
        @Param("indexInfoId") Long indexInfoId,
        @Param("baseDate") LocalDate baseDate
    );

    boolean existsByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);
}
