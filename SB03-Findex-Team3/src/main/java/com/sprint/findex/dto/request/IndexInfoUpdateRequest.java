package com.sprint.findex.dto.request;

import java.time.LocalDate;

public record IndexInfoUpdateRequest(
    Integer employedItemsCount,
    LocalDate basePointInTime,
    Integer baseIndex,
    boolean favorite
) {

}
