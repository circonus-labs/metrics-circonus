package com.circonus.metrics.circonus;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.circonus.metrics.circonus.HistogramAlaCoda;
import com.circonus.metrics.circonus.TimerAlaCoda;

public class CirconusMetricRegistryAlaCoda extends MetricRegistry {

    @Override
    public Histogram histogram(String name) {
        Histogram existed = (Histogram) getMetrics().get(name);
        if(existed != null) return existed;
        return register(name, new HistogramAlaCoda(new ExponentiallyDecayingReservoir()));
    }
    @Override
    public Timer timer(String name) {
        Timer existed = (Timer) getMetrics().get(name);
        if(existed != null) return existed;
        return register(name, new TimerAlaCoda());
    }
}
