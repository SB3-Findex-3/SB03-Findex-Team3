package com.sprint.findex.specification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.dto.request.IndexDataQueryParams;
import com.sprint.findex.entity.IndexData;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
public class IndexDataSpecifications {

    public static Specification<IndexData> withFilters(IndexDataQueryParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.indexInfoId() != null) {
                predicates.add(cb.equal(root.get("indexInfo").get("id"), params.indexInfoId()));
            }
            if (params.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("baseDate"), params.startDate()));
            }
            if (params.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("baseDate"), params.endDate()));
            }

            if (params.cursor() != null && params.idAfter() != null && params.sortField() != null) {
                Predicate cursorPredicate = createCursorPredicate(params, root, cb);
                if (cursorPredicate != null) {
                    predicates.add(cursorPredicate);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate createCursorPredicate(IndexDataQueryParams params, Root<IndexData> root, CriteriaBuilder cb) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> cursorMap = decodeCursorJson(params.cursor(), mapper);
            Map<String, Object> idMap = decodeCursorJson(params.idAfter(), mapper);

            String sortField = params.sortField();
            boolean isAsc = "asc".equalsIgnoreCase(params.sortDirection());
            Long idAfter = Long.valueOf(idMap.get("value").toString());

            return switch (sortField) {
                case "baseDate" -> {
                    LocalDate cursorVal = LocalDate.parse(cursorMap.get("value").toString());
                    yield buildCursorPredicate(cb, root, "baseDate", cursorVal, idAfter, isAsc);
                }
                case "closingPrice", "marketPrice", "highPrice", "lowPrice",
                     "versus", "fluctuationRate" -> {
                    Double cursorVal = Double.parseDouble(cursorMap.get("value").toString());
                    yield buildCursorPredicate(cb, root, sortField, cursorVal, idAfter, isAsc);
                }
                case "tradingQuantity", "tradingPrice", "marketTotalAmount" -> {
                    Long cursorVal = Long.parseLong(cursorMap.get("value").toString());
                    yield buildCursorPredicate(cb, root, sortField, cursorVal, idAfter, isAsc);
                }
                default -> {
                    log.warn("[IndexDataSpecifications] 지원하지 않는 정렬 필드: {}", sortField);
                    yield cb.greaterThan(root.get("id"), idAfter);
                }
            };
        } catch (Exception e) {
            log.error("[IndexDataSpecifications] 커서 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private static Map<String, Object> decodeCursorJson(String encoded, ObjectMapper mapper) throws Exception {
        String json = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        return mapper.readValue(json, Map.class);
    }

    private static <T extends Comparable<T>> Predicate buildCursorPredicate(
        CriteriaBuilder cb,
        Root<IndexData> root,
        String sortField,
        T cursorValue,
        Long idAfter,
        boolean isAsc
    ) {
        Path<T> sortPath = root.get(sortField);
        Path<Long> idPath = root.get("id");

        Predicate mainSort = isAsc
            ? cb.greaterThan(sortPath, cursorValue)
            : cb.lessThan(sortPath, cursorValue);

        Predicate tieBreaker = isAsc
            ? cb.and(cb.equal(sortPath, cursorValue), cb.greaterThan(idPath, idAfter))
            : cb.and(cb.equal(sortPath, cursorValue), cb.lessThan(idPath, idAfter));

        return cb.or(mainSort, tieBreaker);
    }
}
