package com.sprint.findex.repository;

import com.sprint.findex.entity.SyncJob;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long>,
    JpaSpecificationExecutor<SyncJob> {
    Optional<SyncJob> findTopByIndexInfoIdOrderByJobTimeDesc(Long indexInfoId);
}