package com.sub.sample.apis.health;

import org.junit.Test;

import com.codahale.metrics.health.HealthCheck.Result;
import com.sub.sample.apis.dtos.health.VipStatus;
import com.sub.sample.apis.health.VipStatusCheck;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author vijay.daniel
 *
 */
public class VipStatusCheckTest {

   private final VipStatusCheck check = new VipStatusCheck();

   @Test(expected = NullPointerException.class)
   public void shouldThrowANullPointerExceptionWhenStatusIsNull() {

      check.setVipStatus(null);
   }

   @Test
   public void shouldReturnHealthyWhenVipIsInRotation() {

      check.setVipStatus(VipStatus.IN_ROTATION);
      Result response = check.execute();
      assertThat(response, is(Result.healthy()));
   }

   @Test
   public void shouldReturnUnhealthyWhenVipIsOutOfRotation() {

      check.setVipStatus(VipStatus.OUT_OF_ROTATION);
      Result response = check.execute();
      assertThat(response, is(Result.unhealthy("Service is not supposed to be behind the VIP")));
   }
}
