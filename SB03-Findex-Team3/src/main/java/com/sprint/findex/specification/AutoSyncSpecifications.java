package com.sprint.findex.specification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.dto.request.AutoSyncConfigQueryParams;
import com.sprint.findex.entity.AutoSyncConfig;
import com.sprint.findex.entity.IndexInfo;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class AutoSyncSpecifications {

    private static final Logger log = LoggerFactory.getLogger(AutoSyncSpecifications.class);

    public static Specification<AutoSyncConfig> withFilters(AutoSyncConfigQueryParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.indexInfoId() != null) {
                predicates.add(cb.equal(root.get("indexInfo").get("id"), params.indexInfoId()));
            }

            if (params.enabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), params.enabled()));
            }


            log.info("[withFilters] cursor: {}", params.cursor());
            log.info("[withFilters] idAfter: {}", params.idAfter());
            log.info("[withFilters] sortField: {}", params.sortField());

            // 정렬 필드 및 방향 설정
            String sortField = Optional.ofNullable(params.sortField()).orElse("indexInfo.indexName");
            String sortDirection = Optional.ofNullable(params.sortDirection()).orElse("ASC");


            if (params.cursor() != null && params.idAfter() != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();

                    // cursor 디코딩
                    String decodedCursorJson = new String(Base64.getDecoder().decode(params.cursor()), StandardCharsets.UTF_8);
                    log.info("decoded cursor JSON: {}", decodedCursorJson);
                    Map<String, Object> cursorMap = mapper.readValue(decodedCursorJson, Map.class);
                    Object cursorValue = cursorMap.get("value");

                    // idAfter 디코딩
                    String decodedIdJson = new String(Base64.getDecoder().decode(params.idAfter().toString()), StandardCharsets.UTF_8);
                    log.info("decoded idAfter JSON: {}", decodedIdJson);
                    Map<String, Object> idMap = mapper.readValue(decodedIdJson, Map.class);
                    Long idAfter = Optional.ofNullable(idMap.get("id"))
                        .map(Object::toString)
                        .map(Long::valueOf)
                        .orElseThrow(() -> new IllegalArgumentException("idAfter decoding failed"));

                    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

                    // 커서 기반 조건 생성 및 추가
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
            case "enabled" -> {
                sortPath = root.get("enabled");
            }
            default -> {
                log.warn("지원하지 않는 정렬 필드: {}", sortField);
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

        return cb.or(mainPredicate, tieBreaker);
    }
}
