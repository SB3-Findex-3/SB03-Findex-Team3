package com.sprint.findex.repository;

import com.sprint.findex.entity.SyncJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long> {
    // 필요에 따라 커스텀 쿼리 메서드 추가 가능
}