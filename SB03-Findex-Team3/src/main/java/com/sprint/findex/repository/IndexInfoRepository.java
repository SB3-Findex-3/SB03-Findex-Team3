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

    Optional<IndexInfo> findByIndexClassificationAndIndexName(String indexClassification, String indexName);

    boolean existsByIndexClassificationAndIndexName(String indexClassification, String indexName);
}