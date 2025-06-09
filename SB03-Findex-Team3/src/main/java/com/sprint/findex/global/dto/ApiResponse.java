package com.sprint.findex.global.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {

    @JsonProperty("response")
    private ResponseData response;

    public ResponseData getResponse() { return response; }
    public void setResponse(ResponseData response) { this.response = response; }

    public Header getHeader() {
        return response != null ? response.getHeader() : null;
    }

    public Body getBody() {
        return response != null ? response.getBody() : null;
    }

    public static class ResponseData {
        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private Body body;

        public Header getHeader() { return header; }
        public void setHeader(Header header) { this.header = header; }

        public Body getBody() { return body; }
        public void setBody(Body body) { this.body = body; }
    }

    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;

        public String getResultCode() { return resultCode; }
        public void setResultCode(String resultCode) { this.resultCode = resultCode; }

        public String getResultMsg() { return resultMsg; }
        public void setResultMsg(String resultMsg) { this.resultMsg = resultMsg; }
    }

    public static class Body {
        @JsonProperty("numOfRows")
        private int numOfRows;

        @JsonProperty("pageNo")
        private int pageNo;

        @JsonProperty("totalCount")
        private int totalCount;

        @JsonProperty("items")
        private Items items;

        public int getNumOfRows() { return numOfRows; }
        public void setNumOfRows(int numOfRows) { this.numOfRows = numOfRows; }

        public int getPageNo() { return pageNo; }
        public void setPageNo(int pageNo) { this.pageNo = pageNo; }

        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

        public Items getItems() { return items; }
        public void setItems(Items items) { this.items = items; }
    }

    public static class Items {
        @JsonProperty("item")
        private List<StockIndexItem> item;

        public List<StockIndexItem> getItem() { return item; }
        public void setItem(List<StockIndexItem> item) { this.item = item; }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockIndexItem {

        @JsonProperty("idxCsf")
        private String indexClassification;

        @JsonProperty("idxNm")
        private String indexName;

        @JsonProperty("epyItmsCnt")
        private String employedItemsCount;

        @JsonProperty("basPntm")
        private String basePointTime;

        @JsonProperty("basIdx")
        private String baseIndex;

        public String getIndexClassification() {
            return indexClassification;
        }
        public void setIndexClassification(String indexClassification) {
            this.indexClassification = indexClassification;
        }

        public String getIndexName() {
            return indexName;
        }
        public void setIndexName(String indexName) {
            this.indexName = indexName;
        }

        public String getEmployedItemsCount() {
            return employedItemsCount;
        }
        public void setEmployedItemsCount(String employedItemsCount) {
            this.employedItemsCount = employedItemsCount;
        }

        public String getBasePointTime() {
            return basePointTime;
        }
        public void setBasePointTime(String basePointTime) {
            this.basePointTime = basePointTime;
        }

        public String getBaseIndex() {
            return baseIndex;
        }
        public void setBaseIndex(String baseIndex) {
            this.baseIndex = baseIndex;
        }
    }
}
