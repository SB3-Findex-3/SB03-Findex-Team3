package com.sprint.findex.service.basic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.dto.dashboard.ChartPoint;
import com.sprint.findex.dto.dashboard.IndexChartDto;
import com.sprint.findex.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex.dto.dashboard.RankedIndexPerformanceDto;
import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.request.IndexDataQueryParams;
import com.sprint.findex.dto.request.IndexDataUpdateRequest;
import com.sprint.findex.dto.response.CursorPageResponseIndexData;
import com.sprint.findex.dto.response.IndexDataDto;
import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.Period;
import com.sprint.findex.entity.SourceType;
import com.sprint.findex.global.exception.InvalidSortFieldException;
import com.sprint.findex.mapper.IndexDataMapper;
import com.sprint.findex.repository.IndexDataRepository;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.service.IndexDataService;
import com.sprint.findex.specification.IndexDataSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicIndexDataService implements IndexDataService {

    private static final String DEFAULT_SORT_FIELD = "baseDate";
    private static final String DEFAULT_SORT_DIRECTION = "desc";
    private static final int DEFAULT_PAGE_SIZE = 200;

    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;
    private final IndexDataMapper indexDataMapper;

    static final int MA5DATA_NUM = 5;
    static final int MA20DATA_NUM = 20;


    @Override
    @Transactional
    public IndexDataDto create(IndexDataCreateRequest request) {
        IndexInfo indexInfo = indexInfoRepository.findById(request.indexInfoId())
            .orElseThrow(() -> new IllegalArgumentException("참조하는 지수 정보를 찾을 수 없음"));

        IndexData indexData = IndexData.from(indexInfo, request, SourceType.USER);
        return indexDataMapper.toDto(indexDataRepository.save(indexData));
    }

    @Override
    @Transactional
    public IndexDataDto update(Long id, IndexDataUpdateRequest request) {
        IndexData indexData = indexDataRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("수정할 지수 데이터를 찾을 수 없음"));

        indexData.update(request);
        return indexDataMapper.toDto(indexDataRepository.save(indexData));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        IndexData indexData = indexDataRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("삭제할 지수 데이터를 찾을 수 없음"));

        indexDataRepository.delete(indexData);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IndexDataDto> findAllByConditions(IndexDataQueryParams params) {

        // 프론트 요청 오류 고려
        if (params == null) {
            throw new IllegalArgumentException("조회 조건(params)은 null일 수 없습니다.");
        }

        Sort sort = resolveSort(params);
        if (!List.of("id", "baseDate", "createdAt").contains(sort)) {
            throw new InvalidSortFieldException("정렬 필드가 올바르지 않습니다: " + sort);
        }
        var spec = IndexDataSpecifications.withFilters(params);

        List<IndexData> results = indexDataRepository.findAll(spec, sort);

        if (results.isEmpty()) {
            log.warn("[IndexDataService] 조건에 맞는 데이터가 없습니다. params: {}", params);
        }

        return results.stream()
            .map(IndexDataMapper::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public CursorPageResponseIndexData<IndexDataDto> findByCursor(IndexDataQueryParams params) {
        if (params == null) {
            throw new IllegalArgumentException("요청 파라미터(params)는 null일 수 없습니다.");
        }

        int pageSize =
            params.size() != null && params.size() > 0 ? params.size() : DEFAULT_PAGE_SIZE;

        Pageable pageable;
        try {
            pageable = resolvePageable(params); // 정렬 필드 유효성 포함
        } catch (IllegalArgumentException e) {
            throw new InvalidSortFieldException("유효하지 않은 정렬 필드: " + params.sortField(), e);
        }

        var spec = IndexDataSpecifications.withFilters(params);

        Page<IndexData> pageResult = indexDataRepository.findAll(spec, pageable);
        List<IndexData> rawResults = pageResult.getContent();
        boolean hasNext = rawResults.size() > pageSize;

        if (hasNext) {
            rawResults = rawResults.subList(0, pageSize);
        }

        List<IndexDataDto> content = rawResults.stream()
            .map(IndexDataMapper::toDto)
            .collect(Collectors.toList());

        if (content.isEmpty()) {
            log.info("[IndexDataService] 결과가 존재하지 않습니다. params: {}", params);
        }

        String nextCursor = buildCursor(rawResults, params.sortField());
        String nextIdAfter = buildIdCursor(rawResults);

        return new CursorPageResponseIndexData<>(
                content,
                nextCursor,
                nextIdAfter,
                pageSize,
                pageResult.getTotalElements(),
                hasNext
        );
    }

    private String buildCursor(List<IndexData> rawResults, String sortField) {
        if (rawResults.isEmpty())
            return null;

        if (sortField == null) {
            throw new InvalidSortFieldException("정렬 필드(sortField)는 null일 수 없습니다.");
        }

        IndexData last = rawResults.get(rawResults.size() - 1);
        Object cursorValue = switch (sortField) {
            case "baseDate" -> last.getBaseDate().toString();
            case "closingPrice" -> last.getClosingPrice();
            case "marketPrice" -> last.getMarketPrice();
            case "highPrice" -> last.getHighPrice();
            case "lowPrice" -> last.getLowPrice();
            case "versus" -> last.getVersus();
            case "fluctuationRate" -> last.getFluctuationRate();
            case "tradingQuantity" -> last.getTradingQuantity();
            case "tradingPrice" -> last.getTradingPrice();
            case "marketTotalAmount" -> last.getMarketTotalAmount();
            default -> throw new InvalidSortFieldException("지원하지 않는 정렬 필드: " + sortField);

        };

        return encodeCursor(cursorValue);
    }

    private String buildIdCursor(List<IndexData> rawResults) {
        if (rawResults == null || rawResults.isEmpty()) {
            return null;
        }

        IndexData last = rawResults.get(rawResults.size() - 1);
        return encodeCursor(last.getId());
    }

    private String encodeCursor(Object value) {
        try {
            if (value != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonCursor = objectMapper.writeValueAsString(Map.of("value", value));
                return Base64.getEncoder()
                    .encodeToString(jsonCursor.getBytes(StandardCharsets.UTF_8));
            }
        } catch (JsonProcessingException e) {
            log.error("[IndexDataService] Cursor 인코딩 실패", e);
        }
        return null;
    }

    private Pageable resolvePageable(IndexDataQueryParams params) {
        int size = (params.size() != null && params.size() > 0)
                ? params.size()
                : DEFAULT_PAGE_SIZE;

        return PageRequest.of(0, size + 1, resolveSort(params)); // 한 페이지 더 요청하여 hasNext 처리
    }

    private Sort resolveSort(IndexDataQueryParams params) {
        String sortField = params.sortField() != null ? params.sortField() : DEFAULT_SORT_FIELD;
        String sortDir = params.sortDirection() != null ? params.sortDirection() : DEFAULT_SORT_DIRECTION;

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDir);
        } catch (IllegalArgumentException e) {
            throw new InvalidSortFieldException("정렬 방향이 잘못되었습니다: " + sortDir, e);
        }

        if (!isAllowedSortField(sortField)) {
            throw new InvalidSortFieldException("지원하지 않는 정렬 필드입니다: " + sortField);
        }

        return Sort.by(direction, sortField)
                .and(Sort.by(Sort.Direction.ASC, "id")); // 커서 일관성 유지용
    }

    private boolean isAllowedSortField(String field) {
        return List.of(
                "baseDate", "closingPrice", "marketPrice", "highPrice",
                "lowPrice", "versus", "fluctuationRate", "tradingQuantity",
                "tradingPrice", "marketTotalAmount"
        ).contains(field);
    }

    @Transactional(readOnly = true)
    @Override
    public IndexChartDto getIndexChart(Long indexInfoId, Period periodType) {
        if (indexInfoId == null) {
            throw new IllegalArgumentException("지수 ID는 null일 수 없습니다.");
        }
        if (periodType == null) {
            throw new IllegalArgumentException("기간 타입은 null일 수 없습니다.");
        }

        IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
            .orElseThrow(() -> new NoSuchElementException("지수 정보를 찾을 수 없습니다."));

        LocalDate startDate = calculateBaseDate(periodType);
        LocalDate currentDate = Instant.now().atZone(ZoneId.of("Asia/Seoul")).toLocalDate();

        List<ChartPoint> pricePoints = new ArrayList<>();

        while (!startDate.isAfter(currentDate)) {
            Optional<IndexData> dataOpt = indexDataRepository
                .findByIndexInfoIdAndBaseDateOnlyDateMatch(indexInfoId, startDate);

            LocalDate finalStartDate = startDate;
            dataOpt.ifPresent(data ->
                pricePoints.add(new ChartPoint(finalStartDate.toString(), data.getClosingPrice()))
            );

            startDate = startDate.plusDays(1);
        }

        if (pricePoints.isEmpty()) {
            log.warn("[IndexDataService] 차트 데이터가 존재하지 않습니다. indexInfoId: {}, 기간: {}", indexInfoId, periodType);
        }

        List<ChartPoint> ma5 = calculateMovingAverageStrict(pricePoints, MA5DATA_NUM);
        List<ChartPoint> ma20 = calculateMovingAverageStrict(pricePoints, MA20DATA_NUM);

        return new IndexChartDto(
                indexInfoId,
                indexInfo.getIndexClassification(),
                indexInfo.getIndexName(),
                periodType,
                pricePoints,
                ma5,
                ma20);
    }

    @Transactional(readOnly = true)
    @Override
    public List<IndexPerformanceDto> getFavoriteIndexPerformances(Period period) {
        if (period == null) {
            throw new IllegalArgumentException("기간(period)은 null일 수 없습니다.");
        }

        List<IndexInfo> favorites = indexInfoRepository.findByFavoriteTrue();

        if (favorites.isEmpty()) {
            log.warn("[IndexDataService] 즐겨찾기된 지수가 존재하지 않습니다.");
            return List.of();
        }

        return favorites.stream()
            .map(indexInfo -> {
                log.info("[IndexDataService] method getFavoriteIndexPerformances, favorite: {} ",
                    indexInfo.getIndexName());

                IndexData current = indexDataRepository.findTopByIndexInfoIdOrderByBaseDateDesc(
                    indexInfo.getId())
                        .orElse(null);

                IndexData past = indexDataRepository.findByIndexInfoIdAndBaseDateOnlyDateMatch(
                        indexInfo.getId(), calculateBaseDate(period))
                    .orElse(null);

                return IndexPerformanceDto.of(indexInfo, current, past);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<RankedIndexPerformanceDto> getIndexPerformanceRank(Long indexInfoId, Period period, int limit) {
        if (period == null) {
            throw new IllegalArgumentException("기간(period)은 null일 수 없습니다.");
        }

        if (limit <= 0) {
            throw new IllegalArgumentException("limit 값은 1 이상이어야 합니다.");
        }

        LocalDate baseDate = calculateBaseDate(period);

        List<IndexInfo> targetInfos = (indexInfoId != null)
            ? indexInfoRepository.findAllById(List.of(indexInfoId))
            : indexInfoRepository.findAll();

        if (targetInfos.isEmpty()) {
            log.warn("[IndexDataService] 지수 정보가 존재하지 않습니다. indexInfoId: {}", indexInfoId);
            return List.of();
        }

        List<IndexPerformanceDto> sortedList = targetInfos.stream()
            .map(info -> {
                IndexData current = indexDataRepository.findTopByIndexInfoIdOrderByBaseDateDesc(
                        info.getId())
                        .orElse(null);

                IndexData before = indexDataRepository.findByIndexInfoIdAndBaseDateOnlyDateMatch(
                        info.getId(), baseDate)
                        .orElse(null);

                return IndexPerformanceDto.of(info, current, before);
            })
            .filter(dto -> dto != null && dto.fluctuationRate() != null)
            .sorted(Comparator.comparing(IndexPerformanceDto::fluctuationRate).reversed())
            .limit(limit)
            .toList();

        List<RankedIndexPerformanceDto> rankedList = new ArrayList<>();
        for (int i = 0; i < sortedList.size(); i++) {
            rankedList.add(new RankedIndexPerformanceDto(sortedList.get(i), i + 1));
        }

        return rankedList;
    }

    private List<ChartPoint> calculateMovingAverageStrict(List<ChartPoint> prices, int window) {
        if (prices == null || prices.isEmpty()) {
            throw new IllegalArgumentException("가격 데이터(prices)가 비어 있거나 null입니다.");
        }

        if (window <= 0) {
            throw new IllegalArgumentException("이동 평균 구간(window)은 1 이상이어야 합니다.");
        }

        List<ChartPoint> pts = prices.stream()
            .sorted(Comparator.comparing(p -> LocalDate.parse(p.date())))
            .toList();

        Deque<BigDecimal> win = new ArrayDeque<>(window);
        BigDecimal sum = BigDecimal.ZERO;
        List<ChartPoint> result = new ArrayList<>(pts.size());

        for (ChartPoint p : pts) {
            BigDecimal v = p.value();
            win.addLast(v);
            sum = sum.add(v);

            if (win.size() > window)             // 윈도 초과 시 맨 앞 제거
            {
                sum = sum.subtract(win.removeFirst());
            }

            BigDecimal avg = (win.size() == window)
                ? sum.divide(BigDecimal.valueOf(window), 2, RoundingMode.HALF_UP)
                : null;                      // 아직 데이터 부족

            result.add(new ChartPoint(p.date(), avg));
        }
        return result;
    }

    private LocalDate calculateBaseDate(Period periodType) {
        if (periodType == null) {
            throw new IllegalArgumentException("기간 타입(periodType)은 null일 수 없습니다.");
        }

        LocalDate today = LocalDate.now();

        LocalDate result = switch (periodType) {
            case DAILY -> today.minusDays(1);
            case WEEKLY -> today.minusWeeks(1);
            case MONTHLY -> today.minusMonths(1);
            case QUARTERLY -> today.minusMonths(3);
            case YEARLY -> today.minusYears(1);
        };

        return result;
    }
}