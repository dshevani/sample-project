package com.sub.sample.apis.jetty;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;

import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck.Result;
import com.sub.sample.apis.dtos.health.VipStatus;
import com.sub.sample.apis.health.MasterHealthCheck;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Since we're letting supervisord monitor our application process, when an
 * application process is started again by supervisord because it died, there is
 * no deployment script to put it back in rotation. So, we'll let our
 * applications adhere to a model where as soon as the application is good to go
 * based on the components registered here, we put it back in rotation
 * 
 * This sets the vip_status to IN_ROTATION after all the registered LifeCycle
 * components have started and a deep health check succeeds.
 * 
 * When Jetty comes up, even if the VIP makes a elb-healthcheck call, the
 * VipStatus would be OOR. So, even though the deep health check may not have
 * run updating the shallow check cache, it's ok.
 * 
 * WARNING: This doesn't work when you have Jetty listening on multiple ports.
 * Ex: 8080 for application and 8081 for management. This works only for the
 * application port listener
 * 
 * @author vijay.daniel
 *
 */
@ThreadSafe
@ParametersAreNonnullByDefault
public class AggregatedLifeCycleListener implements LifeCycle.Listener {

   private static final Logger LOGGER = LoggerFactory.getLogger(AggregatedLifeCycleListener.class);

   private final MasterHealthCheck masterHealthCheck;
   private final Object lifeCycleCheckLock;

   /*
    * Tracks whether a component has started or not. true -> started, false ->
    * not yet started
    */
   private final ConcurrentMap<LifeCycle, Boolean> componentsStatus;

   public AggregatedLifeCycleListener(MasterHealthCheck masterHealthCheck) {

      this.masterHealthCheck = masterHealthCheck;
      this.lifeCycleCheckLock = new Object();
      this.componentsStatus = Maps.newConcurrentMap();
   }

   public void register(LifeCycle component) {

      LOGGER.info("Registering component with listener: {}", component);

      synchronized (lifeCycleCheckLock) {

         componentsStatus.put(component, false);
         component.addLifeCycleListener(this);
      }
   }

   public void deregister(LifeCycle component) {

      LOGGER.info("Deregistering component with listener: {}", component);

      synchronized (lifeCycleCheckLock) {

         component.removeLifeCycleListener(this);
         componentsStatus.remove(component);
      }
   }

   public Map<LifeCycle, Boolean> getComponentsStatus() {

      return ImmutableMap.copyOf(componentsStatus);
   }

   @Override
   public void lifeCycleStarting(LifeCycle event) {

      LOGGER.info("Lifecycle starting for component: {}", event);
   }

   @Override
   public void lifeCycleStarted(LifeCycle event) {

      LOGGER.info("Lifecyle started for component: {}", event);

      synchronized (lifeCycleCheckLock) {

         componentsStatus.put(event, true);

         LOGGER.info("Components started status: {}", componentsStatus);

         if (componentsStatus.values().stream().allMatch(v -> v)) {

            LOGGER.info("All components have been started. "
                  + "Performing deep health check and updating shallow check cache");
            Map<String, Result> deepCheckResults = masterHealthCheck.doDeepCheckAndUpdateCache();
            LOGGER.info("Deep health check results: {}", deepCheckResults);

            Map<String, Result> failedChecks = deepCheckResults.entrySet().stream()
                  .filter(e -> !e.getValue().isHealthy()).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

            if (failedChecks.isEmpty()) {

               LOGGER.info("All deep health checks have succeeded. Setting the vip_status to IN_ROTATION");
               masterHealthCheck.setVipStatus(VipStatus.IN_ROTATION);

            } else {

               // NOTE: Not throwing an exception and failing here because
               // keeping the service up might help in debugging
               LOGGER.error("Some deep health checks have failed. Not setting the vip_status: {}", failedChecks);
            }
         }
      }
   }

   @Override
   public void lifeCycleFailure(LifeCycle event, Throwable cause) {

      LOGGER.error("Component lifecycle failed: {}", event, cause);
   }

   @Override
   public void lifeCycleStopping(LifeCycle event) {

      LOGGER.info("Component stopping: {}", event);
   }

   @Override
   public void lifeCycleStopped(LifeCycle event) {

      LOGGER.info("Component stopped: {}", event);
   }
}
