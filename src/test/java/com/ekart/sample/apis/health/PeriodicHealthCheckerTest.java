package com.sub.sample.apis.health;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Map;
import java.util.SortedMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.codahale.metrics.health.HealthCheck.Result;
import com.sub.sample.apis.health.PeriodicHealthChecker;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.collect.ImmutableSortedMap;

import static org.mockito.Mockito.*;

/**
 * @author vijay.daniel
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PeriodicHealthCheckerTest {

   @Mock
   private HealthCheckRegistry healthCheckRegistry;

   private PeriodicHealthChecker healthChecker;

   @Before
   public void setUp() {

      healthChecker = new PeriodicHealthChecker(healthCheckRegistry);
   }

   @Test
   public void shouldRunScheduledHealthChecksAndStoreInLastRunResults() {

      SortedMap<String, Result> scheduledRunResults = ImmutableSortedMap.of("s2", Result.healthy());

      when(healthCheckRegistry.runHealthChecks()).thenReturn(scheduledRunResults);

      healthChecker.runScheduledHealthChecks();
      assertThat(healthChecker.getLastRunResults(), is(scheduledRunResults));
   }

   @Test
   public void shouldNotAffectLastRunResultsForNonScheduledChecks() {

      SortedMap<String, Result> nonScheduledRunResults = ImmutableSortedMap.of("s2", Result.healthy());

      when(healthCheckRegistry.runHealthChecks()).thenReturn(nonScheduledRunResults);

      Map<String, Result> response = healthChecker.runHealthChecks();
      assertThat(response, is(nonScheduledRunResults));
      assertThat(healthChecker.getLastRunResults().isEmpty(), is(true));
   }

   @Test
   public void shouldRunHealthChecksAndUpdateCache() {

      SortedMap<String, Result> nonScheduledRunResults = ImmutableSortedMap.of("s2", Result.healthy());

      when(healthCheckRegistry.runHealthChecks()).thenReturn(nonScheduledRunResults);

      Map<String, Result> response = healthChecker.runHealthChecksAndUpdateCache();
      assertThat(response, is(nonScheduledRunResults));
      assertThat(healthChecker.getLastRunResults(), is(nonScheduledRunResults));
   }

   @Test
   public void shouldReturnEmptyResultsWhenNotStarted() {

      assertThat(healthChecker.getLastRunResults().isEmpty(), is(true));
   }
}
