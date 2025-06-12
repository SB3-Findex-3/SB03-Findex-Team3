package com.sprint.findex.service.basic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.findex.dto.response.IndexInfoSearchDto;
import com.sprint.findex.dto.request.IndexInfoCreateCommand;
import com.sprint.findex.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex.dto.response.cursor.CursorPageResponseIndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoSummaryDto;
import com.sprint.findex.dto.response.ResponseCursorDto;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.global.exception.CommonException;
import com.sprint.findex.global.exception.Errors;
import com.sprint.findex.mapper.IndexInfoMapper;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.service.IndexInfoService;
import com.sprint.findex.specification.IndexInfoSpecifications;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicIndexInfoService implements IndexInfoService {

    private final IndexInfoRepository indexInfoRepository;
    private final IndexInfoMapper indexInfoMapper;
    private final ObjectMapper objectMapper;


    @Override
    public IndexInfoDto createIndexInfo(IndexInfoCreateCommand command) {
        IndexInfo indexInfo = IndexInfo.create(command);
        IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);
        return indexInfoMapper.toDto(savedIndexInfo);
    }

    @Override
    @Transactional
    public IndexInfoDto updateIndexInfo(Long id, IndexInfoUpdateRequest updateDto){
        IndexInfo indexInfo = indexInfoRepository.findById(id)
            .orElseThrow(() -> new CommonException(Errors.INDEX_INFO_NOT_FOUND));

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
        indexInfoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public IndexInfoDto findById(Long id) {

        IndexInfo indexInfo = indexInfoRepository.findById(id)
            .orElseThrow(() -> new CommonException(Errors.INDEX_INFO_NOT_FOUND));

        return indexInfoMapper.toDto(indexInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseIndexInfoDto findIndexInfoByCursor(IndexInfoSearchDto searchDto) {
        try {
            ResponseCursorDto responseCursorDto = null;
            if (searchDto.cursor() != null) {
                responseCursorDto = parseCurser(searchDto.cursor());
                log.info("[IndexInfoService] 지수 목록 조회를 위해 커서 디코딩 완료, 디코딩 된 커서: {}", responseCursorDto);
            }
  
            Specification<IndexInfo> spec = IndexInfoSpecifications.withFilters(responseCursorDto, searchDto);

            Specification<IndexInfo> countSpec = IndexInfoSpecifications.withFilters(null,
                searchDto);

            Sort sort = createSort(searchDto.sortField(), searchDto.sortDirection());
            Pageable pageable = PageRequest.of(0, searchDto.size(), sort);

            Slice<IndexInfo> slice = indexInfoRepository.findAll(spec, pageable);

            Long totalElements = indexInfoRepository.count(countSpec);

        log.info("[IndexInfoService] 조회 완료 -> 결과 수: {}, 다음 페이지 존재: {}, 전체 개수: {}",
            slice.getNumberOfElements(), slice.hasNext(), totalElements);

            return convertToResponse(slice, searchDto, totalElements);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new CommonException(Errors.INDEX_INFO_BAD_REQUEST, e);
        } catch (Exception e) {
            throw new CommonException(Errors.INTERNAL_SERVER_ERROR, e);
        }
    }

    public List<IndexInfoSummaryDto> findIndexInfoSummary() {
        return indexInfoRepository.findAllByOrderByIdAsc().stream()
            .map(indexInfo -> new IndexInfoSummaryDto(
                indexInfo.getId(),
                indexInfo.getIndexClassification(),
                indexInfo.getIndexName()
            ))
            .toList();
    }

    private CursorPageResponseIndexInfoDto convertToResponse(
        Slice<IndexInfo> slice,
        IndexInfoSearchDto searchDto,
        Long totalElements) {

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

        }

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
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ?
            Sort.Direction.DESC : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortField).and(Sort.by("id").ascending());

        log.debug("[IndexInfoService] Sort 생성 -> {}", sort);
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
                    log.warn("[IndexInfoService] 알 수 없는 정렬 필드. 기본값을 사용합니다");
                    yield item.indexClassification();
                }
            };

            log.debug("[IndexInfoService] 커서 생성 -> sortField: {}, cursorValue: {}", sortField, cursorValue);

            ResponseCursorDto responseCursorDto = new ResponseCursorDto(
                item.id(),
                item.indexClassification(),
                item.indexName(),
                item.employedItemsCount()
            );

            String jString = objectMapper.writeValueAsString(responseCursorDto);
            String idToString = objectMapper.writeValueAsString(responseCursorDto.id());

            String encodedCursor = Base64.getEncoder().encodeToString(jString.getBytes());
            encodedPageData.add(encodedCursor);
            String encodedIdAfter = Base64.getEncoder().encodeToString(idToString.getBytes());
            encodedPageData.add(encodedIdAfter);

            return encodedPageData;

        }catch (JsonProcessingException e) {
            log.error("[IndexInfoService] 커서 생성 실패, item: {} sortField: {}", item, sortField);
            throw new RuntimeException(e);
        }
    }

    private ResponseCursorDto parseCurser(String cursor){
        try{

            byte[] decodedBytes = Base64.getDecoder().decode(cursor);
            String jsonString = new String(decodedBytes);

            return objectMapper.readValue(jsonString, ResponseCursorDto.class);
        }
        catch (JsonProcessingException e){
            log.error("[IndexInfoService] 입력커서: {} 파싱 실패 ", cursor);
            throw new RuntimeException(e);
        }
    }
}
