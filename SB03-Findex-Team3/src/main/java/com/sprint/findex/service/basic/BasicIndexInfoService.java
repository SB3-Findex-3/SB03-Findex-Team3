package com.sprint.findex.service.basic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.dto.request.IndexInfoCreateCommand;
import com.sprint.findex.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex.dto.response.*;
import com.sprint.findex.entity.AutoSyncConfig;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.global.exception.custom.AutoSyncConfigCreationException;
import com.sprint.findex.global.exception.custom.IndexInfoNotFoundException;
import com.sprint.findex.global.exception.custom.InvalidCursorException;
import com.sprint.findex.global.exception.custom.InvalidIndexInfoCommandException;
import com.sprint.findex.mapper.IndexInfoMapper;
import com.sprint.findex.repository.AutoSyncConfigRepository;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.service.IndexInfoService;
import com.sprint.findex.specification.IndexInfoSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicIndexInfoService implements IndexInfoService {

    private final IndexInfoRepository indexInfoRepository;
    private final IndexInfoMapper indexInfoMapper;
    private final ObjectMapper objectMapper;
    private final AutoSyncConfigRepository autoSyncConfigRepository;


    @Override
    public IndexInfoDto createIndexInfo(IndexInfoCreateCommand command) {
        if (command == null) {
            throw new InvalidIndexInfoCommandException("지수 생성 명령(command)이 null입니다.");
        }

        IndexInfo indexInfo;
        try {
            indexInfo = IndexInfo.create(command);
        } catch (Exception e) {
            throw new InvalidIndexInfoCommandException("지수 정보 생성에 실패했습니다.", e);
        }

        IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);

        try {
            AutoSyncConfig config = AutoSyncConfig.ofIndexInfo(savedIndexInfo);
            autoSyncConfigRepository.save(config);
        } catch (Exception e) {
            log.error("[IndexInfoService] createIndexInfo - 자동 연동 설정 저장 실패. indexInfoId={}", savedIndexInfo.getId(), e);
            throw new AutoSyncConfigCreationException("자동 연동 설정 저장에 실패했습니다.", e);
        }

        return indexInfoMapper.toDto(savedIndexInfo);
    }

    @Override
    @Transactional
    public IndexInfoDto updateIndexInfo(Long id, IndexInfoUpdateRequest updateDto){
        if (id == null) {
            throw new IllegalArgumentException("지수 ID는 null일 수 없습니다.");
        }

        if (updateDto == null) {
            throw new InvalidIndexInfoCommandException("지수 수정 요청(updateDto)이 null입니다.");
        }

        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() ->
                        new IndexInfoNotFoundException("지수 정보를 찾을 수 없습니다. id: " + id));

        if(updateDto.employedItemsCount() != null && !updateDto.employedItemsCount().equals(indexInfo.getEmployedItemsCount())){
            indexInfo.updateEmployedItemsCount(updateDto.employedItemsCount());
        }

        if(updateDto.basePointInTime() != null && !updateDto.basePointInTime().equals(indexInfo.getBasePointInTime())){
            indexInfo.updateBasePointInTime(updateDto.basePointInTime());
        }

        if(updateDto.baseIndex() != null && !updateDto.baseIndex().equals(indexInfo.getBaseIndex())){
            indexInfo.updateBaseIndex(updateDto.baseIndex());
        }

        if(updateDto.favorite() != null && updateDto.favorite() != indexInfo.isFavorite()){
            indexInfo.updateFavorite(updateDto.favorite());
        }


        return indexInfoMapper.toDto(indexInfo);
    }

    @Override
    @Transactional
    public void deleteIndexInfo(Long id) {
        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() -> new IndexInfoNotFoundException("지수 정보를 찾을 수 없습니다. id: " + id));

        autoSyncConfigRepository.deleteById(id);

        indexInfoRepository.delete(indexInfo);
    }


    @Override
    @Transactional(readOnly = true)
    public IndexInfoDto findById(Long id) {
        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() ->
                        new IndexInfoNotFoundException("지수 정보를 찾을 수 없습니다. id: " + id));

        return indexInfoMapper.toDto(indexInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseIndexInfoDto findIndexInfoByCursor(IndexInfoSearchDto searchDto) {
        ResponseCursorDto responseCursorDto = null;

        if (searchDto.cursor() != null) {
            try {
                responseCursorDto = parseCurser(searchDto.cursor());
                log.info("[IndexInfoService] 커서 디코딩 완료, 디코딩 된 커서: {}", responseCursorDto);
            } catch (Exception e) {
                log.warn("[IndexInfoService] 커서 디코딩 실패: {}", e.getMessage());
                throw new InvalidCursorException("유효하지 않은 커서 값입니다.");
            }
        }

        Specification<IndexInfo> spec = IndexInfoSpecifications.withFilters(responseCursorDto, searchDto);
        Specification<IndexInfo> countSpec = IndexInfoSpecifications.withFilters(null, searchDto);

        Sort sort = createSort(searchDto.sortField(), searchDto.sortDirection());
        Pageable pageable = PageRequest.of(0, searchDto.size(), sort);

        Slice<IndexInfo> slice = indexInfoRepository.findAll(spec, pageable);

        Long totalElements = indexInfoRepository.count(countSpec);

        log.info("[IndexInfoService] 조회 완료 → 결과 수: {}, 다음 페이지 존재: {}, 전체 개수: {}",
                slice.getNumberOfElements(), slice.hasNext(), totalElements);

        return convertToResponse(slice, searchDto, totalElements);
    }


    @Override
    @Transactional(readOnly = true)
    public List<IndexInfoSummaryDto> findIndexInfoSummary() {
        List<IndexInfo> indexInfoList = indexInfoRepository.findAllByOrderByIdAsc();

        log.info("[IndexInfoService] 지수 요약 정보 조회 요청 - 총 {}건", indexInfoList.size());

        List<IndexInfoSummaryDto> summaries = indexInfoList.stream()
                .map(indexInfo -> new IndexInfoSummaryDto(
                        indexInfo.getId(),
                        indexInfo.getIndexClassification(),
                        indexInfo.getIndexName()
                ))
                .toList();

        log.info("[IndexInfoService] 지수 요약 정보 변환 완료");

        return summaries;
    }


    private CursorPageResponseIndexInfoDto convertToResponse(
        Slice<IndexInfo> slice,
        IndexInfoSearchDto searchDto,
        Long totalElements
    ) {

        List<IndexInfoDto> content = slice.getContent().stream()
            .map(indexInfoMapper::toDto)
            .toList();

        String nextCursor = null;
        String nextIdAfter = null;

        if (slice.hasNext() && !content.isEmpty()) {
            IndexInfoDto lastItem = content.get(content.size() - 1);
            List<String> nextInfo = generateNextCursor(lastItem, searchDto.sortField());
            nextCursor = nextInfo.get(0);
            nextIdAfter = nextInfo.get(1);

            log.info("[IndexInfoService] 다음 커서 생성 완료 - nextCursor: {}, nextIdAfter: {}", nextCursor, nextIdAfter);
        } else {
            log.info("[IndexInfoService] 더 이상 다음 커서 없음 (hasNext: {}, contentSize: {})", slice.hasNext(), content.size());
        }

        log.info("[IndexInfoService] 커서 페이지 응답 생성 완료 - 총 개수: {}, 응답 크기: {}", totalElements, content.size());


        return new CursorPageResponseIndexInfoDto(
            content,
            nextCursor,
            nextIdAfter,
            searchDto.size(),
            totalElements,
            slice.hasNext()
        );
    }

    private Sort createSort(String sortField, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortField)
                .and(Sort.by("id").ascending());

        log.info("[IndexInfoService] 정렬 조건 생성 완료 - sortField: {}, direction: {}, 최종 정렬: {}",
                sortField, direction, sort);

        return sort;
    }

    private List<String> generateNextCursor(IndexInfoDto item, String sortField) {
        List<String> encodedPageData = new ArrayList<>();

        try {
            String cursorValue = switch (sortField) {
                case "indexClassification" -> item.indexClassification();
                case "indexName" -> item.indexName();
                case "employedItemsCount" -> String.valueOf(item.employedItemsCount());
                default -> {
                    log.warn("[IndexInfoService] 알 수 없는 정렬 필드: '{}'. 기본값 'indexClassification' 사용", sortField);
                    yield item.indexClassification();
                }
            };

            log.info("[IndexInfoService] 커서 생성 시작 - sortField: {}, cursorValue: {}", sortField, cursorValue);

            ResponseCursorDto responseCursorDto = new ResponseCursorDto(
                item.id(),
                item.indexClassification(),
                item.indexName(),
                item.employedItemsCount()
            );

            String encodedCursor = Base64.getEncoder().encodeToString(
                    objectMapper.writeValueAsString(responseCursorDto).getBytes(StandardCharsets.UTF_8)
            );
            String encodedIdAfter = Base64.getEncoder().encodeToString(
                    objectMapper.writeValueAsString(responseCursorDto.id()).getBytes(StandardCharsets.UTF_8)
            );

            encodedPageData.add(encodedCursor);
            encodedPageData.add(encodedIdAfter);

            log.info("[IndexInfoService] 커서 생성 완료 - encodedCursor: {}, encodedIdAfter: {}", encodedCursor, encodedIdAfter);


            return encodedPageData;

        } catch (JsonProcessingException e) {
            log.error("[IndexInfoService] 커서 JSON 변환 실패 - item: {}, sortField: {}", item, sortField, e);
            throw new RuntimeException("커서 생성 중 예외 발생", e);
        }
    }

    private ResponseCursorDto parseCurser(String cursor){ // 이거 메소드명 고쳐야함
        try{
            byte[] decodedBytes = Base64.getDecoder().decode(cursor);
            String jsonString = new String(decodedBytes, StandardCharsets.UTF_8);

            log.info("[IndexInfoService] 커서 파싱 시작");
            log.info("[IndexInfoService] 인코딩된 커서: {}", cursor);
            log.info("[IndexInfoService] 디코딩된 JSON: {}", jsonString);

            return objectMapper.readValue(jsonString, ResponseCursorDto.class);
        } catch (JsonProcessingException e){
            log.error("[IndexInfoService] 커서 파싱 실패 - 입력 커서: {}", cursor, e);
            throw new InvalidCursorException("커서 파싱에 실패했습니다.");
        }
    }
}
