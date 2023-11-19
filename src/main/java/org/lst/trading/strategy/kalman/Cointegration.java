package org.lst.trading.strategy.kalman;

import lombok.Getter;
import lombok.Setter;
import org.la4j.Matrix;

@Getter
@Setter
public class Cointegration {
    private double delta;
    private double rVariance;
    private KalmanFilter filter;
    private final int numberOfObservations = 2;

    public Cointegration(double delta, double rVariance) {
        this.delta = delta;
        this.rVariance = rVariance;

        Matrix processNoise = Matrix.identity(numberOfObservations).multiply(delta / (1 - delta));
        Matrix stateTransitionMatrix = Matrix.identity(numberOfObservations);

        Matrix initialState = Matrix.zero(numberOfObservations, 1);

        filter = new KalmanFilter(numberOfObservations, 1);
        filter.setUpdateMatrix(stateTransitionMatrix);
        filter.setState(initialState);
        filter.setStateCovariance(Matrix.zero(numberOfObservations, numberOfObservations));
        filter.setUpdateCovariance(processNoise);
        filter.setMeasurementCovariance(Matrix.constant(1, 1, rVariance));
    }

    public void step(double x, double y) {
        filter.setExtractionMatrix(Matrix.from1DArray(1, 2, new double[]{1, x}));
        filter.step(Matrix.constant(1, 1, y));
    }

    public double getAlpha() {
        return filter.getState().getRow(0).get(0);
    }

    public double getBeta() {
        return filter.getState().getRow(1).get(0);
    }

    public double getVariance() {
        return filter.getInnovationCovariance().get(0, 0);
    }

    public double getError() {
        return filter.getInnovation().get(0, 0);
    }
}
