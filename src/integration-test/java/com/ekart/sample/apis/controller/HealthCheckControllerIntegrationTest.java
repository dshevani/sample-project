package com.ekart.sample.apis.controller;

import static org.hamcrest.MatcherAssert.*;
import static org.unitils.reflectionassert.ReflectionAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ekart.sample.apis.BaseIntegrationTest;
import com.ekart.sample.apis.dtos.health.ElbHealthCheckResponse;
import com.ekart.sample.apis.dtos.health.HealthCheckResponse;
import com.ekart.sample.apis.dtos.health.HealthStatus;
import com.ekart.sample.apis.dtos.health.VipConfiguration;
import com.google.common.collect.ImmutableMap;

public class HealthCheckControllerIntegrationTest extends BaseIntegrationTest {

   @Test
   public void shouldReturn400IfVipStatusIsNull() throws Exception {

      HttpEntity<VipConfiguration> request = new HttpEntity<>(new VipConfiguration(), newHeader());
      ResponseEntity<String> response = CLIENT.exchange(url("/elb-healthcheck"), HttpMethod.PUT, request, String.class);
      assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
   }

   @Test
   public void shouldReturn400IfVipStatusIsNotRecognized() throws Exception {

      HttpEntity<Map<String, String>> request = new HttpEntity<>(ImmutableMap.of("vip_status", "someStatus"),
            newHeader());
      ResponseEntity<String> response = CLIENT.exchange(url("/elb-healthcheck"), HttpMethod.PUT, request, String.class);
      assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
   }

   // NOTE: We won't be running tests for changing the VIP status as we don't
   // want to take any Alpha stage hosts OOR from behind their VIPs :)

   @Test
   public void shouldReturnSufficientDataForElbHealthCheckWhenShallowHealthCheckSucceeds() throws Exception {

      ResponseEntity<ElbHealthCheckResponse> response = CLIENT.getForEntity(url("/elb-healthcheck"),
            ElbHealthCheckResponse.class);

      // Of course, we can't validate the exact values! :)
      assertThat(response.getBody().getCapacity(), notNullValue());
      assertThat(response.getBody().getRequests(), notNullValue());
      assertThat(response.getBody().getUptime(), notNullValue());

      assertThat(response.getStatusCode(), is(HttpStatus.OK));
   }

   @Test
   public void shouldReturnSuccessIfShallowHealthCheckSucceeds() throws Exception {

      ResponseEntity<HealthCheckResponse> response = CLIENT.getForEntity(url("/elb-healthcheck/shallow"),
            HealthCheckResponse.class);

      HealthCheckResponse expected = new HealthCheckResponse();
      expected.setServiceStatus(HealthStatus.HEALTHY);
      expected.setComponentStatuses(
            ImmutableMap.of("vip_status", HealthStatus.HEALTHY, "threadDeadlockDetector", HealthStatus.HEALTHY));

      assertThat(response.getStatusCode(), is(HttpStatus.OK));
      assertReflectionEquals(response.getBody(), expected);
   }

   @Test
   public void shouldReturnSuccessIfDeepHealthCheckSucceeds() throws Exception {

      ResponseEntity<HealthCheckResponse> response = CLIENT.getForEntity(url("/elb-healthcheck/deep"),
            HealthCheckResponse.class);

      HealthCheckResponse expected = new HealthCheckResponse();
      expected.setServiceStatus(HealthStatus.HEALTHY);
      expected.setComponentStatuses(ImmutableMap.of("threadDeadlockDetector", HealthStatus.HEALTHY));

      assertThat(response.getStatusCode(), is(HttpStatus.OK));
      assertReflectionEquals(response.getBody(), expected);
   }
}
