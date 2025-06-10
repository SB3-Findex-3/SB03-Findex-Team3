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

    /* 지수 정보 ID와 날짜가 일치하는 IndexData 레코드를 조회하는 쿼리*/
    @Query("SELECT i FROM IndexData i " +
        "WHERE i.indexInfo.id = :indexInfoId " +
        "AND FUNCTION('DATE', i.baseDate) = :baseDate")
    Optional<IndexData> findByIndexInfoIdAndBaseDateOnlyDateMatch(
        @Param("indexInfoId") Long indexInfoId,
        @Param("baseDate") LocalDate baseDate
    );

    Optional<IndexData> findTopByIndexInfoIdOrderByBaseDateDesc(Long id);

    boolean existsByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);
}
