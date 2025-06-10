package com.sprint.findex.dto.response;

import java.util.List;
import java.util.stream.Collectors;

public class IndexDataCsvExporter {

    public static String toCsv(List<IndexDataDto> data) {
        String header = String.join(",", List.of(
            "id", "baseDate", "sourceType", "closingPrice", "marketPrice", "highPrice", "lowPrice",
            "versus", "fluctuationRate", "tradingQuantity", "tradingPrice", "marketTotalAmount"
        ));

        String rows = data.stream()
            .map(d -> String.join(",", List.of(
                d.id().toString(),
                d.baseDate().toString(),
                d.sourceType().name(),
                d.closingPrice().toString(),
                d.marketPrice().toString(),
                d.highPrice().toString(),
                d.lowPrice().toString(),
                d.versus().toString(),
                d.fluctuationRate().toString(),
                d.tradingQuantity().toString(),
                d.tradingPrice().toString(),
                d.marketTotalAmount().toString()
            )))
            .collect(Collectors.joining("\n"));

        return header + "\n" + rows;
    }
}
