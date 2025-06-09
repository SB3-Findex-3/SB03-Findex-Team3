package com.sprint.findex.controller;

import com.sprint.findex.dto.request.SyncJobHistoryCreateRequest;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.SyncJobHistory;
import com.sprint.findex.entity.SyncJobResult;
import com.sprint.findex.entity.SyncJobType;
import com.sprint.findex.global.util.IpUtil;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.service.IndexInfoSyncService;
import com.sprint.findex.service.SyncJobHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
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
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequestMapping("/api/sync-jobs/index-infos")
@RequiredArgsConstructor
public class IndexInfoSyncController {

    private final IndexInfoSyncService indexInfoSyncService;
    private final SyncJobHistoryService syncJobHistoryService;
    private final IndexInfoRepository indexInfoRepository;

    // 지수 정보 비동기 동기화 실행 (WebClient 비동기 방식)
    @PostMapping
    public Mono<ResponseEntity<String>> syncIndexInfoAsync(HttpServletRequest request) {
        // ip 주소 받아오기~
        String clientIp = IpUtil.getClientIp(request);
        String requestUrl = request.getMethod() + " " + request.getRequestURL();

        log.info("지수 정보 비동기 동기화 요청 시작");

        // 먼저 API 호출을 시작하고 결과를 처리
        return indexInfoSyncService.fetchAndSaveIndexInfoAsync()
            .flatMap(apiResponse -> {
                log.info("지수 정보 비동기 동기화 완료");
                log.info("  - 요청자 IP: {}", clientIp);

                // API 호출 성공 후 모든 지수 정보에 대해 이력 저장
                return Mono.fromCallable(() -> {
                    try {
                        // 현재 날짜
                        LocalDate today = LocalDate.now();

                        // 동기화 완료 후 지수 정보 가져오기
                        List<IndexInfo> indexInfos = indexInfoRepository.findAllByOrderByIdAsc();
                        if (indexInfos.isEmpty()) {
                            log.warn("동기화 완료 후에도 지수 정보가 없습니다. 동기화가 제대로 되었는지 확인하세요.");
                            // 이력 저장 없이 성공 응답
                            return ResponseEntity.ok("지수 정보 비동기 동기화가 완료되었으나, 저장된 지수 정보가 없습니다.");
                        }

                        // 각 지수 정보마다 별도의 이력 저장
                        for (IndexInfo indexInfo : indexInfos) {
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

                        return ResponseEntity.ok("지수 정보 비동기 동기화가 성공적으로 완료되었습니다. " +
                            indexInfos.size() + "개의 지수 정보에 대한 이력이 저장되었습니다.");
                    } catch (Exception e) {
                        log.error("동기화 이력 저장 중 오류 발생", e);
                        return ResponseEntity.ok(
                            "지수 정보 비동기 동기화는 완료되었으나, 이력 저장 중 오류가 발생했습니다: " + e.getMessage());
                    }
                }).subscribeOn(Schedulers.boundedElastic());
            })

            .onErrorResume(e -> {
                log.error("지수 정보 비동기 동기화 실패", e);

                // API 호출 실패 시 이력 저장 시도 (가능한 경우에만)
                try {
                    List<IndexInfo> indexInfos = indexInfoRepository.findAllByOrderByIdAsc();
                    if (!indexInfos.isEmpty()) {
                        LocalDate today = LocalDate.now();
                        IndexInfo representativeIndex = indexInfos.get(0);

                        SyncJobHistoryCreateRequest syncRequest = new SyncJobHistoryCreateRequest(
                            SyncJobType.INDEX_INFO,
                            representativeIndex,
                            today,
                            clientIp
                        );

                        SyncJobHistory jobHistory = syncJobHistoryService.saveHistory(syncRequest);
                        syncJobHistoryService.updateResult(jobHistory.getId(),
                            SyncJobResult.FAILED);
                    }
                } catch (Exception ex) {
                    log.error("실패 이력 저장 중 오류 발생", ex);
                }

                return Mono.just(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("지수 정보 비동기 동기화 중 오류가 발생했습니다: " + e.getMessage())
                );
            })
            .timeout(Duration.ofMinutes(2)) // 타임아웃(2분)
            .doOnSubscribe(subscription -> log.info("비동기 동기화 구독 시작"))
            .doFinally(signalType -> log.info("비동기 동기화 종료: {}", signalType));
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




