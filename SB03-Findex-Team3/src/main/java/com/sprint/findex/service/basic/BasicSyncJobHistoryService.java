package com.sprint.findex.service.basic;

import com.sprint.findex.dto.request.SyncJobHistoryCreateRequest;
import com.sprint.findex.entity.SyncJobHistory;
import com.sprint.findex.entity.SyncJobResult;
import com.sprint.findex.repository.SyncJobHistoryRepository;
import com.sprint.findex.service.SyncJobHistoryService;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicSyncJobHistoryService implements SyncJobHistoryService {

    private final SyncJobHistoryRepository syncJobHistoryRepository;

    @Override
    public SyncJobHistory saveHistory(SyncJobHistoryCreateRequest syncJobHistoryCreateRequest) {
        SyncJobHistory syncJobHistory = SyncJobHistory.create(
            syncJobHistoryCreateRequest.jobType(),
            syncJobHistoryCreateRequest.indexInfo(),
            syncJobHistoryCreateRequest.targetDate(),
            syncJobHistoryCreateRequest.worker(),
            OffsetDateTime.now(),
            SyncJobResult.SUCCESS
        );

        return syncJobHistoryRepository.save(syncJobHistory);
    }

    @Override
    public void updateResult(Long id, SyncJobResult result) {
        SyncJobHistory history = syncJobHistoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("[SyncJobHistoryService] 작업 이력 찾을 수 없음: " + id));

        // 결과 업데이트 로직
        // SyncJobHistory 클래스가 불변 객체인 경우 새 인스턴스 생성 필요
        SyncJobHistory updatedHistory = SyncJobHistory.create(
            history.getJobType(),
            history.getIndexInfo(),
            history.getTargetDate(),
            history.getWorker(),
            history.getJobTime(),
            result
        );

        // ID 설정 (필요한 경우)
        // updatedHistory.setId(id); // SyncJobHistory에 setter가 있다면

        syncJobHistoryRepository.save(updatedHistory);
    }

}
