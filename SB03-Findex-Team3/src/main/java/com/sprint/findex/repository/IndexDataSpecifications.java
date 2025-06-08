package com.sprint.findex.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.dto.request.IndexDataQueryParams;
import com.sprint.findex.entity.IndexData;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IndexDataSpecifications {

    private static final Logger log = LoggerFactory.getLogger(IndexDataSpecifications.class);

    /**
     * 동적 쿼리 생성을 위한 Specification 생성 메서드
     */
    public static Specification<IndexData> withFilters(IndexDataQueryParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 지수 ID 필터
            if (params.indexInfoId() != null) {
                predicates.add(cb.equal(root.get("indexInfo").get("id"), params.indexInfoId()));
            }

            if (params.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("baseDate"), params.startDate()));
            }
            if (params.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("baseDate"), params.endDate()));
            }

            if (params.cursor() != null && params.idAfter() != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();

                    String decodedCursorJson = new String(Base64.getDecoder().decode(params.cursor()), StandardCharsets.UTF_8);
                    Map<String, Object> cursorMap = mapper.readValue(decodedCursorJson, Map.class);
                    Object cursorValue = cursorMap.get("value");

                    String decodedIdJson = new String(Base64.getDecoder().decode(params.idAfter()), StandardCharsets.UTF_8);
                    Map<String, Object> idMap = mapper.readValue(decodedIdJson, Map.class);
                    Long idAfter = Long.valueOf(idMap.get("id").toString());

                    String sortField = params.sortField() != null ? params.sortField() : "baseDate";
                    Sort.Direction direction = "asc".equalsIgnoreCase(params.sortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;

                    Predicate fieldGt = direction == Sort.Direction.ASC
                        ? cb.greaterThan(root.get(sortField), (Comparable) cursorValue)
                        : cb.lessThan(root.get(sortField), (Comparable) cursorValue);

                    Predicate fieldEq = cb.equal(root.get(sortField), cursorValue);
                    Predicate idGt = cb.greaterThan(root.get("id"), idAfter);
                    Predicate compound = cb.or(fieldGt, cb.and(fieldEq, idGt));

                    predicates.add(compound);
                } catch (Exception e) {
                    log.error("❌ 커서 디코딩 또는 조건 생성 실패", e);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 커서 조건이 유효한지 확인하는 메서드
     */
    private static boolean hasCursorConditions(IndexDataQueryParams params) {
        return params.cursor() != null &&
            !params.cursor().isBlank() &&
            params.idAfter() != null &&
            params.sortField() != null;
    }

    /**
     * 커서 조건에 맞는 Predicate 생성
     */
    private static Predicate createCursorPredicate(IndexDataQueryParams params,
        Root<IndexData> root,
        CriteriaBuilder cb) {

        String sortField = params.sortField();
        String cursor = params.cursor();
        String idAfter = params.idAfter();
        boolean isAsc = "asc".equalsIgnoreCase(params.sortDirection());

        // ✅ sourceType 정렬 금지
        if ("sourceType".equals(sortField)) {
            log.warn("⚠️ 소스 타입(sourceType)은 정렬 조건으로 사용할 수 없습니다.");
            return null;
        }

        logCursorInfo(sortField, cursor, idAfter, isAsc);

        try {
            return switch (sortField) {
                // baseDate는 날짜이므로 LocalDate로 파싱
                case "baseDate" -> {
                    LocalDate cursorDate = LocalDate.parse(cursor);
                    yield buildCursorPredicate(cb, root, "baseDate", cursorDate, idAfter, isAsc);
                }
                // 숫자 필드인 경우 Double로 파싱
                case "closingPrice", "marketPrice", "highPrice", "lowPrice",
                     "versus", "fluctuationRate", "tradingQuantity",
                     "tradingPrice", "marketTotalAmount" -> {
                    Double cursorValue = Double.parseDouble(cursor);
                    yield buildCursorPredicate(cb, root, sortField, cursorValue, idAfter, isAsc);
                }
                // 지원하지 않는 필드인 경우 null 반환
                default -> {
                    log.warn("⚠️ 지원하지 않는 정렬 필드: {}", sortField);
                    yield null;
                }
            };
        } catch (Exception e) {
            // 커서 파싱 오류 로그 출력
            log.error("❌ 커서 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 커서 조건 관련 로그 출력
     */
    private static void logCursorInfo(String sortField, String cursor, String idAfter, boolean isAsc) {
        log.debug("📌 [Cursor 조건 정보]");
        log.debug("   • 정렬 필드   : {}", sortField);
        log.debug("   • 커서 값     : {}", cursor);
        log.debug("   • 마지막 ID   : {}", idAfter);
        log.debug("   • 정렬 방향   : {}", isAsc ? "ASC" : "DESC");
    }

    /**
     * 공통 커서 조건 처리
     * - 커서 기준 필드값 비교
     * - 동일 값인 경우 ID로 추가 정렬 (tie-breaker)
     */
    private static <T extends Comparable<T>> Predicate buildCursorPredicate(
        CriteriaBuilder cb,
        Root<IndexData> root,
        String sortField,
        T cursorValue,
        String idAfterStr,
        boolean isAsc
    ) {
        Path<T> sortPath = root.get(sortField);
        Path<Long> idPath = root.get("id");

        Predicate mainSortPredicate = isAsc
            ? cb.greaterThan(sortPath, cursorValue)
            : cb.lessThan(sortPath, cursorValue);

        log.debug("🧩 메인 정렬 조건: {} {} {}", sortField, isAsc ? ">" : "<", cursorValue);

        if (idAfterStr != null) {
            Long idAfter = Long.parseLong(idAfterStr); 

            Predicate tieBreaker = isAsc
                ? cb.and(cb.equal(sortPath, cursorValue), cb.greaterThan(idPath, idAfter))
                : cb.and(cb.equal(sortPath, cursorValue), cb.lessThan(idPath, idAfter));

            log.debug("🧩 Tie-breaker 조건: {} == {}, 그리고 id {} {}", sortField, cursorValue,
                isAsc ? ">" : "<", idAfter);

            return cb.or(mainSortPredicate, tieBreaker);
        }

        return mainSortPredicate;
    }


}
