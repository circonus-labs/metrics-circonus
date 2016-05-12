package com.circonus.metrics.circonus;

import com.circonus.metrics.circonus.HistImpl;
import com.circonus.metrics.circonus.HistImplContainer;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Histogram;

public class HistogramAlaCoda extends Histogram implements HistImplContainer {
    private final HistImpl  circonus_sub_histogram;

    public HistogramAlaCoda(Reservoir reservoir) {
        super(reservoir);
        this.circonus_sub_histogram = new HistImpl();
    }

    public HistImpl getHistImpl() {
        return circonus_sub_histogram;
    }

    public void update(int value) {
        update((long) value);
    }

    public void update(long value) {
        super.update(value);
        circonus_sub_histogram.insert(value);
    }
}
