
package com.quancheng.saluki.core.grpc.cluster.metrics;

import com.codahale.metrics.Counting;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.Slf4jReporter;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

public class DropwizardMetricRegistry implements MetricRegistry {

    private final com.codahale.metrics.MetricRegistry registry = new com.codahale.metrics.MetricRegistry();

    public static void createSlf4jReporter(DropwizardMetricRegistry registry, Logger logger, long period,
                                           TimeUnit unit) {
        MetricFilter nonZeroMatcher = new MetricFilter() {

            @Override
            public boolean matches(String name, Metric metric) {
                if (metric instanceof Counting) {
                    Counting counter = (Counting) metric;
                    return counter.getCount() > 0;
                }
                return true;
            }
        };
        Slf4jReporter.forRegistry(registry.getRegistry()).outputTo(logger).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).filter(nonZeroMatcher).build().start(period,
                                                                                                                                                                                           unit);
    }

    @Override
    public Counter counter(String name) {
        final com.codahale.metrics.Counter counter = registry.counter(name);
        return new Counter() {

            @Override
            public void inc() {
                counter.inc();
            }

            @Override
            public void dec() {
                counter.dec();
            }
        };
    }

    @Override
    public Timer timer(String name) {
        final com.codahale.metrics.Timer timer = registry.timer(name);
        return new Timer() {

            @Override
            public Timer.Context time() {
                final com.codahale.metrics.Timer.Context timerContext = timer.time();
                return new Context() {

                    @Override
                    public void close() {
                        timerContext.close();
                    }
                };
            }

            @Override
            public void update(long duration, TimeUnit unit) {
                timer.update(duration, unit);
            }
        };
    }

    @Override
    public Meter meter(String name) {
        final com.codahale.metrics.Meter meter = registry.meter(name);
        return new Meter() {

            @Override
            public void mark() {
                meter.mark();
            }

            @Override
            public void mark(long size) {
                meter.mark(size);
            }
        };
    }

    public com.codahale.metrics.MetricRegistry getRegistry() {
        return registry;
    }
}
