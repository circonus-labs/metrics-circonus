package com.circonus.metrics.circonus;

/*
https://github.com/dropwizard/metrics/blob/3.1-maintenance/metrics-core/src/main/java/com/codahale/metrics/Timer.java
*/

import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.circonus.metrics.circonus.HistImpl;
import com.circonus.metrics.circonus.HistImplContainer;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Clock;
import com.codahale.metrics.Timer;
import com.codahale.metrics.ExponentiallyDecayingReservoir;

public class TimerAlaCoda extends Timer implements HistImplContainer {
    private final HistImpl circonus_sub_histogram;

    public TimerAlaCoda() {
        this(new ExponentiallyDecayingReservoir());
    }

    public TimerAlaCoda(Reservoir reservoir) {
        this(reservoir, Clock.defaultClock());
    }

    public TimerAlaCoda(Reservoir reservoir, Clock clock) {
        super(reservoir, clock);
        this.circonus_sub_histogram = new HistImpl();
    }

    @Override
    public void update(long duration, TimeUnit unit) {
        super.update(duration, unit);

        long nanos = unit.toNanos(duration);
        if(nanos >= 0)
          circonus_sub_histogram.insert((double)duration/1000000000.0);
    }

    public HistImpl getHistImpl() {
        return circonus_sub_histogram;
    }
}
