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

            if (params.cursor() != null && params.idAfter() != null && params.sortField() != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();

                    // cursor 디코딩
                    String decodedCursorJson = new String(Base64.getDecoder().decode(params.cursor()), StandardCharsets.UTF_8);
                    Map<String, Object> cursorMap = mapper.readValue(decodedCursorJson, Map.class);
                    Object cursorValue = cursorMap.get("value");

                    // idAfter 디코딩
                    String decodedIdJson = new String(Base64.getDecoder().decode(params.idAfter()), StandardCharsets.UTF_8);
                    Map<String, Object> idMap = mapper.readValue(decodedIdJson, Map.class);
                    Long idAfter = Optional.ofNullable(idMap.get("id"))
                        .map(Object::toString)
                        .map(Long::valueOf)
                        .orElseThrow(() -> new IllegalArgumentException("❌ idAfter 디코딩 실패"));

                    String sortField = params.sortField();
                    boolean isAsc = "asc".equalsIgnoreCase(params.sortDirection());

                    Predicate compound = buildCursorPredicate(cb, root, sortField, cursorValue, idAfter, isAsc);
                    if (compound != null) {
                        predicates.add(compound);
                    }

                } catch (Exception e) {
                    log.error("❌ 커서 디코딩 또는 조건 생성 실패", e);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate buildCursorPredicate(CriteriaBuilder cb,
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
                log.warn("⚠️ 지원하지 않는 정렬 필드: {}", sortField);
                return null;
            }
        }

        Path<Long> idPath = root.get("id");

        Predicate mainSortPredicate = isAsc
            ? cb.greaterThan((Path<Comparable>) sortPath, (Comparable) cursorValue)
            : cb.lessThan((Path<Comparable>) sortPath, (Comparable) cursorValue);

        Predicate tieBreaker = isAsc
            ? cb.and(cb.equal(sortPath, cursorValue), cb.greaterThan(idPath, idAfter))
            : cb.and(cb.equal(sortPath, cursorValue), cb.lessThan(idPath, idAfter));

        return cb.or(mainSortPredicate, tieBreaker);
    }
}
