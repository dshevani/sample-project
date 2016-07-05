package com.ekart.sample.apis.health;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.codahale.metrics.health.HealthCheck.Result;
import com.ekart.sample.apis.dtos.health.VipStatus;
import com.ekart.sample.apis.health.MasterHealthCheck;
import com.ekart.sample.apis.health.PeriodicHealthChecker;
import com.ekart.sample.apis.health.VipStatusCheck;
import com.google.common.collect.ImmutableMap;

import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author vijay.daniel
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MasterHealthCheckTest {

   @Mock
   private VipStatusCheck vipCheck;

   @Mock
   private PeriodicHealthChecker periodicHealthChecker;

   private MasterHealthCheck masterHealthCheck;

   @Before
   public void setUp() {

      masterHealthCheck = new MasterHealthCheck(vipCheck, periodicHealthChecker);
   }

   @Test
   public void shouldSetVipStatus() {

      masterHealthCheck.setVipStatus(VipStatus.IN_ROTATION);

      verify(vipCheck).setVipStatus(VipStatus.IN_ROTATION);
   }

   @Test
   public void shouldDoDeepCheck() {

      Map<String, Result> healthCheckResults = ImmutableMap.of("k1", Result.healthy());
      when(periodicHealthChecker.runHealthChecks()).thenReturn(healthCheckResults);

      assertThat(masterHealthCheck.doDeepCheck(), is(healthCheckResults));
   }

   @Test
   public void shouldReturnVipStatusOnlyForShallowCheckWhenLastRunsAreAbsent() {

      Result vipCheckResult = mock(Result.class);
      when(periodicHealthChecker.getLastRunResults()).thenReturn(ImmutableMap.of());
      when(vipCheck.execute()).thenReturn(vipCheckResult);

      assertThat(masterHealthCheck.doShallowCheckWithVipStatus(), is(ImmutableMap.of("vip_status", vipCheckResult)));
   }

   @Test
   public void shouldReturnConsolidatedStatusForShallowCheck() {

      Result vipCheckResult = mock(Result.class);
      when(periodicHealthChecker.getLastRunResults()).thenReturn(ImmutableMap.of("k1", Result.healthy()));
      when(vipCheck.execute()).thenReturn(vipCheckResult);

      assertThat(masterHealthCheck.doShallowCheckWithVipStatus(),
            is(ImmutableMap.of("vip_status", vipCheckResult, "k1", Result.healthy())));
   }

   @Test
   public void shouldDoDeepCheckUpdateCacheAndReturnResults() {

      Map<String, Result> healthCheckResults = ImmutableMap.of("k1", Result.healthy());
      when(periodicHealthChecker.runHealthChecksAndUpdateCache()).thenReturn(healthCheckResults);

      assertThat(masterHealthCheck.doDeepCheckAndUpdateCache(), is(healthCheckResults));

   }
}
