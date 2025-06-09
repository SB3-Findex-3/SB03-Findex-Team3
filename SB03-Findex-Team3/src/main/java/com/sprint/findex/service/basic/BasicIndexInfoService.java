package com.sprint.findex.service.basic;

import com.sprint.findex.dto.IndexInfoSearchDto;
import com.sprint.findex.dto.request.IndexInfoCreateCommand;
import com.sprint.findex.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex.dto.response.CursorPageResponseIndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoDto;
import com.sprint.findex.dto.response.IndexInfoSummaryDto;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.mapper.IndexInfoMapper;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.repository.IndexInfoSpecifications;
import com.sprint.findex.service.IndexInfoService;
import java.util.List;
import java.util.NoSuchElementException;
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
@Service
@RequiredArgsConstructor
public class BasicIndexInfoService implements IndexInfoService {

    private final IndexInfoMapper indexInfoMapper;
    private final IndexInfoRepository indexInfoRepository;


    @Override
    public IndexInfoDto createIndexInfo(IndexInfoCreateCommand command) {
        return null;
    }

    @Override
    public IndexInfo createIndexInfoFromApi(IndexInfoCreateCommand command) {
        return null;
    }

    @Override
    @Transactional
    public IndexInfoDto updateIndexInfo(Long id, IndexInfoUpdateRequest updateDto){
        IndexInfo indexInfo = indexInfoRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("No index info found with id: " + id));

        if(updateDto.employedItemsCount() != null && !updateDto.employedItemsCount().equals(indexInfo.getEmployedItemsCount())){
            indexInfo.updateEmployedItemsCount(updateDto.employedItemsCount());
        }

        if(updateDto.basePointInTime() != null && !updateDto.basePointInTime().equals(indexInfo.getBasePointInTime())){
            indexInfo.updateBasePointInTime(updateDto.basePointInTime());
        }

        if(updateDto.baseIndex() != null && !updateDto.baseIndex().equals(indexInfo.getBaseIndex())){
            indexInfo.updateBaseIndex(updateDto.baseIndex());
        }

        if(!updateDto.favorite() == indexInfo.isFavorite()){
            indexInfo.updateFavorite(updateDto.favorite());
        }

        return indexInfoMapper.toDto(indexInfo);
    }

    @Override
    @Transactional
    public void deleteIndexInfo(Long id) {
        indexInfoRepository.deleteById(id);
    }


    // 지수 정보 아이디로 조회
    @Transactional(readOnly = true)
    public IndexInfoDto findById(Long id) {

        IndexInfo indexInfo = indexInfoRepository.findById(id)
            .orElseThrow(
                () -> new NoSuchElementException("IndexInfoService: 해당하는 지수 정보가 존재하지 않습니다."));

        return indexInfoMapper.toDto(indexInfo);
    }


    // 지수 목록 조회 (페이지네이션)
    @Transactional(readOnly = true)
    public CursorPageResponseIndexInfoDto findIndexInfoByCursor(IndexInfoSearchDto searchDto) {

        // Specification 생성
        Specification<IndexInfo> spec = IndexInfoSpecifications.withFilters(searchDto);

        // Pageable 생성
        Sort sort = createSort(searchDto.sortField(), searchDto.sortDirection());
        Pageable pageable = PageRequest.of(0, searchDto.size(), sort);

        // 조회
        Slice<IndexInfo> slice = indexInfoRepository.findAll(spec, pageable);

        // totalElements
        Long totalElements = !searchDto.hasCursorInfo() ?
            indexInfoRepository.count(spec) : null;

        log.info("IndexInfoService: 조회 완료 -> 결과 수: {}, 다음 페이지 존재: {}, 전체 개수: {}",
            slice.getNumberOfElements(), slice.hasNext(), totalElements);

        // 5. 응답 생성
        return convertToResponse(slice, searchDto, totalElements);
    }

    // 지수 정보 요약 목록 조회
    public List<IndexInfoSummaryDto> findIndexInfoSummary() {
        return indexInfoRepository.findAllByOrderByIdAsc().stream()
            .map(indexInfo -> new IndexInfoSummaryDto(
                indexInfo.getId(),
                indexInfo.getIndexClassification(),
                indexInfo.getIndexName()
            ))
            .toList();
    }

    // slice를 CursorPageResponseIndexInfoDto로 변환
    private CursorPageResponseIndexInfoDto convertToResponse(
        Slice<IndexInfo> slice,
        IndexInfoSearchDto searchDto,
        Long totalElements) {

        // IndexInfoDto로 변환
        List<IndexInfoDto> content = slice.getContent().stream()
            .map(indexInfoMapper::toDto)
            .toList();

        // 다음 커서 정보
        String nextCursor = null;
        Long nextIdAfter = null;

        if (slice.hasNext() && !content.isEmpty()) {
            IndexInfoDto lastItem = content.get(content.size() - 1);
            nextIdAfter = lastItem.id();
            nextCursor = generateNextCursor(lastItem, searchDto.sortField());

            log.debug("IndexInfoService: 다음 페이지 정보 생성 -> nextIdAfter: {}, nextCursor: {}",
                nextIdAfter, nextCursor);
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

    // Sort 객체 생성
    private Sort createSort(String sortField, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ?
            Sort.Direction.DESC : Sort.Direction.ASC;

        // 정렬 필드로 주 정렬 + ID 이용해서 보조 정렬
        Sort sort = Sort.by(direction, sortField).and(Sort.by("id").ascending());

        log.debug("IndexInfoService: Sort 생성 -> {}", sort);
        return sort;
    }

    // 다음 커서 생성
    private String generateNextCursor(IndexInfoDto item, String sortField) {
        String cursorValue = switch (sortField) {
            case "indexClassification" -> item.indexClassification();
            case "indexName" -> item.indexName();
            case "employedItemsCount" -> String.valueOf(item.employedItemsCount());
            default -> {
                log.warn("IndexInfoService: 알 수 없는 정렬 필드, 기본값을 사용합니다. -> sortField: {}", sortField);
                yield item.indexClassification();
            }
        };

        log.debug("IndexInfoService: 커서 생성 -> sortField: {}, cursorValue: {}", sortField, cursorValue);
        return cursorValue;
    }

}
