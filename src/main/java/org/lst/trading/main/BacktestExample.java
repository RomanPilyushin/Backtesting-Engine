package org.lst.trading.main;

import org.lst.trading.lib.backtest.Backtest;
import org.lst.trading.lib.model.TradingStrategy;
import org.lst.trading.lib.series.DoubleSeries;
import org.lst.trading.lib.series.MultipleDoubleSeries;
import org.lst.trading.lib.util.AlphaVantageHistoricalPriceService;
import org.lst.trading.lib.util.DatabaseHelper;
import org.lst.trading.lib.util.HistoricalPriceService;
import org.lst.trading.lib.util.Util;
import org.lst.trading.strategy.kalman.CointegrationTradingStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class BacktestExample {
    private static final Logger LOGGER = Logger.getLogger(BacktestExample.class.getName());
    private static final String CONFIG_FILE = "config.properties";
    private static final String API_KEY_PROPERTY = "alphavantantage.apikey";
    private static String alphaVantageApiKey;
    private TradingStrategy strategy;
    private int deposit;
    private Backtest backtest;

    public static void main(String[] args) {
        try {
            loadProperties();
            new BacktestExample().runBacktest();
        } catch (Exception e) {
            LOGGER.severe("Error running backtest: " + e.getMessage());
        }
    }

    private void runBacktest() throws IOException {
        String x = "AMZN";
        String y = "LMT";
        strategy = new CointegrationTradingStrategy(x, y);
        HistoricalPriceService finance = new AlphaVantageHistoricalPriceService(alphaVantageApiKey);
        MultipleDoubleSeries priceSeries = getPriceSeries(finance, x, y);

        deposit = 15000;
        backtest = new Backtest(deposit, priceSeries);
        backtest.setLeverage(4);

        Backtest.Result result = backtest.run(strategy);
        displayResults(result, priceSeries);
    }

    private MultipleDoubleSeries getPriceSeries(HistoricalPriceService finance, String x, String y) throws IOException {
        DatabaseHelper db = new DatabaseHelper();

        DoubleSeries seriesX = fetchData(finance, db, x);
        DoubleSeries seriesY = fetchData(finance, db, y);

        return new MultipleDoubleSeries(seriesX, seriesY);
    }

    private DoubleSeries fetchData(HistoricalPriceService finance, DatabaseHelper db, String symbol) throws IOException {
        if (!db.isDataPresent(symbol)) {
            DoubleSeries series = finance.getHistoricalAdjustedPrices(symbol).toBlocking().first();
            db.insertData(symbol, doubleSeriesToString(series));
            return series;
        } else {
            String data = db.getData(symbol);
            return stringToDoubleSeries(data, symbol);
        }
    }


    private String doubleSeriesToString(DoubleSeries series) {
        StringBuilder sb = new StringBuilder();
        for (DoubleSeries.Entry<Double> entry : series) {
            sb.append(entry.getInstant().toString()).append(",").append(entry.getItem()).append("\n");
        }
        return sb.toString();
    }

    private DoubleSeries stringToDoubleSeries(String data, String seriesName) {
        List<DoubleSeries.Entry<Double>> entries = new ArrayList<>();
        String[] lines = data.split("\n");

        for (String line : lines) {
            String[] parts = line.split(",");
            Instant instant = Instant.parse(parts[0]);
            double value = Double.parseDouble(parts[1]);
            entries.add(new DoubleSeries.Entry<>(value, instant));
        }

        return new DoubleSeries(entries, seriesName);
    }

    private void displayResults(Backtest.Result result, MultipleDoubleSeries priceSeries) {
        String orders = formatOrders(result);
        LOGGER.info(orders);

        int days = priceSeries.size();
        LOGGER.info("\nBacktest result of " + strategy.getClass() + ": " + strategy);
        LOGGER.info("Prices: " + priceSeries);
        LOGGER.info(format(Locale.US, "Simulated %d days, Initial deposit %d, Leverage %f",
                priceSeries.size(), deposit, backtest.getLeverage()));
        LOGGER.info(format(Locale.US, "Commissions = %f", result.getCommissions()));
        LOGGER.info(format(Locale.US, "P/L = %.2f, Final value = %.2f, Result = %.2f%%, Annualized = %.2f%%, Sharpe (rf=0%%) = %.2f",
                result.getPl(), result.getFinalValue(), result.getReturn() * 100, result.getReturn() / (days / 251.) * 100, result.getSharpe()));

        LOGGER.info("Orders: " + Util.writeStringToTempFile(orders));
        LOGGER.info("Statistics: " + Util.writeCsv(new MultipleDoubleSeries(result.getPlHistory(), result.getMarginHistory())));
    }

    private static String formatOrders(Backtest.Result result) {
        return result.getOrders().stream()
                .map(order -> format(Locale.US, "%d,%d,%s,%s,%s,%s,%f,%f,%f",
                        order.getId(), Math.abs(order.getAmount()), order.isLong() ? "Buy" : "Sell",
                        order.getInstrument(), order.getOpenInstant(), order.getCloseInstant(),
                        order.getOpenPrice(), order.getClosePrice(), order.getPl()))
                .collect(Collectors.joining("\n", "id,amount,side,instrument,from,to,open,close,pl\n", ""));
    }

    private static void loadProperties() {
        try (InputStream input = BacktestExample.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            Properties prop = new Properties();

            if (input == null) {
                LOGGER.warning("Sorry, unable to find " + CONFIG_FILE);
                return;
            }

            prop.load(input);
            alphaVantageApiKey = prop.getProperty(API_KEY_PROPERTY);
        } catch (IOException ex) {
            LOGGER.severe("Error loading properties: " + ex.getMessage());
        }
    }
}
