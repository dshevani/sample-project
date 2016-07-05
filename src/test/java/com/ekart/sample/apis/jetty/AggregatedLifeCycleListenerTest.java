package com.sub.sample.apis.jetty;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.codahale.metrics.health.HealthCheck.Result;
import com.sub.sample.apis.dtos.health.VipStatus;
import com.sub.sample.apis.health.MasterHealthCheck;
import com.sub.sample.apis.jetty.AggregatedLifeCycleListener;
import com.google.common.collect.ImmutableMap;

import static org.mockito.Mockito.*;

/**
 * @author vijay.daniel
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AggregatedLifeCycleListenerTest {

   @Mock
   private MasterHealthCheck masterHealthCheck;

   @Mock
   private LifeCycle event;

   private AggregatedLifeCycleListener lifeCycleListener;

   @Before
   public void setUp() {

      lifeCycleListener = new AggregatedLifeCycleListener(masterHealthCheck);
   }

   @Test
   public void shouldRegisterComponent() {

      LifeCycle c1 = mock(LifeCycle.class);
      LifeCycle c2 = mock(LifeCycle.class);
      lifeCycleListener.register(c1);
      lifeCycleListener.register(c2);

      verify(c1).addLifeCycleListener(lifeCycleListener);
      verify(c2).addLifeCycleListener(lifeCycleListener);
      assertThat(lifeCycleListener.getComponentsStatus(), is(ImmutableMap.of(c1, false, c2, false)));
   }

   @Test
   public void shouldDeregisterComponent() {

      LifeCycle component = mock(LifeCycle.class);
      lifeCycleListener.register(component);

      lifeCycleListener.deregister(component);
      verify(component).removeLifeCycleListener(lifeCycleListener);
      assertThat(lifeCycleListener.getComponentsStatus(), is(ImmutableMap.of()));
   }

   @Test
   public void shouldSetVipStatusToInRotationIfDeepHealthCheckSucceedsAndAllComponentsAreStarted() {

      LifeCycle c1 = mock(LifeCycle.class);
      LifeCycle c2 = mock(LifeCycle.class);
      lifeCycleListener.register(c1);
      lifeCycleListener.register(c2);

      when(masterHealthCheck.doDeepCheck()).thenReturn(ImmutableMap.of("something", Result.healthy()));

      lifeCycleListener.lifeCycleStarted(c1);
      lifeCycleListener.lifeCycleStarted(c2);

      assertThat(lifeCycleListener.getComponentsStatus(), is(ImmutableMap.of(c1, true, c2, true)));
      verify(masterHealthCheck).setVipStatus(VipStatus.IN_ROTATION);
   }

   @Test
   public void shouldSetVipStatusToInRotationIfDeepHealthCheckHasNoResultsAndAllComponentsAreStarted() {

      LifeCycle c1 = mock(LifeCycle.class);
      LifeCycle c2 = mock(LifeCycle.class);
      lifeCycleListener.register(c1);
      lifeCycleListener.register(c2);

      when(masterHealthCheck.doDeepCheck()).thenReturn(ImmutableMap.of());

      lifeCycleListener.lifeCycleStarted(c1);
      lifeCycleListener.lifeCycleStarted(c2);

      assertThat(lifeCycleListener.getComponentsStatus(), is(ImmutableMap.of(c1, true, c2, true)));
      verify(masterHealthCheck).setVipStatus(VipStatus.IN_ROTATION);
   }

   @Test
   public void shouldNotSetVipStatusIfDeepHealthCheckFailsAndAllComponentsAreStarted() {

      LifeCycle c1 = mock(LifeCycle.class);
      LifeCycle c2 = mock(LifeCycle.class);
      lifeCycleListener.register(c1);
      lifeCycleListener.register(c2);

      when(masterHealthCheck.doDeepCheckAndUpdateCache())
            .thenReturn(ImmutableMap.of("s1", Result.healthy(), "s2", Result.unhealthy("s2")));

      lifeCycleListener.lifeCycleStarted(c1);
      lifeCycleListener.lifeCycleStarted(c2);

      assertThat(lifeCycleListener.getComponentsStatus(), is(ImmutableMap.of(c1, true, c2, true)));
      verify(masterHealthCheck, never()).setVipStatus(VipStatus.IN_ROTATION);
   }

   @Test
   public void shouldNotPerformDeepHealthCheckOrSetVipStatusIfAllComponentsAreNotStarted() {

      LifeCycle c1 = mock(LifeCycle.class);
      LifeCycle c2 = mock(LifeCycle.class);
      lifeCycleListener.register(c1);
      lifeCycleListener.register(c2);

      lifeCycleListener.lifeCycleStarted(c1);

      assertThat(lifeCycleListener.getComponentsStatus(), is(ImmutableMap.of(c1, true, c2, false)));
      verifyZeroInteractions(masterHealthCheck);
   }

   @Test
   public void shouldDoNothingWhenLifecycleIsStarting() {

      lifeCycleListener.lifeCycleStarting(event);
   }

   @Test
   public void shouldDoNothingWhenLifecycleFails() {

      lifeCycleListener.lifeCycleFailure(event, new RuntimeException());
   }

   @Test
   public void shouldDoNothingWhenLifecycleIsStopping() {

      lifeCycleListener.lifeCycleStopping(event);
   }

   @Test
   public void shouldDoNothingWhenLifecyleIsStopped() {

      lifeCycleListener.lifeCycleStopped(event);
   }
}
