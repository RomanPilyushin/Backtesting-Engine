package org.lst.trading.lib.series;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MultipleDoubleSeries extends TimeSeries<List<Double>> {
    private List<String> names;

    public MultipleDoubleSeries(Collection<String> names) {
        super();
        this.names = new ArrayList<>(names);
    }

    public MultipleDoubleSeries(DoubleSeries... series) {
        super();
        this.names = new ArrayList<>();
        for (DoubleSeries s : series) {
            addSeries(s);
        }
    }

    private void initializeWithSeries(DoubleSeries series) {
        List<Entry<List<Double>>> entries = new ArrayList<>();
        for (Entry<Double> entry : series.getData()) {
            List<Double> list = new ArrayList<>();
            list.add(entry.getItem());
            entries.add(new Entry<>(list, entry.getInstant()));
        }
        // Use the protected constructor to set mData
        mData = new ArrayList<>(entries);
        names.add(series.getName());
    }

    public void addSeries(DoubleSeries series) {
        TimeSeries<List<Double>> merged = TimeSeries.merge(this, series, (l, t) -> {
            l.add(t);
            return l;
        });
        // Use the protected constructor to set mData
        mData = new ArrayList<>(merged.getData());
        names.add(series.getName());
    }

    public DoubleSeries getColumn(String name) {
        int index = names.indexOf(name);
        List<Entry<Double>> entries = mData.stream()
                .map(entry -> new Entry<>(entry.getItem().get(index), entry.getInstant()))
                .collect(Collectors.toList());

        return new DoubleSeries(entries, name);
    }

    public int indexOf(String name) {
        return names.indexOf(name);
    }

    public List<String> getNames() {
        return new ArrayList<>(names);
    }

    @Override
    public String toString() {
        return mData.isEmpty() ? "MultipleDoubleSeries{empty}" :
                "MultipleDoubleSeries{" +
                        "names={" + String.join(", ", names) +
                        "}, from=" + mData.get(0).getInstant() +
                        ", to=" + mData.get(mData.size() - 1).getInstant() +
                        ", size=" + mData.size() +
                        '}';
    }
}
