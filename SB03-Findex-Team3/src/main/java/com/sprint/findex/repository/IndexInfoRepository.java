package com.sprint.findex.repository;

import com.sprint.findex.entity.IndexInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long>,
    JpaSpecificationExecutor<IndexInfo> {

    List<IndexInfo> findAllByOrderByIdAsc();

    List<IndexInfo> findByFavoriteTrue();

    // Open API 연동을 위해 추가한 메서드 3개
    Optional<IndexInfo> findByIndexClassificationAndIndexName(String indexClassification, String indexName);

    List<IndexInfo> findByIndexClassification(String indexClassification);

    Optional<IndexInfo> findByIndexName(String indexName);
}