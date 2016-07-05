/**
 * 
 */
package com.ekart.sample.apis.config.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.ekart.sample.apis.health.MasterHealthCheck;
import com.ekart.sample.apis.health.PeriodicHealthChecker;
import com.ekart.sample.apis.health.VipStatusCheck;

/**
 * @author vijay.daniel
 *
 */
@Configuration
public class HealthCheckConfig {

   @Bean
   public PeriodicHealthChecker periodicHealthChecker() {

      return new PeriodicHealthChecker(healthCheckRegistry());
   }

   @Bean
   public HealthCheckRegistry healthCheckRegistry() {

      HealthCheckRegistry registry = new HealthCheckRegistry();
      registry.register("threadDeadlockDetector", new ThreadDeadlockHealthCheck());
      return registry;
   }

   @Bean
   public VipStatusCheck vipStatusCheck() {

      return new VipStatusCheck();
   }

   @Bean(name = "masterHealthCheck")
   public MasterHealthCheck masterHealthCheck() {

      return new MasterHealthCheck(vipStatusCheck(), periodicHealthChecker());
   }
}
