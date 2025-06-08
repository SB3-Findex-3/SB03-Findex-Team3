package com.sprint.findex.controller;

import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.global.util.IpUtil;
import com.sprint.findex.service.IndexInfoSyncService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
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
@RequestMapping("/api/sync-jobs/index-infos")
@RequiredArgsConstructor
public class IndexInfoSyncController {

    private final IndexInfoSyncService indexInfoSyncService;


    // 지수 정보 비동기 동기화 실행 (WebClient 비동기 방식)

    @PostMapping("/sync")
    public Mono<ResponseEntity<String>> syncIndexInfoAsync(HttpServletRequest request) {

        // ip 주소 받아오기~
        String clientIp = IpUtil.getClientIp(request);
        String requestUrl = request.getMethod() + " " + request.getRequestURL();

        log.info("지수 정보 비동기 동기화 요청 시작");

        return indexInfoSyncService.fetchAndSaveIndexInfoAsync()
            .map(apiResponse -> {
                log.info("지수 정보 비동기 동기화 완료");
                log.info("  - 요청자 IP: {}", clientIp);
                return ResponseEntity.ok("지수 정보 비동기 동기화가 성공적으로 완료되었습니다.");
            })
            .onErrorResume(e -> {
                log.error("지수 정보 비동기 동기화 실패", e);
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

            List<IndexInfo> indexInfoList = indexInfoSyncService.getAllIndexInfo();

            log.info("지수 정보 {}개 조회 완료", indexInfoList.size());
            log.info("  - 요청자 IP: {}", clientIp);
            return ResponseEntity.ok(indexInfoList);

        } catch (Exception e) {
            log.error("지수 정보 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
