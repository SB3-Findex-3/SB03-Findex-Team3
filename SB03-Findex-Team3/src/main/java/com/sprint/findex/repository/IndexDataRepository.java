package com.sprint.findex.repository;

import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexDataRepository extends JpaRepository<IndexData, Long>,
    JpaSpecificationExecutor<IndexData> {

    Optional<IndexData> findTopByIndexInfoIdOrderByBaseDateDesc(Long id);

    Optional<IndexData> findByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);
      
    @Query(value = "SELECT * FROM index_data i " +
        "WHERE i.index_info_id = :indexInfoId " +
        "AND i.base_date <= :baseDate " +
        "ORDER BY i.base_date DESC LIMIT 1",
        nativeQuery = true)
    Optional<IndexData> findByIndexInfoIdAndBaseDateOnlyDateMatch(
        @Param("indexInfoId") Long indexInfoId,
        @Param("baseDate") LocalDate baseDate);
}