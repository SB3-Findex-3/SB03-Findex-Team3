package com.sprint.findex.service.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.dto.request.AutoSyncQueryParams;
import com.sprint.findex.dto.response.AutoSyncConfigDto;
import com.sprint.findex.dto.response.CursorPageResponseAutoSyncConfigDto;
import com.sprint.findex.entity.AutoSyncConfig;
import com.sprint.findex.mapper.AutoSyncMapper;
import com.sprint.findex.repository.AutoSyncRepository;
import com.sprint.findex.specification.AutoSyncSpecifications;
import com.sprint.findex.service.AutoSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicAutoSyncService implements AutoSyncService {

    private final AutoSyncRepository autoSyncRepository;
    private final AutoSyncMapper autoSyncMapper;

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String DEFAULT_SORT_FIELD = "indexInfo.indexName";
    private static final String DEFAULT_SORT_DIRECTION = "asc";

    @Override
    public CursorPageResponseAutoSyncConfigDto findByCursor(AutoSyncQueryParams params) {
        int pageSize = (params.size() != null && params.size() > 0) ? params.size() : DEFAULT_PAGE_SIZE;
        Pageable pageable = resolvePageable(params, pageSize + 1);
        var spec = AutoSyncSpecifications.withFilters(params);

        Page<AutoSyncConfig> page = autoSyncRepository.findAll(spec, pageable);
        List<AutoSyncConfig> rawResults = page.getContent();

        boolean hasNext = rawResults.size() > pageSize;
        if (hasNext) {
            rawResults = rawResults.subList(0, pageSize);
        }

        List<AutoSyncConfigDto> content = rawResults.stream()
            .map(autoSyncMapper::toDto)
            .collect(Collectors.toList());

        String nextCursor = buildCursor(rawResults, params.sortField());
        String nextIdAfter = buildIdCursor(rawResults);

        return new CursorPageResponseAutoSyncConfigDto(content, nextCursor, nextIdAfter, pageSize,
            page.getTotalElements(), hasNext);
    }

    private Pageable resolvePageable(AutoSyncQueryParams params, int pageSize) {
        String sortField = params.sortField() != null ? params.sortField() : DEFAULT_SORT_FIELD;
        String sortDir = params.sortDirection() != null ? params.sortDirection() : DEFAULT_SORT_DIRECTION;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort;
        if ("indexInfo.indexName".equals(sortField)) {
            sort = Sort.by(direction, "indexInfo.indexName");
        } else {
            sort = Sort.by(direction, sortField);
        }

        // tie-breaker로 항상 id 오름차순 추가
        sort = sort.and(Sort.by(Sort.Direction.ASC, "id"));

        return PageRequest.of(0, pageSize, sort);
    }
    private String buildCursor(List<AutoSyncConfig> results, String sortField) {
        if (results.isEmpty()) return null;

        AutoSyncConfig last = results.get(results.size() - 1);
        Object cursorValue = switch (sortField != null ? sortField : DEFAULT_SORT_FIELD) {
            case "indexInfo.indexName" -> last.getIndexInfo().getIndexName();
            case "enabled" -> last.isEnabled();
            default -> null;
        };

        return encodeCursor(Map.of("value", cursorValue));
    }

    private String buildIdCursor(List<AutoSyncConfig> results) {
        if (results.isEmpty()) return null;
        Long id = results.get(results.size() - 1).getId();
        return encodeCursor(Map.of("id", id));
    }

    private String encodeCursor(Map<String, ?> data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("❌ 커서 인코딩 실패", e);
            return null;
        }
    }
}
