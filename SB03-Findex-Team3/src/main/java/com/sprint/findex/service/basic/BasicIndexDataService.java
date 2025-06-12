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
import com.sprint.findex.dto.response.IndexDataDto;
import com.sprint.findex.dto.response.cursor.CursorPageResponseIndexData;
import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.Period;
import com.sprint.findex.entity.SourceType;
import com.sprint.findex.global.exception.CommonException;
import com.sprint.findex.global.exception.Errors;
import com.sprint.findex.mapper.IndexDataMapper;
import com.sprint.findex.repository.IndexDataRepository;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.service.IndexDataService;
import com.sprint.findex.specification.IndexDataSpecifications;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicIndexDataService implements IndexDataService {

    private static final String DEFAULT_SORT_FIELD = "baseDate";
    private static final String DEFAULT_SORT_DIRECTION = "desc";
    private static final int DEFAULT_PAGE_SIZE = 50;

    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;
    private final IndexDataMapper indexDataMapper;

    static final int MA5DATA_NUM = 5;
    static final int MA20DATA_NUM = 20;


    @Override
    @Transactional
    public IndexDataDto create(IndexDataCreateRequest request) {
        IndexInfo indexInfo = indexInfoRepository.findById(request.indexInfoId())
            .orElseThrow(() -> new CommonException(Errors.INDEX_INFO_NOT_FOUND));

        IndexData indexData = IndexData.from(indexInfo, request, SourceType.USER);
        return indexDataMapper.toDto(indexDataRepository.save(indexData));
    }

    @Override
    @Transactional
    public IndexDataDto update(Long id, IndexDataUpdateRequest request) {
        IndexData indexData = indexDataRepository.findById(id)
            .orElseThrow(() -> new CommonException(Errors.INDEX_DATA_NOT_FOUND));

        indexData.update(request);
        return indexDataMapper.toDto(indexDataRepository.save(indexData));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        IndexData indexData = indexDataRepository.findById(id)
            .orElseThrow(() -> new CommonException(Errors.INDEX_DATA_NOT_FOUND));

        indexDataRepository.delete(indexData);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IndexDataDto> findAllByConditions(IndexDataQueryParams params) {
        Sort sort = resolveSort(params);
        var spec = IndexDataSpecifications.withFilters(params);

        List<IndexData> results = indexDataRepository.findAll(spec, sort);
        return results.stream()
            .map(IndexDataMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseIndexData<IndexDataDto> findByCursor(IndexDataQueryParams params) {
        int pageSize = (params.size() != null && params.size() > 0) ? params.size() : DEFAULT_PAGE_SIZE;

        Pageable pageable = resolvePageable(params); // 커서 기반 페이지네이션용
        var fullSpec = IndexDataSpecifications.withFilters(params);
        var countSpec = IndexDataSpecifications.withFilters(params.withoutCursor()); // cursor 제거한 조건

        Page<IndexData> pageResult = indexDataRepository.findAll(fullSpec, pageable);
        List<IndexData> rawResults = pageResult.getContent();

        boolean hasNext = rawResults.size() > pageSize;
        if (hasNext) {
            rawResults = rawResults.subList(0, pageSize);
        }

        List<IndexDataDto> content = rawResults.stream()
            .map(IndexDataMapper::toDto)
            .collect(Collectors.toList());

        String nextCursor = buildCursor(rawResults, params.sortField());
        String nextIdAfter = buildIdCursor(rawResults);

        long totalCount = indexDataRepository.count(countSpec);

        return new CursorPageResponseIndexData<>(
            content,
            nextCursor,
            nextIdAfter,
            pageSize,
            totalCount,
            hasNext
        );
    }
    private String buildCursor(List<IndexData> rawResults, String sortField) {
        if (rawResults.isEmpty())
            return null;

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
            default -> null;
        };

        return encodeCursor(cursorValue);
    }

    private String buildIdCursor(List<IndexData> rawResults) {
        if (rawResults.isEmpty())
            return null;

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
        int size = params.size() != null && params.size() > 0 ? params.size() : DEFAULT_PAGE_SIZE;
        return PageRequest.of(0, size + 1, resolveSort(params)); // 한 페이지 더 요청하여 hasNext 처리
    }

    private Sort resolveSort(IndexDataQueryParams params) {
        String sortField = params.sortField() != null ? params.sortField() : DEFAULT_SORT_FIELD;
        String sortDir =
            params.sortDirection() != null ? params.sortDirection() : DEFAULT_SORT_DIRECTION;
        Sort.Direction direction =
            "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, sortField).and(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional(readOnly = true)
    @Override
    public IndexChartDto getIndexChart(Long indexInfoId, Period periodType) {
        IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
            .orElseThrow(() -> new CommonException(Errors.INDEX_DATA_NOT_FOUND));

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

        List<ChartPoint> ma5 = calculateMovingAverageStrict(pricePoints, MA5DATA_NUM);
        List<ChartPoint> ma20 = calculateMovingAverageStrict(pricePoints, MA20DATA_NUM);

        return new IndexChartDto(indexInfoId, indexInfo.getIndexClassification(),
            indexInfo.getIndexName(), periodType, pricePoints, ma5, ma20);
    }

    @Transactional(readOnly = true)
    @Override
    public List<IndexPerformanceDto> getFavoriteIndexPerformances(Period period) {
        List<IndexInfo> favorites = indexInfoRepository.findByFavoriteTrue();

        return favorites.stream()
            .map(indexInfo -> {
                log.info("[IndexDataService] getFavoriteIndexPerformances 메서드 진입, favorite: {} ",
                    indexInfo.getIndexName());
                IndexData current = indexDataRepository.findTopByIndexInfoIdOrderByBaseDateDesc(
                    indexInfo.getId()).orElse(null);

                IndexData past = indexDataRepository.findByIndexInfoIdAndBaseDateOnlyDateMatch(
                        indexInfo.getId(),
                        calculateBaseDate(period))
                    .orElse(null);
                return IndexPerformanceDto.of(indexInfo, current,
                    past);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<RankedIndexPerformanceDto> getIndexPerformanceRank(Long indexInfoId,
        Period period, int limit) {
        LocalDate baseDate = calculateBaseDate(period);

        List<IndexInfo> targetInfos = (indexInfoId != null)
            ? indexInfoRepository.findAllById(List.of(indexInfoId))
            : indexInfoRepository.findAll();

        List<IndexPerformanceDto> sortedList = targetInfos.stream()
            .map(info -> {
                IndexData current = indexDataRepository.findTopByIndexInfoIdOrderByBaseDateDesc(
                    info.getId()).orElse(null);

                IndexData before = indexDataRepository
                    .findByIndexInfoIdAndBaseDateOnlyDateMatch(info.getId(), baseDate)
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

            if (win.size() > window)
            {
                sum = sum.subtract(win.removeFirst());
            }

            BigDecimal avg = (win.size() == window)
                ? sum.divide(BigDecimal.valueOf(window), 2, RoundingMode.HALF_UP)
                : null;

            result.add(new ChartPoint(p.date(), avg));
        }
        return result;
    }

    private LocalDate calculateBaseDate(Period periodType) {
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