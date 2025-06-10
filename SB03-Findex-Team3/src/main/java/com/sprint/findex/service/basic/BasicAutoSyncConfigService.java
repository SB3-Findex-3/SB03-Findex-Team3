package com.sprint.findex.service.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.dto.request.AutoSyncConfigQueryParams;
import com.sprint.findex.dto.request.AutoSyncConfigUpdateRequest;
import com.sprint.findex.dto.response.AutoSyncConfigDto;
import com.sprint.findex.dto.response.CursorPageResponseAutoSyncConfigDto;
import com.sprint.findex.entity.AutoSyncConfig;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.mapper.AutoSyncConfigMapper;
import com.sprint.findex.repository.AutoSyncConfigRepository;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.service.AutoSyncConfigService;
import com.sprint.findex.specification.AutoSyncSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BasicAutoSyncConfigService implements AutoSyncConfigService {

    private final AutoSyncConfigRepository autoSyncConfigRepository;
    private final IndexInfoRepository indexInfoRepository;
    private final AutoSyncConfigMapper autoSyncConfigMapper;

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String DEFAULT_SORT_FIELD = "indexInfo.indexName";
    private static final String DEFAULT_SORT_DIRECTION = "asc";

    @Override
    @Transactional
    public AutoSyncConfigDto updateOrCreate(Long id, AutoSyncConfigUpdateRequest request) {
        boolean enabled = request.enabled();
        return autoSyncConfigRepository.findById(id)
                .map(config -> {
                    config.update(enabled); // 기존 설정 업데이트
                    AutoSyncConfig updatedConfig = autoSyncConfigRepository.save(config);

                    return autoSyncConfigMapper.toDto(updatedConfig);
                })
                .orElseGet(() -> {
                    IndexInfo indexInfo = indexInfoRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("IndexInfo with id " + id + " not found"));

                    AutoSyncConfig newAutoSyncConfig = AutoSyncConfig.ofIndexInfo(indexInfo);
                    newAutoSyncConfig.update(enabled);
                    autoSyncConfigRepository.save(newAutoSyncConfig);

                    return autoSyncConfigMapper.toDto(newAutoSyncConfig);
                });
    }


    @Override
    @Transactional
    public CursorPageResponseAutoSyncConfigDto findByCursor(AutoSyncConfigQueryParams params) {
        int pageSize = (params.size() != null && params.size() > 0) ? params.size() : DEFAULT_PAGE_SIZE;
        Pageable pageable = resolvePageable(params, pageSize + 1);
        var spec = AutoSyncSpecifications.withFilters(params);

        Page<AutoSyncConfig> page = autoSyncConfigRepository.findAll(spec, pageable);
        List<AutoSyncConfig> rawResults = page.getContent();

        boolean hasNext = rawResults.size() > pageSize;
        if (hasNext) {
            rawResults = rawResults.subList(0, pageSize);
        }

        List<AutoSyncConfigDto> content = rawResults.stream()
                .map(autoSyncConfigMapper::toDto)
                .collect(Collectors.toList());

        String nextCursor = buildCursor(rawResults, params.sortField());
        String nextIdAfter = buildIdCursor(rawResults);

        return new CursorPageResponseAutoSyncConfigDto(content, nextCursor, nextIdAfter, pageSize,
                page.getTotalElements(), hasNext);
    }

    private Pageable resolvePageable(AutoSyncConfigQueryParams params, int pageSize) {
        String sortField = params.sortField() != null ? params.sortField() : DEFAULT_SORT_FIELD;
        String sortDir = params.sortDirection() != null ? params.sortDirection() : DEFAULT_SORT_DIRECTION;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // 필드 매핑
        String mappedField = switch (sortField) {
            case "indexName" -> "indexInfo.indexName"; // for internal use
            case "enabled" -> "enabled";
            default -> "id";
        };

        // 제 처리 방식은 이렇게 (단, indexInfo.indexName은 JPA에서 불가능하므로 fallback 필요)
        Sort sort;
        if ("indexName".equals(sortField)) {
            // 여기서는 "indexInfo.indexName" 말고 DB에 실제 조인 후 정렬하는 방식이 필요
            // 가능하면 DB View 또는 Projection 사용 고려
            // 임시로 id 정렬로 대체
            sort = Sort.by(direction, "indexInfo.id"); // fallback
        } else {
            sort = Sort.by(direction, mappedField);
        }

        // tie-breaker로 항상 id 오름차순 추가
        sort = sort.and(Sort.by(Sort.Direction.ASC, "id")); // tie-breaker
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

        assert cursorValue != null;
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
            return null;
        }
    }
}


