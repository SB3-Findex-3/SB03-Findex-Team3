package com.sprint.findex.global.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {

    public static String getClientIp(HttpServletRequest request) {
        // IP 주소 가져오기
        String clientIp = request.getRemoteAddr();

        return clientIp;
    }
}