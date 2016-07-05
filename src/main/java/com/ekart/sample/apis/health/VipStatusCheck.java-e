package com.ekart.sample.apis.health;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.concurrent.ThreadSafe;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;
import com.ekart.sample.apis.dtos.health.VipStatus;
import com.google.common.base.Preconditions;

/**
 * @author vijay.daniel
 *
 */
@ThreadSafe
public class VipStatusCheck extends HealthCheck {

   private static final Logger LOGGER = LoggerFactory.getLogger(VipStatusCheck.class);

   private final AtomicReference<VipStatus> vipStatus = new AtomicReference<>(VipStatus.OUT_OF_ROTATION);

   public void setVipStatus(@NotNull VipStatus vipStatus) {

      Preconditions.checkNotNull(vipStatus, "vipStatus cannot be null");

      LOGGER.info("Setting vipStatus to: {}", vipStatus);

      this.vipStatus.set(vipStatus);
   }

   @Override
   protected Result check() throws Exception {

      if (VipStatus.OUT_OF_ROTATION.equals(vipStatus.get())) {

         return Result.unhealthy("Service is not supposed to be behind the VIP");
      }

      return Result.healthy();
   }
}
