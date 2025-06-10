package com.sprint.findex.repository;

import com.sprint.findex.entity.AutoSyncConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AutoSyncRepository extends JpaRepository<AutoSyncConfig, Long>, JpaSpecificationExecutor<AutoSyncConfig> {
}
