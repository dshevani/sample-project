package com.sub.sample.apis.config.spring;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.sub.sample.apis.controller.HealthCheckController;

@Configuration
@ComponentScan({ "com.sub.sample.apis" })
@Import({ EnvironmentConfig.class, JettyConfig.class, HealthCheckConfig.class })
public class ApplicationConfig {

   // Reference:
   // http://docs.spring.io/spring-javaconfig/docs/1.0.0.M4/reference/html/ch04s02.html
   @Inject
   private HealthCheckConfig healthCheckConfig;

   @Inject
   private JettyConfig jettyConfig;

   @Bean
   public HealthCheckController healthCheckController() {

      return new HealthCheckController(healthCheckConfig.masterHealthCheck(), jettyConfig.elbStatisticsCollector());
   }
}
