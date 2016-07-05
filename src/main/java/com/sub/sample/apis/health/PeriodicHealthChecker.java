package com.sub.sample.apis.health;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.codahale.metrics.health.HealthCheck.Result;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.collect.ImmutableMap;

/**
 * As our health checks become more and more complex, we don't want the health
 * check APIs latency to go up proportionately. So, we'll periodically calculate
 * the health of our system, and just return the last run's results in the
 * health check API.
 * 
 * @author vijay.daniel
 *
 */
@Component
@ThreadSafe
@ParametersAreNonnullByDefault
public class PeriodicHealthChecker {

   private static final Logger LOGGER = LoggerFactory.getLogger(PeriodicHealthChecker.class);

   private final HealthCheckRegistry healthCheckRegistry;
   private final AtomicReference<Map<String, Result>> lastRunResults = new AtomicReference<>(ImmutableMap.of());

   @Inject
   public PeriodicHealthChecker(HealthCheckRegistry healthCheckRegistry) {

      this.healthCheckRegistry = healthCheckRegistry;
   }

   // NOTE: We are using fixedDelay instead of fixedRate because we are
   // interested in the entire result, not partial ones.
   @Scheduled(fixedDelayString = "${periodicHealthCheck.fixedDelayMs}")
   public synchronized void runScheduledHealthChecks() {

      runHealthChecksAndUpdateCache();
   }

   public Map<String, Result> runHealthChecks() {

      return healthCheckRegistry.runHealthChecks();
   }

   public synchronized Map<String, Result> runHealthChecksAndUpdateCache() {

      Map<String, Result> healthCheckResults = runHealthChecks();
      Map<String, Result> immutableResults = ImmutableMap.<String, Result>builder().putAll(healthCheckResults).build();
      lastRunResults.set(immutableResults);

      LOGGER.debug("Results of deep health check: {}", immutableResults);
      return immutableResults;
   }

   public Map<String, Result> getLastRunResults() {

      return lastRunResults.get();
   }
}
