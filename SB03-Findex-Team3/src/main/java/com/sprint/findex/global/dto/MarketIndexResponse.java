package com.sprint.findex.global.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class MarketIndexResponse {
    private MarketIndexResponseBody response;

    @Data
    public static class MarketIndexResponseBody {
        private MarketIndexBody body;
    }

    @Data
    public static class MarketIndexBody {
        private Items items;

        @Data
        public static class Items {
            private List<MarketIndexData> item;
        }
    }

    @Data
    public static class MarketIndexData {
        private String basDt;
        private BigDecimal mkp;
        private BigDecimal clpr;
        private BigDecimal hipr;
        private BigDecimal lopr;
        private BigDecimal vs;
        private BigDecimal fltRt;
        private Long trqu;
        private Long trPrc;
        private Long lstgMrktTotAmt;
        private String idxCsf;
        private String idxNm;
    }
}
