package com.objectist.alphavantage;

import org.patriques.output.timeseries.TimeSeriesResponse;
import org.patriques.output.timeseries.data.StockData;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockContent {

    public static final List<StockItem> ITEMS = new ArrayList<StockItem>();
    public static final Map<String, StockItem> ITEM_MAP = new HashMap<>();

    public static void update(final List<TimeSeriesResponse> timeSeriesResponseList) {
        for (TimeSeriesResponse timeSeriesResponse : timeSeriesResponseList) {
            final String id = timeSeriesResponse.getMetaData().get("2. Symbol");
            final StockData stockData = timeSeriesResponse.getStockData().get(0);
            final StockItem stockItem = createStockItem(id, stockData);
            addItem(stockItem);
        }
    }

    private static void addItem(final StockItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static StockItem createStockItem(final String id, StockData stockData) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String content = stockData.getDateTime().format(dateTimeFormatter)
                .concat("    $" + Double.toString(stockData.getClose()));
        String details = "open:   " + Double.toString(stockData.getOpen())
                .concat("\nhigh:   ") + Double.toString(stockData.getHigh())
                .concat("\nlow:   ") + Double.toString(stockData.getLow())
                .concat("\nclose:   ") + Double.toString(stockData.getClose())
                .concat("\nvolume:   ") + Double.toString(stockData.getVolume());
        return new StockItem(id, content, details);
    }

    /**
     * Normally a stock item would be represented by a model object. Taking some shortcuts here.
     */
    public static class StockItem {
        public final String id;
        public final String content;
        public final String details;

        public StockItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
