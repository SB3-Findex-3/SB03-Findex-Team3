package com.sprint.findex.specification;

import com.sprint.findex.dto.response.ResponseSyncJobCursorDto;
import com.sprint.findex.dto.request.SyncJobQueryParams;
import com.sprint.findex.entity.SyncJob;
import com.sprint.findex.entity.SyncJobType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
public class SyncJobSpecifications {

    public static Specification<SyncJob> withFilters(ResponseSyncJobCursorDto decodedCursor, SyncJobQueryParams params){

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (params.jobType() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("jobType"), params.jobType()));
            }

            if (params.jobType() == null && "targetDate".equals(params.sortField())) {
                predicates.add(criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("jobType"), SyncJobType.INDEX_DATA),
                    criteriaBuilder.isNotNull(root.get("targetDate"))
                ));
            }

            if (params.indexInfoId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("indexInfo").get("id"), params.indexInfoId()));
            }

            if (params.baseDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("targetDate"), params.baseDateTo()));
            }

            if (params.baseDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("targetDate"), params.baseDateFrom()));
            }

            if (hasValue(params.worker())) {
                predicates.add(criteriaBuilder.equal(root.get("worker"), params.worker()));
            }

            if (params.jobTimeTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("jobTime"), params.jobTimeTo()));
            }
            if (params.jobTimeFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("jobTime"), params.jobTimeFrom()));
            }

            if (params.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("result"), params.status()));
            }

            if (decodedCursor != null){
                addCursorConditions(params, decodedCursor, root, criteriaBuilder, predicates);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addCursorConditions(SyncJobQueryParams params,
        ResponseSyncJobCursorDto decodedCursor,
        Root<SyncJob> root,
        CriteriaBuilder criteriaBuilder,
        List<Predicate> predicates) {

        Predicate cursorPredicate = createCursorPredicate(params, decodedCursor, root, criteriaBuilder);

        if (cursorPredicate != null) {
            predicates.add(cursorPredicate);
        }

    }

    private static Predicate createCursorPredicate(SyncJobQueryParams params,
        ResponseSyncJobCursorDto decodedCursor,
        Root<SyncJob> root,
        CriteriaBuilder criteriaBuilder) {

        String sortField = params.sortField();
        List<Predicate> predicates = new ArrayList<>();
        boolean isAsc = "asc".equalsIgnoreCase(params.sortDirection());

        LocalDate cursorDate = decodedCursor.targetDate();

        return switch (sortField) {
            case "targetDate" -> {
                 try {
                     if (cursorDate == null) {
                         if (params.jobType() == SyncJobType.INDEX_INFO){
                             yield criteriaBuilder.greaterThan(root.get("id"), decodedCursor.id());
                         }
                         if (params.jobType() == null){
                             yield criteriaBuilder.and(
                                 criteriaBuilder.equal(root.get("jobType"), SyncJobType.INDEX_DATA),
                                 criteriaBuilder.isNotNull(root.get("targetDate"))
                             );
                         }
                         if (params.jobType() == SyncJobType.INDEX_DATA) {
                             yield criteriaBuilder.greaterThan(root.get("id"), decodedCursor.id());
                         }
                     }

                     yield isAsc
                         ? criteriaBuilder.or(
                         criteriaBuilder.greaterThan(root.get("targetDate"), cursorDate),
                         criteriaBuilder.and(
                             criteriaBuilder.equal(root.get("targetDate"), cursorDate),
                             criteriaBuilder.greaterThan(root.get("id"), decodedCursor.id())
                         )
                     )
                         : criteriaBuilder.or(
                             criteriaBuilder.lessThan(root.get("targetDate"), cursorDate),
                             criteriaBuilder.and(
                                 criteriaBuilder.equal(root.get("targetDate"), cursorDate),
                                 criteriaBuilder.greaterThan(root.get("id"), decodedCursor.id())
                             )
                         );
                 } catch (Exception e) {
                     yield null;
                 }

            }
            case "jobTime" -> {
                try {
                    OffsetDateTime cursorDateTime =  decodedCursor.jobTime();

                    yield isAsc
                        ? criteriaBuilder.or(
                        criteriaBuilder.greaterThan(root.get("jobTime"), cursorDateTime),
                        criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("jobTime"), cursorDateTime),
                            criteriaBuilder.greaterThan(root.get("id"), decodedCursor.id())
                        )
                    )
                        : criteriaBuilder.or(
                            criteriaBuilder.lessThan(root.get("jobTime"), cursorDateTime),
                            criteriaBuilder.and(
                                criteriaBuilder.equal(root.get("jobTime"), cursorDateTime),
                                criteriaBuilder.greaterThan(root.get("id"), decodedCursor.id())
                            )
                        );
                } catch (Exception e) {
                    yield null;
                }
            }
            default -> {
                log.warn("Specification: 지원하지 않는 정렬 필드: {}", sortField);
                yield null;
            }
        };
    }

    private static boolean hasValue(String str) {
        return str != null && !str.isBlank();
    }

}
