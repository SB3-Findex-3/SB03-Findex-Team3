//package com.sprint.findex.specification;
//
//import com.sprint.findex.dto.ResponseCursorDto;
//import com.sprint.findex.dto.SyncJobSearchDto;
//import com.sprint.findex.entity.SyncJob;
//import com.sprint.findex.entity.SyncJobResult;
//import com.sprint.findex.entity.SyncJobType;
//import jakarta.persistence.criteria.CriteriaBuilder;
//import jakarta.persistence.criteria.Predicate;
//import jakarta.persistence.criteria.Root;
//import java.time.LocalDate;
//import java.time.OffsetDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import org.springframework.data.jpa.domain.Specification;
//
///**
// * SyncJob 엔티티에 대한 동적 쿼리 조건을 생성하는 Specification 클래스
// * 복합 조건 검색과 커서 기반 페이지네이션을 지원합니다.
// */
//public class SyncJobSpecification {
//
//    /**
//     * 모든 필터 조건과 커서 조건을 조합하는 메인 메서드
//     *
//     * @param responseCursorDto 커서 페이지네이션을 위한 이전 페이지 정보
//     * @param searchDto 검색 조건들을 담은 DTO
//     * @return 모든 조건이 결합된 Specification
//     */
//    public static Specification<SyncJob> withFilters(ResponseCursorDto responseCursorDto, SyncJobSearchDto searchDto) {
//        return (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            /**
//             * Root: SyncJob 엔티티 (쿼리의 FROM 절)
//             * query: 전체 쿼리 객체 (SELECT, FROM, WHERE, ORDER BY 등을 구성)
//             * CriteriaBuilder: 쿼리 조건 생성 도구 (비교연산자, 논리연산자 제공)
//             */
//
//            // 작업 유형 완전 일치 (INDEX_INFO, INDEX_DATA)
//            if (searchDto.jobType() != null) {
//                predicates.add(criteriaBuilder.equal(root.get("jobType"), searchDto.jobType()));
//            }
//
//            // 지수 정보 ID 완전 일치
//            if (searchDto.indexInfoId() != null) {
//                predicates.add(criteriaBuilder.equal(root.get("indexInfoId"), searchDto.indexInfoId()));
//            }
//
//            // 대상 날짜 범위 필터링 (시작일부터)
//            // INDEX_INFO 타입은 targetDate가 null이므로 필터링에서 제외
//            if (searchDto.baseDateFrom() != null) {
//                predicates.add(criteriaBuilder.and(
//                    criteriaBuilder.notEqual(root.get("jobType"), SyncJobType.INDEX_INFO),
//                    criteriaBuilder.greaterThanOrEqualTo(root.get("targetDate"), searchDto.baseDateFrom())
//                ));
//            }
//
//            // 대상 날짜 범위 필터링 (종료일까지)
//            // INDEX_INFO 타입은 targetDate가 null이므로 필터링에서 제외
//            if (searchDto.baseDateTo() != null) {
//                predicates.add(criteriaBuilder.and(
//                    criteriaBuilder.notEqual(root.get("jobType"), SyncJobType.INDEX_INFO),
//                    criteriaBuilder.lessThanOrEqualTo(root.get("targetDate"), searchDto.baseDateTo())
//                ));
//            }
//
//            // 작업자 완전 일치 검색
//            if (hasValue(searchDto.worker())) {
//                predicates.add(criteriaBuilder.equal(root.get("worker"), searchDto.worker()));
//            }
//
//            // 작업 일시 범위 필터링 (시작 시간부터)
//            if (searchDto.jobTimeFrom() != null) {
//                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("jobTime"), searchDto.jobTimeFrom()));
//            }
//
//            // 작업 일시 범위 필터링 (종료 시간까지)
//            if (searchDto.jobTimeTo() != null) {
//                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("jobTime"), searchDto.jobTimeTo()));
//            }
//
//            // 작업 상태 완전 일치 (SUCCESS, FAILED)
//            if (searchDto.status() != null) {
//                predicates.add(criteriaBuilder.equal(root.get("result"), searchDto.status()));
//            }
//
//            // 커서 조건이 있으면 추가
//            if (responseCursorDto != null) {
//                addCursorConditions(searchDto, responseCursorDto, root, criteriaBuilder, predicates);
//            }
//
//            // 모든 조건을 AND로 결합
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//
//    /**
//     * 커서 기반 페이지네이션 조건을 추가하는 메서드
//     */
//    private static void addCursorConditions(SyncJobSearchDto searchDto,
//        ResponseCursorDto responseCursorDto,
//        Root<SyncJob> root,
//        CriteriaBuilder criteriaBuilder,
//        List<Predicate> predicates) {
//
//        Predicate cursorPredicate = createCursorPredicate(searchDto, responseCursorDto, root, criteriaBuilder);
//
//        // cursorPredicate가 있으면 조건에 추가
//        if (cursorPredicate != null) {
//            predicates.add(cursorPredicate);
//        }
//    }
//
//    /**
//     * 정렬 필드에 따른 커서 조건을 생성하는 메서드
//     * 정렬 방향(asc/desc)에 따라 다른 비교 연산자를 사용합니다.
//     */
//    private static Predicate createCursorPredicate(SyncJobSearchDto searchDto,
//        ResponseCursorDto responseCursorDto,
//        Root<SyncJob> root,
//        CriteriaBuilder criteriaBuilder) {
//
//        String sortField = searchDto.sortField();
//        boolean isAsc = "asc".equalsIgnoreCase(searchDto.sortDirection());
//
//        return switch (sortField) {
//            case "targetDate" -> {
//                // INDEX_INFO 타입의 경우 targetDate가 null이므로 ID 기준 정렬로 처리
//                yield createTargetDateCursorPredicate(responseCursorDto, root, criteriaBuilder, isAsc);
//            }
//            case "jobTime" -> {
//                OffsetDateTime cursorValue = responseCursorDto.jobTime();
//                if (cursorValue == null) {
//                    yield criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id());
//                }
//                yield isAsc
//                    ? criteriaBuilder.or(
//                    // jobTime이 커서 값보다 큰 경우
//                    criteriaBuilder.greaterThan(root.get("jobTime"), cursorValue),
//                    // jobTime이 같고 ID가 커서 ID보다 큰 경우
//                    criteriaBuilder.and(
//                        criteriaBuilder.equal(root.get("jobTime"), cursorValue),
//                        criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id())
//                    )
//                )
//                    : criteriaBuilder.or(
//                        // desc 정렬: jobTime이 커서 값보다 작은 경우
//                        criteriaBuilder.lessThan(root.get("jobTime"), cursorValue),
//                        criteriaBuilder.and(
//                            criteriaBuilder.equal(root.get("jobTime"), cursorValue),
//                            criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id())
//                        )
//                    );
//            }
//            // 기본 정렬은 ID로 처리
//            default -> criteriaBuilder.greaterThan(root.get("id"), responseCursorDto.id());
//        };
//    }
//
//    /**
//     * targetDate 정렬을 위한 특별한 커서 처리 메서드
//     * INDEX_INFO는 targetDate가 null이므로 항상 ID 기준 오름차순으로 처리
//     */
//    private static Predicate createTargetDateCursorPredicate(ResponseCursorDto responseCursorDto,
//        Root<SyncJob> root,
//        CriteriaBuilder criteriaBuilder,
//        boolean isAsc) {
//        LocalDate cursorValue = responseCursorDto.targetDate();
//        Long cursorId = responseCursorDto.id();
//
//        if (cursorValue == null) {
//            // 커서의 targetDate가 null인 경우 (INDEX_INFO였던 경우)
//            // INDEX_INFO는 항상 ID 기준 오름차순
//            return criteriaBuilder.or(
//                // INDEX_DATA 타입이면서 정렬 방향에 따른 조건
//                isAsc
//                    ? criteriaBuilder.and(
//                    criteriaBuilder.equal(root.get("jobType"), SyncJobType.INDEX_DATA),
//                    criteriaBuilder.isNotNull(root.get("targetDate"))
//                )
//                    : criteriaBuilder.conjunction(), // desc일 때는 INDEX_DATA가 먼저 나옴
//                // INDEX_INFO 타입이면서 ID가 커서보다 큰 경우
//                criteriaBuilder.and(
//                    criteriaBuilder.equal(root.get("jobType"), SyncJobType.INDEX_INFO),
//                    criteriaBuilder.greaterThan(root.get("id"), cursorId)
//                )
//            );
//        } else {
//            // 커서의 targetDate가 있는 경우 (INDEX_DATA였던 경우)
//            return isAsc
//                ? criteriaBuilder.or(
//                // INDEX_INFO는 항상 INDEX_DATA보다 뒤에 (ID 기준 오름차순)
//                criteriaBuilder.equal(root.get("jobType"), SyncJobType.INDEX_INFO),
//                // INDEX_DATA 중에서 targetDate가 커서보다 큰 경우
//                criteriaBuilder.and(
//                    criteriaBuilder.equal(root.get("jobType"), SyncJobType.INDEX_DATA),
//                    criteriaBuilder.greaterThan(root.get("targetDate"), cursorValue)
//                ),
//                // 같은 targetDate에서 ID가 더 큰 경우
//                criteriaBuilder.and(
//                    criteriaBuilder.equal(root.get("jobType"), SyncJobType.INDEX_DATA),
//                    criteriaBuilder.equal(root.get("targetDate"), cursorValue),
//                    criteriaBuilder.greaterThan(root.get("id"), cursorId)
//                )
//            )
//                : criteriaBuilder.or(
//                    // desc 정렬: targetDate가 커서보다 작은 INDEX_DATA
//                    criteriaBuilder.and(
//                        criteriaBuilder.equal(root.get("jobType"), SyncJobType.INDEX_DATA),
//                        criteriaBuilder.lessThan(root.get("targetDate"), cursorValue)
//                    ),
//                    // 같은 targetDate에서 ID가 더 큰 경우
//                    criteriaBuilder.and(
//                        criteriaBuilder.equal(root.get("jobType"), SyncJobType.INDEX_DATA),
//                        criteriaBuilder.equal(root.get("targetDate"), cursorValue),
//                        criteriaBuilder.greaterThan(root.get("id"), cursorId)
//                    )
//                );
//        }
//    }
//
//    /**
//     * 문자열 값이 있는지 확인하는 유틸리티 메서드
//     * null이거나 빈 문자열, 공백만 있는 경우 false 반환
//     */
//    private static boolean hasValue(String str) {
//        return str != null && !str.isBlank();
//    }
//
//    // ========== 개별 조건 메서드들 (필요시 사용) ==========
//
//    public static Specification<SyncJob> hasJobType(SyncJobType jobType) {
//        return (root, query, criteriaBuilder) -> {
//            if (jobType == null) {
//                return criteriaBuilder.conjunction(); // 항상 true
//            }
//            return criteriaBuilder.equal(root.get("jobType"), jobType);
//        };
//    }
//
//    public static Specification<SyncJob> hasIndexInfoId(Long indexInfoId) {
//        return (root, query, criteriaBuilder) -> {
//            if (indexInfoId == null) {
//                return criteriaBuilder.conjunction();
//            }
//            return criteriaBuilder.equal(root.get("indexInfoId"), indexInfoId);
//        };
//    }
//
//    public static Specification<SyncJob> hasWorker(String worker) {
//        return (root, query, criteriaBuilder) -> {
//            if (!hasValue(worker)) {
//                return criteriaBuilder.conjunction();
//            }
//            return criteriaBuilder.equal(root.get("worker"), worker);
//        };
//    }
//
//    public static Specification<SyncJob> hasStatus(SyncJobResult status) {
//        return (root, query, criteriaBuilder) -> {
//            if (status == null) {
//                return criteriaBuilder.conjunction();
//            }
//            return criteriaBuilder.equal(root.get("result"), status);
//        };
//    }
//
//    public static Specification<SyncJob> afterId(Long idAfter) {
//        return (root, query, criteriaBuilder) -> {
//            if (idAfter == null) {
//                return criteriaBuilder.conjunction();
//            }
//            return criteriaBuilder.greaterThan(root.get("id"), idAfter);
//        };
//    }
//}