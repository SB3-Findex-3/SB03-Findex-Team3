package com.sprint.findex.specification;

import com.sprint.findex.dto.IndexInfoSearchDto;
import com.sprint.findex.entity.IndexInfo;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class IndexInfoSpecifications {

    public static Specification<IndexInfo> withFilters(IndexInfoSearchDto searchDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 지수 분류명 부분 일치 (대소문자 구분 없음)
            if (hasValue(searchDto.indexClassification())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("indexClassification")),
                    "%" + searchDto.indexClassification().toLowerCase() + "%"
                ));
            }

            // 지수명 부분 일치 (대소문자 구분 없음)
            if (hasValue(searchDto.indexName())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("indexName")),
                    "%" + searchDto.indexName().toLowerCase() + "%"
                ));
            }

            // 즐겨찾기 완전 일치
            if (searchDto.favorite() != null) {
                predicates.add(criteriaBuilder.equal(root.get("favorite"), searchDto.favorite()));
            }

            // 커서 조건 추가 (복잡한 조건 또는 단순 조건)
            addCursorConditions(searchDto, root, criteriaBuilder, predicates);

            // 모든 조건을 AND로 결합
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


    // 커서 조건
    private static void addCursorConditions(IndexInfoSearchDto searchDto,
        Root<IndexInfo> root,
        CriteriaBuilder criteriaBuilder,
        List<Predicate> predicates) {

        // 단순 ID 커서만 있는 경우 (간단하게 조회하기)
        if (searchDto.idAfter() != null && !hasValue(searchDto.cursor())) {
            predicates.add(criteriaBuilder.greaterThan(root.get("id"), searchDto.idAfter()));
            return;
        }

        // 복잡한 정렬 조건을 가지는 경우
        if (hasValue(searchDto.cursor()) && searchDto.idAfter() != null) {
            Predicate cursorPredicate = createCursorPredicate(searchDto, root, criteriaBuilder);
            if (cursorPredicate != null) {
                predicates.add(cursorPredicate);
            }
        }
    }


    // 정렬 필드가 있을 때 커서 생성
    private static Predicate createCursorPredicate(IndexInfoSearchDto searchDto,
        Root<IndexInfo> root,
        CriteriaBuilder criteriaBuilder) {
        String sortField = searchDto.sortField();
        String cursor = searchDto.cursor();
        Long idAfter = searchDto.idAfter();
        boolean isAsc = "asc".equalsIgnoreCase(searchDto.sortDirection());

        return switch (sortField) {
            case "indexClassification" -> isAsc
                ? criteriaBuilder.or(
                criteriaBuilder.greaterThan(root.get("indexClassification"), cursor),
                criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("indexClassification"), cursor),
                    criteriaBuilder.greaterThan(root.get("id"), idAfter)
                )
            )
                : criteriaBuilder.or(
                    criteriaBuilder.lessThan(root.get("indexClassification"), cursor),
                    criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("indexClassification"), cursor),
                        criteriaBuilder.greaterThan(root.get("id"), idAfter)
                    )
                );

            case "indexName" -> isAsc
                ? criteriaBuilder.or(
                criteriaBuilder.greaterThan(root.get("indexName"), cursor),
                criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("indexName"), cursor),
                    criteriaBuilder.greaterThan(root.get("id"), idAfter)
                )
            )
                : criteriaBuilder.or(
                    criteriaBuilder.lessThan(root.get("indexName"), cursor),
                    criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("indexName"), cursor),
                        criteriaBuilder.greaterThan(root.get("id"), idAfter)
                    )
                );

            case "employedItemsCount" -> {
                try {
                    Integer cursorValue = Integer.parseInt(cursor);
                    yield isAsc
                        ? criteriaBuilder.or(
                        criteriaBuilder.greaterThan(root.get("employedItemsCount"), cursorValue),
                        criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("employedItemsCount"), cursorValue),
                            criteriaBuilder.greaterThan(root.get("id"), idAfter)
                        )
                    )
                        : criteriaBuilder.or(
                            criteriaBuilder.lessThan(root.get("employedItemsCount"), cursorValue),
                            criteriaBuilder.and(
                                criteriaBuilder.equal(root.get("employedItemsCount"), cursorValue),
                                criteriaBuilder.greaterThan(root.get("id"), idAfter)
                            )
                        );
                } catch (NumberFormatException e) {
                    // 잘못된 커서 값은 무시
                    yield null;
                }
            }

            default -> null;
        };
    }
     // 문자열 값이 있는지 확인 (조회 조건 있는지?)
    private static boolean hasValue(String str) {
        return str != null && !str.isBlank();
    }




    // 개별 조건
    public static Specification<IndexInfo> hasIndexClassification(String indexClassification) {
        return (root, query, criteriaBuilder) -> {
            if (!hasValue(indexClassification)) {
                return criteriaBuilder.conjunction(); // 항상 true
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("indexClassification")),
                "%" + indexClassification.toLowerCase() + "%"
            );
        };
    }


    public static Specification<IndexInfo> hasIndexName(String indexName) {
        return (root, query, criteriaBuilder) -> {
            if (!hasValue(indexName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("indexName")),
                "%" + indexName.toLowerCase() + "%"
            );
        };
    }


    public static Specification<IndexInfo> hasFavorite(Boolean favorite) {
        return (root, query, criteriaBuilder) -> {
            if (favorite == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("favorite"), favorite);
        };
    }


    public static Specification<IndexInfo> afterId(Long idAfter) {
        return (root, query, criteriaBuilder) -> {
            if (idAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThan(root.get("id"), idAfter);
        };
    }
}