package org.lst.trading.lib.series;

import java.util.List;
import java.util.function.Function;

public class DoubleSeries extends TimeSeries<Double> {
    private String name;

    public DoubleSeries(String name) {
        super();
        this.name = name;
    }

    public DoubleSeries(List<Entry<Double>> data, String name) {
        super(data);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DoubleSeries merge(DoubleSeries other, MergeFunction<Double, Double> f) {
        return new DoubleSeries(TimeSeries.merge(this, other, f).getData(), name);
    }

    public DoubleSeries mapToDouble(Function<Double, Double> f) {
        return new DoubleSeries(map(f).getData(), name);
    }

    public DoubleSeries plus(DoubleSeries other) {
        return merge(other, Double::sum);
    }

    public DoubleSeries plus(double other) {
        return mapToDouble(x -> x + other);
    }

    public DoubleSeries mul(DoubleSeries other) {
        return merge(other, (x, y) -> x * y);
    }

    public DoubleSeries mul(double factor) {
        return mapToDouble(x -> x * factor);
    }

    public DoubleSeries div(DoubleSeries other) {
        return merge(other, (x, y) -> x / y);
    }

    public DoubleSeries returns() {
        return this.div(lag(1)).plus(-1);
    }

    public double getLast() {
        List<Entry<Double>> data = getData();
        return data.isEmpty() ? 0.0 : data.get(data.size() - 1).getItem();
    }

    public DoubleSeries tail(int n) {
        List<Entry<Double>> data = getData();
        return new DoubleSeries(data.subList(Math.max(0, data.size() - n), data.size()), name);
    }

    public DoubleSeries returns(int days) {
        return this.div(lag(days)).plus(-1);
    }

    public double[] toArray() {
        return stream().mapToDouble(Entry::getItem).toArray();
    }

    @Override public DoubleSeries toAscending() {
        return new DoubleSeries(super.toAscending().getData(), name);
    }

    @Override public DoubleSeries toDescending() {
        return new DoubleSeries(super.toDescending().getData(), name);
    }

    @Override public DoubleSeries lag(int k) {
        return new DoubleSeries(super.lag(k).getData(), name);
    }

    @Override public String toString() {
        List<Entry<Double>> data = getData();
        return data.isEmpty() ? "DoubleSeries{empty}" :
                "DoubleSeries{" +
                        "name=" + name +
                        ", from=" + data.get(0).getInstant() +
                        ", to=" + data.get(data.size() - 1).getInstant() +
                        ", size=" + data.size() +
                        '}';
    }
}
