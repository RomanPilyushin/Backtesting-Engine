package org.lst.trading.strategy.kalman;

import lombok.Getter;
import org.apache.commons.math3.stat.StatUtils;
import org.lst.trading.lib.model.Order;
import org.lst.trading.lib.model.TradingContext;
import org.lst.trading.lib.series.DoubleSeries;
import org.lst.trading.lib.series.MultipleDoubleSeries;
import org.lst.trading.lib.series.TimeSeries;
import org.lst.trading.lib.util.Util;
import org.lst.trading.strategy.AbstractTradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class CointegrationTradingStrategy extends AbstractTradingStrategy {
    private static final Logger log = LoggerFactory.getLogger(CointegrationTradingStrategy.class);

    private boolean reinvest = false;

    private final String xSymbol;
    private final String ySymbol;
    private TradingContext context;
    private Cointegration cointegration;

    private DoubleSeries alphaSeries;
    private DoubleSeries betaSeries;
    private DoubleSeries xSeries;
    private DoubleSeries ySeries;
    private DoubleSeries errorSeries;
    private DoubleSeries varianceSeries;
    private DoubleSeries modelSeries;

    private Order xOrder;
    private Order yOrder;

    public CointegrationTradingStrategy(String x, String y) {
        this(1, x, y);
    }

    public CointegrationTradingStrategy(double weight, String x, String y) {
        setWeight(weight);
        this.xSymbol = x;
        this.ySymbol = y;
    }

    @Override
    public void onStart(TradingContext context) {
        this.context = context;
        this.cointegration = new Cointegration(1e-10, 1e-7);
        initializeSeries();
    }

    private void initializeSeries() {
        alphaSeries = new DoubleSeries("alpha");
        betaSeries = new DoubleSeries("beta");
        xSeries = new DoubleSeries("x");
        ySeries = new DoubleSeries("y");
        errorSeries = new DoubleSeries("error");
        varianceSeries = new DoubleSeries("variance");
        modelSeries = new DoubleSeries("model");
    }

    @Override
    public void onTick() {
        double priceX = context.getLastPrice(xSymbol);
        double priceY = context.getLastPrice(ySymbol);
        double alpha = cointegration.getAlpha();
        double beta = cointegration.getBeta();

        cointegration.step(priceX, priceY);
        alphaSeries.add(alpha, context.getTime());
        betaSeries.add(beta, context.getTime());
        xSeries.add(priceX, context.getTime());
        ySeries.add(priceY, context.getTime());
        errorSeries.add(cointegration.getError(), context.getTime());
        varianceSeries.add(cointegration.getVariance(), context.getTime());

        double currentError = cointegration.getError();
        modelSeries.add(beta * priceX + alpha, context.getTime());

        if (errorSeries.size() > 30) {
            double[] recentErrors = errorSeries.reversedStream().mapToDouble(TimeSeries.Entry::getItem).limit(15).toArray();
            double standardDeviation = Math.sqrt(StatUtils.variance(recentErrors));

            if (yOrder == null && Math.abs(currentError) > standardDeviation) {
                double portfolioValue = reinvest ? context.getNetValue() : context.getInitialFunds();
                double baseAmount = (portfolioValue * getWeight() * 0.5 * Math.min(4, context.getLeverage())) / (priceY + beta * priceX);

                if (beta > 0 && baseAmount * beta >= 1) {
                    yOrder = context.order(ySymbol, currentError < 0, (int) baseAmount);
                    xOrder = context.order(xSymbol, currentError > 0, (int) (baseAmount * beta));
                }
            } else if (yOrder != null) {
                if (yOrder.isLong() && currentError > 0 || !yOrder.isLong() && currentError < 0) {
                    context.close(yOrder);
                    context.close(xOrder);

                    yOrder = null;
                    xOrder = null;
                }
            }
        }
    }


    @Override
    public void onEnd() {
        log.debug("Kalman filter statistics: " + Util.writeCsv(new MultipleDoubleSeries(xSeries, ySeries, alphaSeries, betaSeries, errorSeries, varianceSeries, modelSeries)));
    }

    @Override
    public String toString() {
        return "CointegrationStrategy{" +
                "ySymbol='" + ySymbol + '\'' +
                ", xSymbol='" + xSymbol + '\'' +
                '}';
    }

}
