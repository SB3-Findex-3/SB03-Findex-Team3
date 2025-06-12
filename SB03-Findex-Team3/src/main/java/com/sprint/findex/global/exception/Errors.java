package com.sprint.findex.global.exception;

import lombok.Getter;

@Getter
public enum Errors {

    UNKNOWN_ERROR(500, "E000", "알 수 없는 에러가 발생했습니다.", "서버 내부 오류"),

    INDEX_INFO_NOT_FOUND(404, "I001", "지수 정보가 존재하지 않습니다.", "지수 코드가 잘못되었거나 삭제되었습니다."),
    INDEX_DATA_NOT_FOUND(404, "I002", "지수 데이터가 존재하지 않습니다.", "해당 지수의 데이터가 없습니다."),
    AUTO_SYNC_NOT_FOUND(404, "S001", "자동 연동 설정이 없습니다.", "해당 자동 동기화 설정을 찾을 수 없습니다."),
    SYNC_JOB_HISTORY_NOT_FOUND(404, "S002", "연동 이력이 없습니다.", "요청한 동기화 이력이 존재하지 않습니다."),

    INDEX_INFO_BAD_REQUEST(400, "I003", "지수 정보 요청이 잘못되었습니다.", "필수 파라미터가 누락되었거나 잘못되었습니다."),
    INDEX_DATA_BAD_REQUEST(400, "I004", "지수 데이터 요청이 잘못되었습니다.", "요청 형식을 확인하세요."),
    AUTO_SYNC_BAD_REQUEST(400, "S003", "자동 연동 요청이 잘못되었습니다.", "설정 값이 유효하지 않습니다."),
    SYNC_JOB_HISTORY_BAD_REQUEST(400, "S004", "연동 이력 요청이 잘못되었습니다.", "조회 조건이 유효하지 않습니다."),
    INVALID_CURSOR(400, "S005", "유효하지 않은 커서입니다.", "Base64 디코딩 혹은 JSON 파싱 실패"),

    INDEX_INFO_ALREADY_EXISTS(409, "I005", "지수 정보가 이미 존재합니다.", "중복된 지수 코드입니다."),
    INDEX_DATA_ALREADY_EXISTS(409, "I006", "지수 데이터가 이미 존재합니다.", "이미 저장된 데이터입니다."),
    AUTO_SYNC_ALREADY_EXISTS(409, "S006", "자동 연동 설정이 이미 존재합니다.", "중복된 설정입니다."),
    SYNC_JOB_HISTORY_ALREADY_EXISTS(409, "S007", "연동 이력이 이미 존재합니다.", "중복된 요청입니다."),

    INTERNAL_SERVER_ERROR(500, "E001", "내부 서버 오류", "서버에서 처리 중 오류 발생");

    private final int status;
    private final String code;
    private final String message;
    private final String detail;

    Errors(int status, String code, String message, String detail) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.detail = detail;
    }
}