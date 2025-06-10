package com.sprint.findex.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CursorPageResponseAutoSyncConfigDto(

        @Schema(name = "페이지 내용")
        List<AutoSyncConfigDto> content,

        @Schema(name = "다음 페이지 커서")
        String nextCursor,

        @Schema(name = "마지막 요소의 ID")
        String nextIdAfter,

        @Schema(name = "페이지 크기")
        int size,

        @Schema(name = "총 요소 수")
        long totalElements,

        @Schema(name = "다음 페이지 여부")
        boolean hasNext
) {
}
