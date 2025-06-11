package com.sprint.findex.repository;

import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IndexDataRepository extends JpaRepository<IndexData, Long>,
    JpaSpecificationExecutor<IndexData> {
    List<IndexData> findByIndexInfo_IdAndBaseDateBetween(
        Long indexInfoId,
        LocalDate baseDateFrom,
        LocalDate baseDateTo
    );

    boolean existsByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);

    /* 지수 정보 ID와 날짜가 일치하는 IndexData 레코드를 조회하는 쿼리*/
    @Query(value = "SELECT * FROM index_data i " +
            "WHERE i.index_info_id = :indexInfoId " +
            "AND i.base_date <= :baseDate " +
            "ORDER BY i.base_date DESC LIMIT 1",
            nativeQuery = true)
    Optional<IndexData> findByIndexInfoIdAndBaseDateOnlyDateMatch(
            @Param("indexInfoId") Long indexInfoId,
            @Param("baseDate") LocalDate baseDate);
}
