//package com.sprint.findex.service;
//
//import com.sprint.findex.entity.IndexInfo;
//import com.sprint.findex.entity.SourceType;
//import com.sprint.findex.global.dto.ApiResponse;
//import com.sprint.findex.repository.IndexInfoRepository;
//import jakarta.annotation.PostConstruct;
//import java.net.URI;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//import java.util.NoSuchElementException;
//import java.util.Optional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;
//
//
//@Slf4j
//@Service
//@Transactional
//@RequiredArgsConstructor
//public class OpenApiService {
//
//    private final IndexInfoRepository indexInfoRepository;
//    private final RestTemplate restTemplate;
//
//    // application.yaml에서 직접 값 읽어오기
//    @Value("${api.data.service-key}")
//    private String serviceKey;
//
//    @Value("${api.data.base-url}")
//    private String baseUrl;
//
//    @PostConstruct
//    public void checkRestTemplate() {
//        System.out.println("RestTemplate 주입 상태: " + (restTemplate != null ? "성공" : "실패"));
//        if (restTemplate != null) {
//            System.out.println("RestTemplate 클래스: " + restTemplate.getClass().getName());
//        }
//    }
//
//    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
//
//    public void fetchAndSaveIndexInfo() {
//        log.info("지수 정보 동기화 시작");
//
//        try {
//            String url = buildApiUrl(2, 15); // 일단 페이지 번호는 1, 값은 10개 불러오는 걸로 설정함
//            ApiResponse response = callApi(url);
//
//            if (response == null || response.getBody() == null) {
//                log.error("API 응답이 비어있습니다.");
//                return;
//            }
//
//            if (response.getHeader() != null && !"00".equals(response.getHeader().getResultCode())) {
//                log.error("API 호출 오류: {} - {}",
//                    response.getHeader().getResultCode(),
//                    response.getHeader().getResultMsg());
//                return;
//            }
//
//            if (response.getBody().getItems() == null ||
//                response.getBody().getItems().getItem() == null ||
//                response.getBody().getItems().getItem().isEmpty()) {
//                log.warn("API에서 반환된 데이터가 없습니다.");
//                return;
//            }
//
//            List<ApiResponse.StockIndexItem> items = response.getBody().getItems().getItem();
//            log.info("API에서 {}개의 지수 정보를 받았습니다.", items.size());
//
//            // 각 지수 정보 처리
//            int savedCount = 0;
//            int updatedCount = 0;
//            int failCount = 0;
//
//            for (ApiResponse.StockIndexItem item : items) {
//                try {
//                    ProcessResult result = processIndexInfo(item);
//                    if (result == ProcessResult.SAVED) {
//                        savedCount++;
//                    } else if (result == ProcessResult.UPDATED) {
//                        updatedCount++;
//                    }
//                } catch (Exception e) {
//                    log.error("지수 정보 처리 실패: {} - {}",
//                        item.getIndexName(), item.getIndexClassification(), e);
//                    failCount++;
//                }
//            }
//
//            log.info("지수 정보 동기화 완료 - 신규 저장: {}, 업데이트: {}, 실패: {}",
//                savedCount, updatedCount, failCount);
//
//        } catch (Exception e) {
//            log.error("지수 정보 동기화 중 오류 발생", e);
//            throw new RuntimeException("지수 정보 동기화 실패", e);
//        }
//    }
//
//    /**
//     * 개별 지수 정보 처리
//     */
//    private ProcessResult processIndexInfo(ApiResponse.StockIndexItem item) {
//        try {
//            // 기존 데이터 확인
//            Optional<IndexInfo> existingInfo = indexInfoRepository
//                .findByIndexClassificationAndIndexName(
//                    item.getIndexClassification(),
//                    item.getIndexName()
//                );
//
//            if (existingInfo.isPresent()) {
//                // 기존 데이터 업데이트
//                IndexInfo indexInfo = existingInfo.get();
//                boolean isUpdated = updateIndexInfoFields(indexInfo, item);
//
//                if (isUpdated) {
//                    indexInfoRepository.save(indexInfo);
//                    log.info("지수 정보 업데이트: {} ({})",
//                        indexInfo.getIndexName(), indexInfo.getIndexClassification());
//                    return ProcessResult.UPDATED;
//                } else {
//                    log.info("지수 정보 변경 없음: {} ({})",
//                        indexInfo.getIndexName(), indexInfo.getIndexClassification());
//                    return ProcessResult.NO_CHANGE;
//                }
//
//            } else {
//                // 새로운 데이터 저장
//                IndexInfo newIndexInfo = createNewIndexInfo(item);
//                indexInfoRepository.save(newIndexInfo);
//                log.info("새로운 지수 정보 저장: {} ({})",
//                    newIndexInfo.getIndexName(), newIndexInfo.getIndexClassification());
//                return ProcessResult.SAVED;
//            }
//
//        } catch (Exception e) {
//            log.error("지수 정보 처리 중 오류: {} - {}",
//                item.getIndexName(), item.getIndexClassification(), e);
//            throw e;
//        }
//    }
//
//    /**
//     * 새로운 IndexInfo 객체 생성
//     */
//    private IndexInfo createNewIndexInfo(ApiResponse.StockIndexItem item) {
//
//        return new IndexInfo(
//            item.getIndexClassification(),
//            item.getIndexName(),
//            parseInteger(item.getEmployedItemsCount()),
//            parseDate(item.getBasePointTime()),
//            parseInteger(item.getBaseIndex()),
//            SourceType.OPEN_API,
//            false );
//    }
//
//    /**
//     * 기존 IndexInfo 필드 업데이트
//     */
//    private boolean updateIndexInfoFields(IndexInfo indexInfo, ApiResponse.StockIndexItem item) {
//        boolean isUpdated = false;
//
//        // 채용종목 수 업데이트
//        Integer newEmployedItemsCount = parseInteger(item.getEmployedItemsCount());
//        if (!newEmployedItemsCount.equals(indexInfo.getEmployedItemsCount())) {
//            indexInfo.updateEmployedItemsCount(newEmployedItemsCount);
//            isUpdated = true;
//        }
//
//        // 기준지수 업데이트
//        Integer newBaseIndex = parseInteger(item.getBaseIndex());
//        if (!newBaseIndex.equals(indexInfo.getBaseIndex())) {
//            indexInfo.updateBaseIndex(newBaseIndex);
//            isUpdated = true;
//        }
//
//        // 기준시점 업데이트 (필요시)
//        LocalDate newBasePointInTime = parseDate(item.getBasePointTime());
//        if (!newBasePointInTime.equals(indexInfo.getBasePointInTime())) {
//            indexInfo.updateBasePointInTime(newBasePointInTime);
//            isUpdated = true;
//        }
//
//        return isUpdated;
//    }
//
//    /**
//     * API URL 생성 - @Value로 받은 설정값 사용
//     */
//    private String buildApiUrl(int pageNo, int numOfRows) {
//        return String.format("%s/getStockMarketIndex?serviceKey=%s&resultType=json&pageNo=%d&numOfRows=%d",
//            baseUrl, serviceKey, pageNo, numOfRows);
//    }
//
//
//    /**
//     * API 호출
//     */
////    private ApiResponse callApi(String url) {
////        try {
////            log.info("API 호출: {}", url.replaceAll("serviceKey=[^&]*", "serviceKey=****"));
////
////            // 원본 응답을 String으로 먼저 받아보기
////            String rawResponse = restTemplate.getForObject(url, String.class);
////            log.info(url);
////            log.info("원본 응답: {}", rawResponse);
////
////            // XML 에러 응답인지 확인
////            if (rawResponse.contains("SERVICE_KEY_IS_NOT_REGISTERED_ERROR")) {
////                throw new RuntimeException("API 키가 등록되지 않았습니다. API 키를 확인해주세요.");
////            }
////
////            // 그 다음 ApiResponse로 변환 시도
////            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);
////            log.info("파싱된 응답: {}", response);
////
////            return response;
////
////        } catch (Exception e) {
////            log.error("API 호출 실패", e);
////            throw new RuntimeException("API 호출 실패", e);
////        }
////    }
//
//
//    private ApiResponse callApi(String url) {
//        try {
//            log.info("API 호출: {}", url.replaceAll("serviceKey=[^&]*", "serviceKey=****"));
//
//            // HTTP 헤더 설정
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("Accept", "*/*;q=0.9"); // HTTP_ERROR 방지
//            HttpEntity<String> httpRequest = new HttpEntity<>(null, headers);
//
//            // URI 클래스로 변환하여 service key is not registered 오류 방지
//            URI uri = new URI(url);
//
//            // 원본 응답을 String으로 먼저 받아보기
//            ResponseEntity<String> rawResponse = restTemplate.exchange(
//                uri, HttpMethod.GET, httpRequest, String.class);
//
//            log.info("원본 응답: {}", rawResponse.getBody());
//
//            // XML 에러 응답인지 확인
//            if (rawResponse.getBody().contains("SERVICE_KEY_IS_NOT_REGISTERED_ERROR")) {
//                throw new RuntimeException("API 키가 등록되지 않았습니다. API 키를 확인해주세요.");
//            }
//
//            // ApiResponse로 변환 시도
//            ResponseEntity<ApiResponse> response = restTemplate.exchange(
//                uri, HttpMethod.GET, httpRequest, ApiResponse.class);
//
//            log.info("파싱된 응답: {}", response.getBody());
//
//            return response.getBody();
//
//        } catch (Exception e) {
//            log.error("API 호출 실패", e);
//            throw new RuntimeException("API 호출 실패: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 문자열을 LocalDate로 파싱 (yyyyMMdd 형식)
//     */
//    private LocalDate parseDate(String dateString) {
//        if (dateString == null || dateString.trim().isEmpty()) {
//            log.warn("기준시점이 비어있습니다. 현재 날짜를 사용합니다.");
//            return LocalDate.now();
//        }
//        try {
//            return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
//        } catch (Exception e) {
//            log.warn("날짜 파싱 실패: {}, 현재 날짜 사용", dateString);
//            return LocalDate.now();
//        }
//    }
//
//    /**
//     * 문자열을 Integer로 파싱
//     */
//    private Integer parseInteger(String value) {
//        if (value == null || value.trim().isEmpty() || "-".equals(value.trim())) {
//            return 0;
//        }
//        try {
//            return Integer.parseInt(value.replaceAll(",", "").trim());
//        } catch (NumberFormatException e) {
//            log.warn("정수 파싱 실패: {}, 기본값 0 사용", value);
//            return 0;
//        }
//    }
//
//    /**
//     * 모든 지수 정보 조회
//     */
//    @Transactional(readOnly = true)
//    public List<IndexInfo> getAllIndexInfo() {
//        return indexInfoRepository.findAll();
//    }
//
//    /**
//     * 지수명으로 조회
//     */
//    @Transactional(readOnly = true)
//    public Optional<IndexInfo> getIndexInfoByName(String indexName) {
//        return indexInfoRepository.findByIndexName(indexName);
//    }
//
//    /**
//     * 즐겨찾기 지수 조회
//     */
//    @Transactional(readOnly = true)
//    public List<IndexInfo> getFavoriteIndexes() {
//        return indexInfoRepository.findByFavoriteTrue();
//    }
//
//    /**
//     * 즐겨찾기 설정/해제
//     */
//    public void toggleFavorite(Long indexInfoId) {
//        IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
//            .orElseThrow(
//                () -> new NoSuchElementException("OpenApiService: 해당하는 지수 정보가 존재하지 않습니다."));
//        indexInfo.updateFavorite(!indexInfo.isFavorite());
//            indexInfoRepository.save(indexInfo);
//            log.info("즐겨찾기 상태 변경: {} - {}",
//                indexInfo.getIndexName(), indexInfo.isFavorite() ? "ON" : "OFF");
//    }
//    /**
//     * 처리 결과 열거형
//     */
//    private enum ProcessResult {
//        SAVED,      // 새로 저장됨
//        UPDATED,    // 업데이트됨
//        NO_CHANGE   // 변경 없음
//    }
//
//
//
//}
