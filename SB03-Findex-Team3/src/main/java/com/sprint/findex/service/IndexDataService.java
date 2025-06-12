package com.sprint.findex.service;

import com.sprint.findex.dto.dashboard.IndexChartDto;
import com.sprint.findex.dto.dashboard.IndexPerformanceDto;
import com.sprint.findex.dto.dashboard.RankedIndexPerformanceDto;
import com.sprint.findex.dto.request.IndexDataCreateRequest;
import com.sprint.findex.dto.request.IndexDataQueryParams;
import com.sprint.findex.dto.request.IndexDataUpdateRequest;
import com.sprint.findex.dto.response.CursorPageResponseIndexData;
import com.sprint.findex.dto.response.IndexDataDto;
import com.sprint.findex.entity.Period;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IndexDataService {

    IndexDataDto create(IndexDataCreateRequest request);

    IndexDataDto update(Long id, IndexDataUpdateRequest request);

    void delete(Long id);

    CursorPageResponseIndexData<IndexDataDto> findByCursor(IndexDataQueryParams params);

    List<IndexDataDto> findAllByConditions(IndexDataQueryParams params);

    List<IndexPerformanceDto> getFavoriteIndexPerformances(Period period);

    List<RankedIndexPerformanceDto> getIndexPerformanceRank(Long indexInfoId, Period period, int limit);

    IndexChartDto getIndexChart(Long indexInfoId, Period period);

}
