package com.sprint.findex.dto.response;

public record IndexDataSyncResponse(
    Long id,                // 작업 ID
    String jobType,         // 작업 유형 (INDEX_DATA)
    Long indexInfoId,       // 지수 정보 ID
    String targetDate,      // 대상 날짜
    String worker,          // 작업자 (IP 또는 'system')
    String jobTime,         // 작업 일시 (ISO 8601 형식)
    String result           // 작업 결과 (성공, 실패)
) {}
