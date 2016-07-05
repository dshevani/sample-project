package com.sub.sample.apis.controller;

import static org.hamcrest.MatcherAssert.*;
import static org.unitils.reflectionassert.ReflectionAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.codahale.metrics.health.HealthCheck.Result;
import com.sub.sample.apis.controller.HealthCheckController;
import com.sub.sample.apis.dtos.health.ElbHealthCheckResponse;
import com.sub.sample.apis.dtos.health.HealthCheckResponse;
import com.sub.sample.apis.dtos.health.HealthStatus;
import com.sub.sample.apis.dtos.health.VipConfiguration;
import com.sub.sample.apis.dtos.health.VipStatus;
import com.sub.sample.apis.health.MasterHealthCheck;
import com.sub.sample.apis.metrics.ElbStatisticsCollector;
import com.google.common.collect.ImmutableMap;

import static org.mockito.Mockito.*;

/**
 * These tests just check the response code. They don't access the Response body
 * to keep it simple
 * 
 * @author vijay.daniel
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class HealthCheckControllerTest {

   @Mock
   private MasterHealthCheck masterHealthCheck;

   @Mock
   private ElbStatisticsCollector elbStatisticsCollector;

   private HealthCheckController controller;

   @Before
   public void setUp() {

      controller = new HealthCheckController(masterHealthCheck, elbStatisticsCollector);
   }

   @Test
   public void shouldReturn500WhenShallowHealthCheckFailsForElbHealthCheck() {

      when(elbStatisticsCollector.getCapacity()).thenReturn(100L);
      when(elbStatisticsCollector.getTotalRequestsSinceUptime()).thenReturn(200L);
      when(elbStatisticsCollector.getUptimeInSeconds()).thenReturn(300L);

      when(masterHealthCheck.doShallowCheckWithVipStatus())
            .thenReturn(ImmutableMap.of("k1", Result.healthy(), "k2", Result.unhealthy("failed")));

      ResponseEntity<ElbHealthCheckResponse> response = controller.getElbHealthCheckResults();
      assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
      assertReflectionEquals(response.getBody(), new ElbHealthCheckResponse(300L, 200L, 100L));
   }

   @Test
   public void shouldReturn200IfShallowHealthCheckResultIsEmptyForElbHealthCheck() {

      when(elbStatisticsCollector.getCapacity()).thenReturn(100L);
      when(elbStatisticsCollector.getTotalRequestsSinceUptime()).thenReturn(200L);
      when(elbStatisticsCollector.getUptimeInSeconds()).thenReturn(300L);

      when(masterHealthCheck.doShallowCheckWithVipStatus()).thenReturn(ImmutableMap.of());

      ResponseEntity<ElbHealthCheckResponse> response = controller.getElbHealthCheckResults();
      assertThat(response.getStatusCode(), is(HttpStatus.OK));
      assertReflectionEquals(response.getBody(), new ElbHealthCheckResponse(300L, 200L, 100L));
   }

   @Test
   public void shouldReturn200IfShallowHealthCheckSucceedsForElbHealthCheck() {

      when(elbStatisticsCollector.getCapacity()).thenReturn(100L);
      when(elbStatisticsCollector.getTotalRequestsSinceUptime()).thenReturn(200L);
      when(elbStatisticsCollector.getUptimeInSeconds()).thenReturn(300L);

      when(masterHealthCheck.doShallowCheckWithVipStatus())
            .thenReturn(ImmutableMap.of("k1", Result.healthy(), "k2", Result.healthy()));

      ResponseEntity<ElbHealthCheckResponse> response = controller.getElbHealthCheckResults();
      assertThat(response.getStatusCode(), is(HttpStatus.OK));
      assertReflectionEquals(response.getBody(), new ElbHealthCheckResponse(300L, 200L, 100L));
   }

   @Test
   public void shouldChangeVipStatus() {

      ResponseEntity<String> response = controller.changeVipStatus(new VipConfiguration(VipStatus.IN_ROTATION));
      assertThat(response.getStatusCode(), is(HttpStatus.OK));
   }

   @Test
   public void shouldReturn500IfShallowHealthCheckFails() {

      when(masterHealthCheck.doShallowCheckWithVipStatus())
            .thenReturn(ImmutableMap.of("k1", Result.healthy(), "k2", Result.unhealthy("failed")));

      ResponseEntity<HealthCheckResponse> response = controller.getShallowHealthCheckResults();
      assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
      assertReflectionEquals(response.getBody(), new HealthCheckResponse(HealthStatus.UNHEALTHY,
            ImmutableMap.of("k1", HealthStatus.HEALTHY, "k2", HealthStatus.UNHEALTHY)));
   }

   @Test
   public void shouldReturn200IfShallowHealthCheckResultIsEmpty() {

      when(masterHealthCheck.doShallowCheckWithVipStatus()).thenReturn(ImmutableMap.of());

      ResponseEntity<HealthCheckResponse> response = controller.getShallowHealthCheckResults();
      assertThat(response.getStatusCode(), is(HttpStatus.OK));
      assertReflectionEquals(response.getBody(), new HealthCheckResponse(HealthStatus.HEALTHY, ImmutableMap.of()));
   }

   @Test
   public void shouldReturn200IfAllShallowHealthCheckSucceeds() {

      when(masterHealthCheck.doShallowCheckWithVipStatus())
            .thenReturn(ImmutableMap.of("k1", Result.healthy(), "k2", Result.healthy()));

      ResponseEntity<HealthCheckResponse> response = controller.getShallowHealthCheckResults();
      assertThat(response.getStatusCode(), is(HttpStatus.OK));
      assertReflectionEquals(response.getBody(), new HealthCheckResponse(HealthStatus.HEALTHY,
            ImmutableMap.of("k1", HealthStatus.HEALTHY, "k2", HealthStatus.HEALTHY)));
   }

   @Test
   public void shouldReturn500IfDeepHealthCheckFails() {

      when(masterHealthCheck.doDeepCheck())
            .thenReturn(ImmutableMap.of("k1", Result.healthy(), "k2", Result.unhealthy("failed")));

      ResponseEntity<HealthCheckResponse> response = controller.getDeepHealthCheckResults();
      assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
      assertReflectionEquals(response.getBody(), new HealthCheckResponse(HealthStatus.UNHEALTHY,
            ImmutableMap.of("k1", HealthStatus.HEALTHY, "k2", HealthStatus.UNHEALTHY)));

   }

   @Test
   public void shouldReturn200IfDeepHealthCheckResultIsEmpty() {

      when(masterHealthCheck.doDeepCheck()).thenReturn(ImmutableMap.of());

      ResponseEntity<HealthCheckResponse> response = controller.getDeepHealthCheckResults();
      assertThat(response.getStatusCode(), is(HttpStatus.OK));
      assertReflectionEquals(response.getBody(), new HealthCheckResponse(HealthStatus.HEALTHY, ImmutableMap.of()));
   }

   @Test
   public void shouldReturn200IfAllDeepHealthCheckSucceeds() {

      when(masterHealthCheck.doDeepCheck()).thenReturn(ImmutableMap.of("k1", Result.healthy(), "k2", Result.healthy()));

      ResponseEntity<HealthCheckResponse> response = controller.getDeepHealthCheckResults();
      assertThat(response.getStatusCode(), is(HttpStatus.OK));
      assertReflectionEquals(response.getBody(), new HealthCheckResponse(HealthStatus.HEALTHY,
            ImmutableMap.of("k1", HealthStatus.HEALTHY, "k2", HealthStatus.HEALTHY)));
   }
}
