package com.sprint.findex.repository;

import com.sprint.findex.entity.AutoSyncConfig;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AutoSyncConfigRepository extends JpaRepository<AutoSyncConfig, Long>, JpaSpecificationExecutor<AutoSyncConfig> {
    Optional<AutoSyncConfig> findById(@NotNull Long id);

//    List<AutoSyncConfig> findAllByIndexInfoIdAndEnabled(Long indexInfoId, boolean enabled);
//
//    CursorPageResponseAutoSyncConfigDto findByCursor(AutoSyncConfigQueryParams queryParams);
}

