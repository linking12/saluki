
package com.quancheng.saluki.core.grpc.cluster.metrics;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClusterClientMetrics {

    private static final String   METRIC_PREFIX = "google-cloud-bigtable.";
    private static MetricRegistry registry      = MetricRegistry.NULL_METRICS_REGISTRY;
    private static MetricLevel    levelToLog    = MetricLevel.Info;

    public enum MetricLevel {
                             Info(1), Debug(2), Trace(3);

        private final int level;

        MetricLevel(int level){
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * Sets a {@link MetricRegistry} to be used in all Bigtable connection created after the call. NOTE: this will not
     * update any existing connections.
     * 
     * @param registry
     */
    public static void setMetricRegistry(MetricRegistry registry) {
        ClusterClientMetrics.registry = registry;
    }

    public static MetricRegistry getMetricRegistry(MetricLevel level) {
        return isEnabled(level) ? registry : MetricRegistry.NULL_METRICS_REGISTRY;
    }

    /**
     * Creates a named {@link Counter}. This is a shortcut for
     * {@link ClusterClientMetrics#getMetricRegistry(MetricLevel)}. {@link MetricRegistry#counter(String)}.
     *
     * @return a {@link Counter}
     */
    public static Counter counter(MetricLevel level, String name) {
        return getMetricRegistry(level).counter(METRIC_PREFIX + name);
    }

    /**
     * Creates a named {@link Timer}. This is a shortcut for
     * {@link ClusterClientMetrics#getMetricRegistry(MetricLevel)}. {@link MetricRegistry#timer(String)}.
     *
     * @return a {@link Timer}
     */
    public static Timer timer(MetricLevel level, String name) {
        return getMetricRegistry(level).timer(METRIC_PREFIX + name);
    }

    /**
     * Creates a named {@link Meter}. This is a shortcut for
     * {@link ClusterClientMetrics#getMetricRegistry(MetricLevel)}. {@link MetricRegistry#meter(String)}.
     *
     * @return a {@link Meter}
     */
    public static Meter meter(MetricLevel level, String name) {
        return getMetricRegistry(level).meter(METRIC_PREFIX + name);
    }

    /**
     * Set a level at which to log. By default, the value is {@link MetricLevel#Info}.
     *
     * @param levelToLog
     */
    public static void setLevelToLog(MetricLevel levelToLog) {
        ClusterClientMetrics.levelToLog = levelToLog;
    }

    /** @return the levelToLog */
    public static MetricLevel getLevelToLog() {
        return levelToLog;
    }

    /**
     * Checks if a {@link MetricLevel} is enabled;
     *
     * @param level the {@link MetricLevel} to check
     * @return true if the level is enabled.
     */
    public static boolean isEnabled(MetricLevel level) {
        return levelToLog.getLevel() >= level.getLevel();
    }

    // Simplistic initialization via slf4j
    static {
        Logger logger = LoggerFactory.getLogger(ClusterClientMetrics.class);
        if (logger.isDebugEnabled()) {
            if (registry == MetricRegistry.NULL_METRICS_REGISTRY) {
                DropwizardMetricRegistry dropwizardRegistry = new DropwizardMetricRegistry();
                registry = dropwizardRegistry;
                DropwizardMetricRegistry.createSlf4jReporter(dropwizardRegistry, logger, 1, TimeUnit.MINUTES);
            } else if (registry instanceof DropwizardMetricRegistry) {
                DropwizardMetricRegistry dropwizardRegistry = (DropwizardMetricRegistry) registry;
                DropwizardMetricRegistry.createSlf4jReporter(dropwizardRegistry, logger, 1, TimeUnit.MINUTES);
            } else {
                logger.info("Could not set up logging since the metrics registry is not a DropwizardMetricRegistry; it is a %s w.",
                            registry.getClass().getName());
            }
        }
    }

    private ClusterClientMetrics(){
    }
}
