package com.sprint.findex.service;


import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.SourceType;
import com.sprint.findex.global.dto.ApiResponse;
import com.sprint.findex.repository.IndexInfoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class IndexInfoSyncService {

    private final IndexInfoRepository indexInfoRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    //HTTP 요청 처리
    private final WebClient webClient;

    // application.yaml에서 직접 값 읽어오기
    @Value("${api.data.service-key}")
    private String serviceKey;

    @Value("${api.data.base-url}")
    private String baseUrl;

    // WebClient 주입 잘 됐는지 확인
    @PostConstruct
    public void checkWebClient() {
        System.out.println("WebClient 주입 상태: " + (webClient != null ? "성공" : "실패"));
        if (webClient != null) {
            System.out.println("WebClient 클래스: " + webClient.getClass().getName());
        }
    }

    // WebClient를 사용한 비동기 API 호출
    public Mono<ApiResponse> fetchAndSaveIndexInfoAsync() {
        log.info("지수 정보 비동기 동기화 시작");

        String url = buildApiUrl(1, 156);

        return callApiBasic(url)
            .doOnNext(response -> {
                if (response != null && response.getBody() != null) {
                    processApiResponse(response);
                }
            })
            .doOnError(error -> log.error("비동기 지수 정보 동기화 실패", error));
    }


    // 메인 로직 (API 요청 로직)
    private Mono<ApiResponse> callApiBasic(String url) {
        try{
            // URI 변환으로 인증키 오류 해결하기 (자꾸 인증키 없다고 해서 URI 객체로 만들기)
            URI uri = new URI(url);
            log.info("API 호출: {}", url.replaceAll("serviceKey=[^&]*", "serviceKey=****"));

            return webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)  // JSON 요청
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .retry(2)
                .doOnNext(response -> System.out.println("API 호출 성공!"))
                .doOnError(error -> System.out.println("API 호출 실패: " + error.getMessage()));

        }catch (URISyntaxException e) {
            log.error("URI 변환 실패: {}", url, e);
            return Mono.error(new RuntimeException("URI 변환 실패: " + e.getMessage()));
        }
    }


    // API URL 생성(@Value로 받은 설정값 사용)
    private String buildApiUrl(int pageNo, int numOfRows) {
        return String.format("%s/getStockMarketIndex?serviceKey=%s&resultType=json&pageNo=%d&numOfRows=%d",
            baseUrl, serviceKey, pageNo, numOfRows);
    }


    // 응답 받은 내용 처리 (DB에 저장하거나 업데이트)
    private void processApiResponse(ApiResponse response) {
        if (response.getBody() == null || response.getBody().getItems() == null) {
            return;
        }
        response.getBody().getItems().getItem()
            .forEach(this::saveOrUpdateIndexInfo);
    }


    // 저장/업데이트 로직
    private void saveOrUpdateIndexInfo(ApiResponse.StockIndexItem item) {
        IndexInfo indexInfo = indexInfoRepository
            // 조회했을 때
            .findByIndexClassificationAndIndexName(item.getIndexClassification(), item.getIndexName())
            // 이미 있으면 업데이트
            .map(existing -> updateExisting(existing, item))
            // 없으면 새로 저장
            .orElse(createNew(item));

        indexInfoRepository.save(indexInfo);
    }


    //객체 저장 로직
    private IndexInfo createNew(ApiResponse.StockIndexItem item) {
        return new IndexInfo(
            item.getIndexClassification(),
            item.getIndexName(),
            parseInteger(item.getEmployedItemsCount()),
            parseDate(item.getBasePointTime()),
            parseBigDecimal(item.getBaseIndex()),
            SourceType.OPEN_API,
            false
        );
    }


    // 업데이트 로직
    private IndexInfo updateExisting(IndexInfo existing, ApiResponse.StockIndexItem item) {
        existing.updateEmployedItemsCount(parseInteger(item.getEmployedItemsCount()));
        existing.updateBaseIndex(parseBigDecimal(item.getBaseIndex()));
        existing.updateBasePointInTime(parseDate(item.getBasePointTime()));
        return existing;
    }



    // 문자열을 LocalDate로 파싱 (yyyyMMdd 형식)
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            log.warn("기준시점이 비어있습니다. 현재 날짜를 사용합니다.");
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}, 현재 날짜 사용", dateString);
            return LocalDate.now();
        }
    }

    // 문자열을 BigDecimal로 파싱
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty() || "-".equals(value.trim())) {
            return null;
        }
        try {
            // 콤마, 공백 제거 후 변환
            return new BigDecimal(value.replaceAll(",", "").trim());
        } catch (NumberFormatException e) {
            log.warn("BigDecimal 파싱 실패: {}, null 반환", value);
            return null;
        }
    }

    // 문자열을 Integer로 파싱
    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty() || "-".equals(value.trim())) {
            return 0;
        }
        try {
            return Integer.parseInt(value.replaceAll(",", "").trim());
        } catch (NumberFormatException e) {
            log.warn("정수 파싱 실패: {}, 기본값 0 사용", value);
            return 0;
        }
    }

    // 모든 지수 정보 조회 (컨트롤러의 findAll 호출에 사용)
    @Transactional(readOnly = true)
    public List<IndexInfo> getAllIndexInfo() {
        return indexInfoRepository.findAll();
    }
}