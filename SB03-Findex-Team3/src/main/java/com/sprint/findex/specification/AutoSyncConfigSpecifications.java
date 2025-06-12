package com.sprint.findex.specification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.dto.request.AutoSyncQueryParams;
import com.sprint.findex.entity.AutoSyncConfig;
import com.sprint.findex.entity.IndexInfo;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class AutoSyncConfigSpecifications {

    private static final Logger log = LoggerFactory.getLogger(AutoSyncConfigSpecifications.class);

    public static Specification<AutoSyncConfig> withFilters(AutoSyncQueryParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.indexInfoId() != null) {
                predicates.add(cb.equal(root.get("indexInfo").get("id"), params.indexInfoId()));
            }

            if (params.enabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), params.enabled()));
            }

            // sortField 기본값 추가
            String sortField = (params.sortField() != null) ? params.sortField() : "indexInfo.indexName";

            // sortDirection 기본값 추가
            String sortDirection = (params.sortDirection() != null) ? params.sortDirection() : "ASC";

            log.info("[AutoSyncSpec] cursor: {}", params.cursor());
            log.info("[AutoSyncSpec] idAfter: {}", params.idAfter());
            log.info("[AutoSyncSpec] sortField (resolved): {}", sortField);

            if (params.cursor() != null && params.idAfter() != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();

                    String decodedCursorJson = new String(Base64.getDecoder().decode(params.cursor()), StandardCharsets.UTF_8);
                    Map<String, Object> cursorMap = mapper.readValue(decodedCursorJson, Map.class);
                    Object cursorValue = cursorMap.get("value");
                    log.info("[AutoSyncSpec] Decoded cursor value: {}", cursorValue);

                    String decodedIdJson = new String(Base64.getDecoder().decode(params.idAfter().toString()), StandardCharsets.UTF_8);
                    Map<String, Object> idMap = mapper.readValue(decodedIdJson, Map.class);
                    Long idAfter = Optional.ofNullable(idMap.get("id"))
                            .map(Object::toString)
                            .map(Long::valueOf)
                            .orElseThrow(() -> new IllegalArgumentException("idAfter decoding failed"));
                    log.info("[AutoSyncSpec] Decoded idAfter: {}", idAfter);

                    // 수정 부분
                    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
                    //

                    Predicate compound = buildCursorPredicate(cb, root, sortField, cursorValue, idAfter, isAsc);
                    if (compound != null) {
                        predicates.add(compound);
                        log.info("[AutoSyncSpec] Cursor predicate added");
                    } else {
                        log.warn("[AutoSyncSpec] No predicate built for sortField: {}", sortField);
                    }
                } catch (Exception e) {
                    log.error("[AutoSyncSpec] Cursor decode or predicate creation failed", e);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate buildCursorPredicate(
            CriteriaBuilder cb,
            Root<AutoSyncConfig> root,
            String sortField,
            Object cursorValue,
            Long idAfter,
            boolean isAsc) {

        Path<?> sortPath;
        Join<AutoSyncConfig, IndexInfo> indexInfoJoin = null;

        switch (sortField) {
            case "indexInfo.indexName" -> {
                indexInfoJoin = root.join("indexInfo", JoinType.INNER);
                sortPath = indexInfoJoin.get("indexName");
            }
            case "enabled" -> sortPath = root.get("enabled");
            default -> {
                log.warn("[AutoSyncSpec] Unsupported sortField: {}", sortField);
                return null;
            }
        }

        Path<Long> idPath = root.get("id");

        Predicate mainPredicate = isAsc
                ? cb.greaterThan((Path<Comparable>) sortPath, (Comparable) cursorValue)
                : cb.lessThan((Path<Comparable>) sortPath, (Comparable) cursorValue);

        Predicate tieBreaker = isAsc
                ? cb.and(cb.equal(sortPath, cursorValue), cb.lessThan(idPath, idAfter))
                : cb.and(cb.equal(sortPath, cursorValue), cb.greaterThan(idPath, idAfter));

        log.info("[AutoSyncSpec] Cursor conditions: main={}, tieBreaker={}", mainPredicate, tieBreaker);

        return cb.or(mainPredicate, tieBreaker);
    }
}
