package com.ekart.sample.apis.health;

import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;

import com.codahale.metrics.health.HealthCheck.Result;
import com.ekart.sample.apis.dtos.health.VipStatus;
import com.google.common.collect.ImmutableMap;

/**
 * @author vijay.daniel
 *
 */
@ThreadSafe
@ParametersAreNonnullByDefault
public class MasterHealthCheck {

   private final VipStatusCheck vipStatusCheck;
   private final PeriodicHealthChecker periodicHealthChecker;

   public MasterHealthCheck(VipStatusCheck vipStatusCheck, PeriodicHealthChecker periodicHealthChecker) {

      this.vipStatusCheck = vipStatusCheck;
      this.periodicHealthChecker = periodicHealthChecker;
   }

   public void setVipStatus(VipStatus status) {

      vipStatusCheck.setVipStatus(status);
   }

   public Map<String, Result> doShallowCheckWithVipStatus() {

      return ImmutableMap.<String, Result>builder().putAll(periodicHealthChecker.getLastRunResults())
            .put("vip_status", vipStatusCheck.execute()).build();
   }

   public Map<String, Result> doDeepCheck() {

      return periodicHealthChecker.runHealthChecks();
   }

   public Map<String, Result> doDeepCheckAndUpdateCache() {

      return periodicHealthChecker.runHealthChecksAndUpdateCache();
   }
}
