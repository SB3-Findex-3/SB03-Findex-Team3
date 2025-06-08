package com.sprint.findex.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.SourceType;
import com.sprint.findex.global.dto.ApiResponse;
import com.sprint.findex.repository.IndexInfoRepository;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;


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


    // WebClient를 사용한 비동기 API 호출 (WebClient는 비동기식을 지원함)
    public Mono<ApiResponse> fetchAndSaveIndexInfoAsync() {
        log.info("지수 정보 비동기 동기화 시작");

        String url = buildApiUrl(1, 10);

        return callApi(url)
            .doOnNext(response -> {
                if (response != null && response.getBody() != null) {
                    processApiResponse(response);
                }
            })
            .doOnError(error -> log.error("비동기 지수 정보 동기화 실패", error));
    }


    // API 응답 처리 로직
    private void processApiResponse(ApiResponse response) {
        if (response.getHeader() != null && !"00".equals(response.getHeader().getResultCode())) {
            log.error("API 호출 오류: {} - {}",
                response.getHeader().getResultCode(),
                response.getHeader().getResultMsg());
            return;
        }

        if (response.getBody().getItems() == null ||
            response.getBody().getItems().getItem() == null ||
            response.getBody().getItems().getItem().isEmpty()) {
            log.warn("API에서 반환된 데이터가 없습니다.");
            return;
        }

        List<ApiResponse.StockIndexItem> items = response.getBody().getItems().getItem();
        log.info("API에서 {}개의 지수 정보를 받았습니다.", items.size());

        items.forEach(this::processIndexInfo);
    }


    // 지수 정보 처리
    private ProcessResult processIndexInfo(ApiResponse.StockIndexItem item) {
        try {
            // 기존 데이터 있는지 확인
            Optional<IndexInfo> existingInfo = indexInfoRepository
                .findByIndexClassificationAndIndexName(
                    item.getIndexClassification(),
                    item.getIndexName()
                );

            if (existingInfo.isPresent()) {
                // 기존 데이터 있으면 업데이트
                IndexInfo indexInfo = existingInfo.get();
                boolean isUpdated = updateIndexInfoFields(indexInfo, item);

                if (isUpdated) {
                    indexInfoRepository.save(indexInfo);
                    log.info("지수 정보 업데이트: {} ({})",
                        indexInfo.getIndexName(), indexInfo.getIndexClassification());
                    return ProcessResult.UPDATED;
                } else {
                    log.info("지수 정보 변경 없음: {} ({})",
                        indexInfo.getIndexName(), indexInfo.getIndexClassification());
                    return ProcessResult.NO_CHANGE;
                }

            } else {
                // 기존 데이터 없으면 새로운 데이터 저장
                IndexInfo newIndexInfo = createNewIndexInfo(item);
                indexInfoRepository.save(newIndexInfo);
                log.info("새로운 지수 정보 저장: {} ({})",
                    newIndexInfo.getIndexName(), newIndexInfo.getIndexClassification());
                return ProcessResult.SAVED;
            }

        } catch (Exception e) {
            log.error("지수 정보 처리 중 오류: {} - {}",
                item.getIndexName(), item.getIndexClassification(), e);
            throw e;
        }
    }


    // 새로운 IndexInfo 객체 생성
    private IndexInfo createNewIndexInfo(ApiResponse.StockIndexItem item) {

        return new IndexInfo(
            item.getIndexClassification(),
            item.getIndexName(),
            parseInteger(item.getEmployedItemsCount()),
            parseDate(item.getBasePointTime()),
            parseInteger(item.getBaseIndex()),
            SourceType.OPEN_API,
            false);
    }


    // 기존 IndexInfo 필드 업데이트
    // (기존 데이터와 값이 동일한지 확인하고 업데이트 하는 메서드)
    private boolean updateIndexInfoFields(IndexInfo indexInfo, ApiResponse.StockIndexItem item) {
        boolean isUpdated = false;

        // 채용종목 수 업데이트
        Integer newEmployedItemsCount = parseInteger(item.getEmployedItemsCount());
        if (!newEmployedItemsCount.equals(indexInfo.getEmployedItemsCount())) {
            indexInfo.updateEmployedItemsCount(newEmployedItemsCount);
            isUpdated = true;
        }

        // 기준지수 업데이트
        Integer newBaseIndex = parseInteger(item.getBaseIndex());
        if (!newBaseIndex.equals(indexInfo.getBaseIndex())) {
            indexInfo.updateBaseIndex(newBaseIndex);
            isUpdated = true;
        }

        // 기준시점 업데이트
        LocalDate newBasePointInTime = parseDate(item.getBasePointTime());
        if (!newBasePointInTime.equals(indexInfo.getBasePointInTime())) {
            indexInfo.updateBasePointInTime(newBasePointInTime);
            isUpdated = true;
        }

        return isUpdated;
    }


    // API URL 생성(@Value로 받은 설정값 사용)
    private String buildApiUrl(int pageNo, int numOfRows) {
        return String.format(
            "%s/getStockMarketIndex?serviceKey=%s&resultType=json&pageNo=%d&numOfRows=%d",
            baseUrl, serviceKey, pageNo, numOfRows);
    }


    // WebClient를 사용한 API 호출 메서드
    private Mono<ApiResponse> callApi(String url) {
        try {
            // URI 변환으로 인증키 오류 해결하기 (자꾸 인증키 없다고 해서 URI 객체로 만들기)
            java.net.URI uri = new java.net.URI(url);
            log.info("API 호출: {}", url.replaceAll("serviceKey=[^&]*", "serviceKey=****"));

            return webClient
                .get()
                .uri(uri)

                // 헤더 설정
                .headers(headers -> {
                    // JSON으로 받아오게 설정 (요청 경로에 JSON으로 지정했는데 XML 오는 경우가 있엇음.. 왜지... /  그래서 추가함)
                    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    headers.set(HttpHeaders.ACCEPT, "*/*;q=0.9"); // HTTP_ERROR 방지
                })

                // 요청 실행 부분
                .retrieve()

                // 에러 응답 처리
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> {
                        log.error("API 호출 HTTP 에러: {} {}", response.statusCode(),
                            response.statusCode());
                        return Mono.error(
                            new RuntimeException("API 호출 HTTP 에러: " + response.statusCode()));
                    }
                )
                // 응답을 문자열로 받게 설정
                .bodyToMono(String.class)
                .doOnNext(rawResponse -> {
                    log.info("원본 응답: {}", rawResponse);

                    // XML 에러 응답 확인
                    if (rawResponse.contains("SERVICE_KEY_IS_NOT_REGISTERED_ERROR")) {
                        throw new RuntimeException("API 키가 등록되지 않았습니다. API 키를 확인해주세요.");
                    }
                })
                // 문자열 응답을 JSON으로 파싱
                .map(this::parseJsonToApiResponse)
                // 타임아웃 설정
                .timeout(Duration.ofSeconds(30))
                // 최대 3회 재시도 하도록 설정 (WebClient의 기능)
                .retry(2)

                // 에러 결과 매핑
                .onErrorMap(WebClientResponseException.class, ex -> {
                    log.error("WebClient 응답 에러: {} - {}", ex.getStatusCode(),
                        ex.getResponseBodyAsString());
                    return new RuntimeException("API 호출 실패: " + ex.getMessage());
                })
                .onErrorMap(Exception.class, ex -> {
                    log.error("API 호출 실패", ex);
                    return new RuntimeException("API 호출 실패: " + ex.getMessage());
                });

        } catch (java.net.URISyntaxException e) {
            log.error("URI 변환 실패: {}", url, e);
            return Mono.error(new RuntimeException("URI 변환 실패: " + e.getMessage()));
        }
    }


    // String으로 들어온 JSON 응답을 파싱해서 JSON으로 변환
    // (원래 String으로 그냥 반환해서 처리했는데 그러니까 데이터 베이스 저장할 때 오류가 발생햇음)
    private ApiResponse parseJsonToApiResponse(String jsonString) {
        try {
            // Jackson ObjectMapper -> JSON과 Java 객체 변환해줌
            ObjectMapper mapper = new ObjectMapper();
            ApiResponse response = mapper.readValue(jsonString, ApiResponse.class);

            // 파싱된 결과 로그
            log.info("파싱된 응답 헤더: {}", response.getHeader());
            log.info("파싱된 응답 바디 아이템 수: {}",
                response.getBody() != null && response.getBody().getItems() != null
                    && response.getBody().getItems().getItem() != null
                    ? response.getBody().getItems().getItem().size() : 0);
            return response;
        } catch (Exception e) {
            log.error("JSON 파싱 실패", e);
            throw new RuntimeException("JSON 파싱 실패: " + e.getMessage());
        }
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


    // 모든 지수 정보 조회
    @Transactional(readOnly = true)
    public List<IndexInfo> getAllIndexInfo() {
        return indexInfoRepository.findAll();
    }


    // 지수명으로 조회
    @Transactional(readOnly = true)
    public Optional<IndexInfo> getIndexInfoByName(String indexName) {
        return indexInfoRepository.findByIndexName(indexName);
    }


    // 즐겨찾기 지수 조회
    @Transactional(readOnly = true)
    public List<IndexInfo> getFavoriteIndexes() {
        return indexInfoRepository.findByFavoriteTrue();
    }

    // 처리 결과 열거
    private enum ProcessResult {
        SAVED,      // 새로 저장됨
        UPDATED,    // 업데이트됨
        NO_CHANGE   // 변경 없음
    }
}