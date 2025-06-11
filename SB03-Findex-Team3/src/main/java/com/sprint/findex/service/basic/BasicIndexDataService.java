package com.sprint.findex.service.basic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.dto.dashboard.ChartPoint;
import com.sprint.findex.dto.dashboard.IndexChartDto;
import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.request.IndexDataQueryParams;
import com.sprint.findex.dto.request.IndexDataUpdateRequest;
import com.sprint.findex.dto.response.CursorPageResponseIndexData;
import com.sprint.findex.dto.response.IndexDataCsvExporter;
import com.sprint.findex.dto.response.IndexDataDto;
import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.Period;
import com.sprint.findex.entity.SourceType;
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
    static final int MA5DATA_NUM = 5;
    static final int MA20DATA_NUM = 20;

    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;
    private final IndexDataMapper indexDataMapper;

    @Override
    @Transactional
    public IndexDataDto create(IndexDataCreateRequest request) {
        IndexInfo indexInfo = indexInfoRepository.findById(request.indexInfoId())
            .orElseThrow(() -> new IllegalArgumentException("참조하는 지수 정보를 찾을 수 없음"));

        IndexData indexData = IndexData.from(indexInfo, request, SourceType.USER);
        return IndexDataMapper.toDto(indexDataRepository.save(indexData));
    }

    @Override
    @Transactional
    public IndexDataDto update(Long id, IndexDataUpdateRequest request) {
        IndexData indexData = indexDataRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("수정할 지수 데이터를 찾을 수 없음"));

        indexData.update(request);
        return IndexDataMapper.toDto(indexDataRepository.save(indexData));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        IndexData indexData = indexDataRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("삭제할 지수 데이터를 찾을 수 없음"));

        indexDataRepository.delete(indexData);
    }


    @Transactional
    @Override
    public String exportToCsv(IndexDataQueryParams params) {

        String sortField = params.sortField() != null ? params.sortField() : "baseDate";
        Sort.Direction direction =
            "asc".equalsIgnoreCase(params.sortDirection()) ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, sortField).and(Sort.by(Sort.Direction.ASC, "id"));

        var spec = IndexDataSpecifications.withFilters(params);

        List<IndexData> rawResults = indexDataRepository.findAll(spec, sort);

        List<IndexDataDto> content = rawResults.stream()
            .map(IndexDataMapper::toDto)
            .collect(Collectors.toList());

        return IndexDataCsvExporter.toCsv(content);
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

    @Transactional(readOnly = true)
    @Override
    public CursorPageResponseIndexData<IndexDataDto> findByCursor(IndexDataQueryParams params) {
        int pageSize =
            params.size() != null && params.size() > 0 ? params.size() : DEFAULT_PAGE_SIZE;

        Pageable pageable = resolvePageable(params); // 페이징과 정렬 한 번에 처리
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

        String nextCursor = buildCursor(rawResults, params.sortField());
        String nextIdAfter = buildIdCursor(rawResults);

        return new CursorPageResponseIndexData<>(content, nextCursor, nextIdAfter, pageSize,
            pageResult.getTotalElements(), hasNext);
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
            log.error("Cursor 인코딩 실패", e);
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
                .orElseThrow(() -> new NoSuchElementException("지수 정보를 찾을 수 없습니다."));

        LocalDate startDate = calculateBaseDate(periodType);
        LocalDate currentDate = Instant.now().atZone(ZoneId.of("Asia/Seoul")).toLocalDate();

        List<ChartPoint> pricePoints = new ArrayList<>();
        // 일별 종가를 위한 price 데이터 수집
        while (!startDate.isAfter(currentDate)) {
            Optional<IndexData> dataOpt = indexDataRepository
                    .findByIndexInfoIdAndBaseDateOnlyDateMatch(indexInfoId, startDate);

            LocalDate finalStartDate = startDate;
            dataOpt.ifPresent(data ->
                    pricePoints.add(new ChartPoint(finalStartDate.toString(), data.getClosingPrice()))
            );

            startDate = startDate.plusDays(1);
        }
        // 이동 평균 계산
        List<ChartPoint> ma5 = calculateMovingAverageStrict(pricePoints, MA5DATA_NUM);
        List<ChartPoint> ma20 = calculateMovingAverageStrict(pricePoints, MA20DATA_NUM);

        return new IndexChartDto(indexInfoId, indexInfo.getIndexClassification(),
                indexInfo.getIndexName(), periodType, pricePoints, ma5, ma20);
    }

    // Deque를 사용해서 이동 평균을 계산하는 메서드
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

    private LocalDate calculateBaseDate(com.sprint.findex.entity.Period periodType) {
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

