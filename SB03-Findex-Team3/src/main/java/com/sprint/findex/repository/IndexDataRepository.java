package com.sprint.findex.repository;

import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndexDataRepository extends JpaRepository<IndexData, Long>,
    JpaSpecificationExecutor<IndexData> {

    Optional<IndexData> findTopByIndexInfoIdOrderByBaseDateDesc(Long id);

    boolean existsByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);

    // 날짜 관련 메서드 수정 - JPQL에서 DATE 함수 대신 날짜 비교를 직접 사용
    @Query("SELECT i FROM IndexData i " +
        "WHERE i.indexInfo.id = :indexInfoId " +
        "AND i.baseDate <= :baseDate " +
        "ORDER BY i.baseDate DESC")
    List<IndexData> findByIndexInfoIdAndBaseDateLessThanEqualOrderByBaseDateDesc(
        @Param("indexInfoId") Long indexInfoId,
        @Param("baseDate") LocalDate baseDate);

    // 첫 번째 결과만 반환하는 버전 - Pageable 사용
    @Query("SELECT i FROM IndexData i " +
        "WHERE i.indexInfo.id = :indexInfoId " +
        "AND i.baseDate <= :baseDate " +
        "ORDER BY i.baseDate DESC")
    List<IndexData> findByIndexInfoIdAndBaseDateLessThanEqualOrderByBaseDateDescWithLimit(
        @Param("indexInfoId") Long indexInfoId,
        @Param("baseDate") LocalDate baseDate,
        Pageable pageable);

    // 즐겨찾기 지수의 최신 데이터 조회 (각 지수별로 가장 최근 날짜의 데이터)
    @Query("SELECT id FROM IndexData id " +
        "WHERE id.indexInfo.id IN " +
        "(SELECT ii.id FROM IndexInfo ii WHERE ii.favorite = true) " +
        "AND (id.indexInfo.id, id.baseDate) IN " +
        "(SELECT i.indexInfo.id, MAX(i.baseDate) FROM IndexData i " +
        "GROUP BY i.indexInfo.id)")
    List<IndexData> findLatestDataForFavoriteIndices();

    // 즐겨찾기 지수의 특정 날짜 이전 데이터 조회
    @Query("SELECT id FROM IndexData id " +
        "JOIN id.indexInfo ii " +
        "WHERE ii.favorite = true " +
        "AND id.baseDate <= :baseDate " +
        "AND (id.indexInfo.id, id.baseDate) IN " +
        "(SELECT i.indexInfo.id, MAX(i.baseDate) " +
        "FROM IndexData i " +
        "WHERE i.baseDate <= :baseDate " +
        "GROUP BY i.indexInfo.id)")
    List<IndexData> findPreviousDataForFavoriteIndices(@Param("baseDate") LocalDate baseDate);

    @Query(value = "SELECT * FROM index_data i " +
        "WHERE i.index_info_id = :indexInfoId " +
        "AND i.base_date <= :baseDate " +
        "ORDER BY i.base_date DESC LIMIT 1",
        nativeQuery = true)
    Optional<IndexData> findByIndexInfoIdAndBaseDateOnlyDateMatch(
        @Param("indexInfoId") Long indexInfoId,
        @Param("baseDate") LocalDate baseDate);

}

