package com.sprint.findex.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record IndexDataSyncRequest(

    @NotEmpty(message = "지수 ID 리스트는 비어있을 수 없습니다.")
    List<Long> indexInfoIds,

    @NotNull(message = "시작 날짜는 null일 수 없습니다.")
    LocalDate baseDateFrom,

    @NotNull(message = "종료 날짜는 null일 수 없습니다.")
    LocalDate baseDateTo

) {}