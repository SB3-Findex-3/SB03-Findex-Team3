package com.sprint.findex.service.basic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.request.IndexDataQueryParams;
import com.sprint.findex.dto.request.IndexDataUpdateRequest;
import com.sprint.findex.dto.response.CursorPageResponseIndexData;
import com.sprint.findex.dto.response.IndexDataDto;
import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.SourceType;
import com.sprint.findex.mapper.IndexDataMapper;
import com.sprint.findex.repository.IndexDataRepository;
import com.sprint.findex.repository.IndexDataSpecifications;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.service.IndexDataService;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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

        Pageable pageable = resolvePageable(params);
        var fullSpec = IndexDataSpecifications.withFilters(params);
        var countSpec = IndexDataSpecifications.withFilters(params.withoutCursor());

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

        // 정확한 총 개수 계산
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
            log.error("❌ Cursor 인코딩 실패", e);
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
}

