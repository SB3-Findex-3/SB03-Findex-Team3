package com.sprint.findex.mapper;

import com.sprint.findex.dto.response.IndexDataDto;
import com.sprint.findex.entity.IndexData;
import org.springframework.stereotype.Component;

@Component
public class IndexDataMapper {

    public static IndexDataDto toDto(IndexData entity) {

        return new IndexDataDto(
            entity.getId(),
            entity.getBaseDate(),
            entity.getSourceType(),
            entity.getClosingPrice(),
            entity.getMarketPrice(),
            entity.getHighPrice(),
            entity.getLowPrice(),
            entity.getVersus(),
            entity.getFluctuationRate(),
            entity.getTradingQuantity(),
            entity.getTradingPrice(),
            entity.getMarketTotalAmount()
        );
    }
}
