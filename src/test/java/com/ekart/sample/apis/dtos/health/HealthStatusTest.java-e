package com.ekart.sample.apis.dtos.health;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import com.ekart.sample.apis.dtos.health.HealthStatus;

/**
 * @author vijay.daniel
 *
 */
public class HealthStatusTest {

   @Test
   public void shouldReturnHealthIfCheckSucceeded() {

      assertThat(HealthStatus.fromCheck(true), is(HealthStatus.HEALTHY));
   }

   @Test
   public void shouldReturnUnhealthyIfCheckFailed() {

      assertThat(HealthStatus.fromCheck(false), is(HealthStatus.UNHEALTHY));
   }
}
