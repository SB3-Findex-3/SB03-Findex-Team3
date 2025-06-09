package com.sprint.findex.mapper;

import com.sprint.findex.dto.response.IndexInfoDto;
import com.sprint.findex.entity.IndexInfo;
import org.springframework.stereotype.Component;

@Component
public class IndexInfoMapper {

    public IndexInfoDto toDto(IndexInfo indexInfo){
        return new IndexInfoDto(
            indexInfo.getId(),
            indexInfo.getIndexClassification(),
            indexInfo.getIndexName(),
            indexInfo.getEmployedItemsCount(),
            indexInfo.getBasePointInTime(),
            indexInfo.getBaseIndex(),
            indexInfo.getSourceType(),
            indexInfo.isFavorite()
        );
    }

    public IndexInfo toEntity(IndexInfoDto indexInfoDto){
        return new IndexInfo(
            indexInfoDto.indexClassification(),
            indexInfoDto.indexName(),
            indexInfoDto.employedItemsCount(),
            indexInfoDto.basePointInTime(),
            indexInfoDto.baseIndex(),
            indexInfoDto.sourceType(),
            indexInfoDto.favorite()
        );
    }
}
