package com.sprint.findex.repository;

import com.sprint.findex.entity.SyncJobHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncJobHistoryRepository extends JpaRepository<SyncJobHistoryRepository, Long> {
    Optional<SyncJobHistory> findTopByOrderByJobTimeDesc();
}
