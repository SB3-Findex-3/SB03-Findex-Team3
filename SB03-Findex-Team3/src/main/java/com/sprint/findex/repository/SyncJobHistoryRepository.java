package com.sprint.findex.repository;

import com.sprint.findex.entity.SyncJobHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncJobHistoryRepository extends JpaRepository<SyncJobHistory, Long> {

}
