package org.lst.trading.strategy.kalman;

import lombok.Getter;
import lombok.Setter;
import org.la4j.LinearAlgebra;
import org.la4j.Matrix;
import org.la4j.matrix.DenseMatrix;

@Getter
@Setter
public class KalmanFilter {
    private final int stateCount; // n
    private final int sensorCount; // m

    private Matrix state; // x, state estimate
    private Matrix stateCovariance; // Covariance matrix of x, process noise (w)

    private Matrix updateMatrix; // F, State transition matrix.
    private Matrix updateCovariance; // Q, Estimated error in process.
    private Matrix moveVector; // u, Control vector

    private Matrix measurement;
    private Matrix measurementCovariance; // R, Covariance matrix of the measurement vector z
    private Matrix extractionMatrix; // H, Observation matrix.

    private Matrix innovation;
    private Matrix innovationCovariance;

    public KalmanFilter(int stateCount, int sensorCount) {
        this.stateCount = stateCount;
        this.sensorCount = sensorCount;
        this.moveVector = Matrix.zero(stateCount, 1);
    }

    private void step() {
        // Prediction
        Matrix predictedState = updateMatrix.multiply(state).add(moveVector);
        Matrix predictedStateCovariance = updateMatrix.multiply(stateCovariance).multiply(updateMatrix.transpose()).add(updateCovariance);

        // Observation
        innovation = measurement.subtract(extractionMatrix.multiply(predictedState));
        innovationCovariance = extractionMatrix.multiply(predictedStateCovariance).multiply(extractionMatrix.transpose()).add(measurementCovariance);

        // Update
        Matrix kalmanGain = predictedStateCovariance.multiply(extractionMatrix.transpose()).multiply(innovationCovariance.withInverter(LinearAlgebra.InverterFactory.SMART).inverse());
        state = predictedState.add(kalmanGain.multiply(innovation));
        stateCovariance = DenseMatrix.identity(stateCovariance.rows()).subtract(kalmanGain.multiply(extractionMatrix)).multiply(predictedStateCovariance);
    }

    public void step(Matrix measurement, Matrix move) {
        this.measurement = measurement;
        this.moveVector = move;
        step();
    }

    public void step(Matrix measurement) {
        this.measurement = measurement;
        step();
    }
}
