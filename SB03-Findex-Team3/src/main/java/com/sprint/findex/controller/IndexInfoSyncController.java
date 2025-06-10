package com.sprint.findex.controller;

import com.sprint.findex.dto.request.SyncJobHistoryCreateRequest;
import com.sprint.findex.dto.response.SyncJobDto;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.SyncJobType;
import com.sprint.findex.global.util.IpUtil;
import com.sprint.findex.service.IndexInfoSyncService;
import com.sprint.findex.service.SyncJobHistoryService;
import com.sprint.findex.service.SyncJobService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class IndexInfoSyncController {

    private final IndexInfoSyncService indexInfoSyncService;
    private final SyncJobHistoryService syncJobHistoryService;
    private final SyncJobService syncJobService;

    // 지수 정보 비동기 동기화 실행 (WebClient 비동기 방식)
    @PostMapping("index-infos")
    public ResponseEntity<List<SyncJobDto>> syncIndexInfoAsync(HttpServletRequest httpRequest) {
        String clientIp = IpUtil.getClientIp(httpRequest);
        Mono<List<SyncJobDto>> result = syncJobService.fetchAllIndexInfo(clientIp);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(result.block());

    }

    // 모든 지수 정보 조회
    @GetMapping("/findAll")
    public ResponseEntity<List<IndexInfo>> getAllIndexInfo(HttpServletRequest request) {
        String clientIp = IpUtil.getClientIp(request);
        String requestUrl = request.getMethod() + " " + request.getRequestURL();

        try {
            log.info("모든 지수 정보 조회 요청");
            log.info("요청 정보 - IP: {}, URL: {}", clientIp, requestUrl);

            // 먼저 데이터 조회
            List<IndexInfo> indexInfoList = indexInfoSyncService.getAllIndexInfo();

            // 데이터가 있는 경우 각 지수 정보마다 이력 저장
            if (!indexInfoList.isEmpty()) {
                try {
                    // 현재 날짜
                    LocalDate today = LocalDate.now();

                    // 각 지수 정보마다 별도의 이력 저장
                    for (IndexInfo indexInfo : indexInfoList) {
                        // 연동 이력을 위한 요청 객체 생성
                        SyncJobHistoryCreateRequest syncRequest = new SyncJobHistoryCreateRequest(
                            SyncJobType.INDEX_INFO,  // 작업 유형
                            indexInfo,              // 각 지수 정보 사용
                            today,                  // 대상 날짜 (오늘)
                            clientIp                // 작업자 (IP 주소)
                        );

                        // 각 지수 정보별 작업 이력 저장
                        syncJobHistoryService.saveHistory(syncRequest);
                    }

                } catch (Exception e) {
                    log.error("이력 저장 중 오류 발생", e);
                    // 이력 저장 실패는 무시하고 계속 진행
                }
            }

            log.info("지수 정보 {}개 조회 완료", indexInfoList.size());
            log.info("  - 요청자 IP: {}", clientIp);

            return ResponseEntity.ok(indexInfoList);

        } catch (Exception e) {
            log.error("지수 정보 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}




