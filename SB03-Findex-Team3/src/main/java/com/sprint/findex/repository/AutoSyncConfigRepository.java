package com.sprint.findex.repository;

import com.sprint.findex.entity.AutoSyncConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AutoSyncConfigRepository extends JpaRepository<AutoSyncConfig, Long>, JpaSpecificationExecutor<AutoSyncConfig> {
}

