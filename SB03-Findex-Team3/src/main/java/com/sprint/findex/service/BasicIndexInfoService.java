package com.sprint.findex.service;

import com.sprint.findex.dto.request.IndexInfoCreateCommand;
import com.sprint.findex.dto.request.IndexInfoUpdateRequest;
import com.sprint.findex.dto.response.IndexInfoDto;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.mapper.IndexInfoMapper;
import com.sprint.findex.repository.IndexInfoRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class BasicIndexInfoService implements IndexInfoService {

    private final IndexInfoRepository indexInfoRepository;
    private final IndexInfoMapper indexInfoMapper;


    @Override
    @Transactional
    public IndexInfoDto createIndexInfo(IndexInfoCreateCommand command) {
        IndexInfo indexInfo = IndexInfo.create(command);
        indexInfoRepository.save(indexInfo);

        return indexInfoMapper.toDto(indexInfo);
    }

    @Override
    @Transactional
    public IndexInfo createIndexInfoFromApi(IndexInfoCreateCommand command) {
        IndexInfo indexInfo = IndexInfo.create(command);
        indexInfoRepository.save(indexInfo);

        return indexInfo;
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

}
