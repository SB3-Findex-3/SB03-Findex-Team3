package com.sprint.findex.repository;

import com.sprint.findex.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {



}
