package com.sprint.findex.entity;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
// TODO 
// Entity
// Table
public class AutoSyncConfig {

    private UUID id; // 해당 지수 ID

    private boolean enabled; // 활성화

    private UUID indexInfoId;

    private String indexClassification;

    private String indexName;
}
