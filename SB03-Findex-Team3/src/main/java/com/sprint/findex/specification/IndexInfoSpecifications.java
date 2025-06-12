package com.sprint.findex.specification;

import com.sprint.findex.dto.response.IndexInfoSearchDto;
import com.sprint.findex.dto.response.ResponseCursorDto;
import com.sprint.findex.entity.IndexInfo;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class IndexInfoSpecifications {

    public static Specification<IndexInfo> withFilters(ResponseCursorDto responseCursorDto, IndexInfoSearchDto searchDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasValue(searchDto.indexClassification())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("indexClassification")),
                    "%" + searchDto.indexClassification().toLowerCase() + "%"
                ));
            }

            if (hasValue(searchDto.indexName())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("indexName")),
                    "%" + searchDto.indexName().toLowerCase() + "%"
                ));
            }

            if (searchDto.favorite() != null) {
                predicates.add(criteriaBuilder.equal(root.get("favorite"), searchDto.favorite()));
            }

            if (responseCursorDto != null){
                addCursorConditions(searchDto, responseCursorDto, root, criteriaBuilder, predicates);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addCursorConditions(IndexInfoSearchDto searchDto,
        ResponseCursorDto responseCursorDto,
        Root<IndexInfo> root,
        CriteriaBuilder criteriaBuilder,
        List<Predicate> predicates) {
        Predicate cursorPredicate = createCursorPredicate(searchDto, responseCursorDto, root, criteriaBuilder);

        if (cursorPredicate != null) {
            predicates.add(cursorPredicate);
        }
    }

    private static Predicate createCursorPredicate(IndexInfoSearchDto searchDto,
        ResponseCursorDto responseCursorDto,
        Root<IndexInfo> root,
        CriteriaBuilder criteriaBuilder) {

        String sortField = searchDto.sortField();
        boolean isAsc = "asc".equalsIgnoreCase(searchDto.sortDirection());

        return switch (sortField) {
            case "indexClassification" -> {
                String cursorValue = responseCursorDto.indexClassification();
                if (cursorValue == null || cursorValue.isEmpty()) {
                    yield criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id());
                }
                yield isAsc
                    ? criteriaBuilder.or(
                    criteriaBuilder.greaterThan(root.get("indexClassification"), cursorValue),
                    criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("indexClassification"), cursorValue),
                        criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id())
                    )
                )
                    : criteriaBuilder.or(
                        criteriaBuilder.lessThan(root.get("indexClassification"), cursorValue),
                        criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("indexClassification"), cursorValue),
                            criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id())
                        )
                    );
            }
            case "indexName" -> {
                String cursorValue = responseCursorDto.indexName();
                if (cursorValue == null || cursorValue.isEmpty()) {
                    yield criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id());
                }
                yield isAsc
                    ? criteriaBuilder.or(
                    criteriaBuilder.greaterThan(root.get("indexName"),
                        responseCursorDto.indexName()),
                    criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("indexName"), responseCursorDto.indexName()),
                        criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id())
                    )
                )
                    : criteriaBuilder.or(
                        criteriaBuilder.lessThan(root.get("indexName"), cursorValue),
                        criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("indexName"), cursorValue),
                            criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id())
                        )
                    );
            }
            case "employedItemsCount" -> {
                Integer cursorValue = responseCursorDto.employedItemsCount();
                if (cursorValue == null) {
                    yield criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id());
                }
                yield isAsc
                    ? criteriaBuilder.or(
                    criteriaBuilder.greaterThan(root.get("employedItemsCount"), cursorValue),
                    criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("employedItemsCount"), cursorValue),
                        criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id())
                    )
                )
                    : criteriaBuilder.or(
                        criteriaBuilder.lessThan(root.get("employedItemsCount"), cursorValue),
                        criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("employedItemsCount"), cursorValue),
                            criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id())
                        )
                    );
            }
            default -> criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id());

        };
    }


    private static boolean hasValue(String str) {
        return str != null && !str.isBlank();
    }

}