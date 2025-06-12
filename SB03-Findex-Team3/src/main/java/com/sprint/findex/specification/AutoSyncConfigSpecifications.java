package com.sprint.findex.specification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.dto.request.AutoSyncQueryParams;
import com.sprint.findex.entity.AutoSyncConfig;
import com.sprint.findex.entity.IndexInfo;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
public class AutoSyncConfigSpecifications {

    private static final String DEFAULT_SORT_FIELD = "indexInfo.indexName";
    private static final String DEFAULT_SORT_DIRECTION = "asc";
    private static final String SORT_INDEX_NAME = "indexInfo.indexName";
    private static final String SORT_ENABLED = "enabled";

    public static Specification<AutoSyncConfig> withFilters(AutoSyncQueryParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (params.indexInfoId() != null) {
                predicates.add(cb.equal(root.get("indexInfo").get("id"), params.indexInfoId()));
            }

            if (params.enabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), params.enabled()));
            }

            String sortField = getOrDefault(params.sortField(), DEFAULT_SORT_FIELD);
            String sortDirection = getOrDefault(params.sortDirection(), DEFAULT_SORT_DIRECTION);
            boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

            log.info("[withFilters] 받은 cursor 파라미터: {}", params.cursor());
            log.info("[withFilters] 받은 idAfter 파라미터: {}", params.idAfter());
            log.info("[withFilters] 받은 sortField: {}", sortField);

            if (params.cursor() != null && params.idAfter() != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();

                    String decodedCursorJson = new String(Base64.getDecoder().decode(params.cursor()), StandardCharsets.UTF_8);
                    log.info("decoded cursor JSON: {}", decodedCursorJson);
                    Map<String, Object> cursorMap = mapper.readValue(decodedCursorJson, Map.class);
                    Object cursorValue = cursorMap.get("value");

                    String decodedIdJson = new String(Base64.getDecoder().decode(params.idAfter().toString()), StandardCharsets.UTF_8);
                    log.info("decoded idAfter JSON: {}", decodedIdJson);
                    Map<String, Object> idMap = mapper.readValue(decodedIdJson, Map.class);
                    Long idAfter = Optional.ofNullable(idMap.get("id"))
                        .map(Object::toString)
                        .map(Long::valueOf)
                        .orElseThrow(() -> new IllegalArgumentException("idAfter 디코딩 실패"));

                    Predicate compound = buildCursorPredicate(cb, root, sortField, cursorValue, idAfter, isAsc);
                    if (compound != null) {
                        predicates.add(compound);
                    }
                } catch (Exception e) {
                    log.error("커서 디코딩 또는 조건 생성 실패", e);
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
            case SORT_INDEX_NAME -> {
                indexInfoJoin = root.join("indexInfo", JoinType.INNER);
                sortPath = indexInfoJoin.get("indexName");
            }
            case SORT_ENABLED -> {
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
            ? cb.and(cb.equal(sortPath, cursorValue), cb.greaterThan(idPath, idAfter))
            : cb.and(cb.equal(sortPath, cursorValue), cb.lessThan(idPath, idAfter));

        return cb.or(mainPredicate, tieBreaker);
    }

    private static String getOrDefault(String value, String defaultValue) {
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
