package com.sprint.findex.service.basic;

import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.response.IndexDataDto;
import com.sprint.findex.entity.IndexData;
import com.sprint.findex.entity.IndexInfo;
import com.sprint.findex.entity.SourceType;
import com.sprint.findex.mapper.IndexDataMapper;
import com.sprint.findex.repository.IndexDataRepository;
import com.sprint.findex.repository.IndexInfoRepository;
import com.sprint.findex.service.IndexDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicIndexDataService implements IndexDataService {

    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;

    @Override
    @Transactional
    public IndexDataDto create(IndexDataCreateRequest request) {
        IndexInfo indexInfo = indexInfoRepository.findById(request.indexInfoId())
            .orElseThrow(() -> new IllegalArgumentException("IndexInfo not found"));

        IndexData indexData = IndexData.from(indexInfo, request, SourceType.USER);
        IndexData savedIndexData = indexDataRepository.save(indexData);

        return IndexDataMapper.toDto(savedIndexData);

    }
}
