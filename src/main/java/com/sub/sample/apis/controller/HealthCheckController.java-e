/**
 * 
 */
package com.sub.sample.apis.controller;

import static com.google.common.base.Preconditions.*;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;
import javax.validation.Valid;
import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.health.HealthCheck.Result;
import com.sub.sample.apis.dtos.health.ElbHealthCheckResponse;
import com.sub.sample.apis.dtos.health.HealthCheckResponse;
import com.sub.sample.apis.dtos.health.HealthStatus;
import com.sub.sample.apis.dtos.health.VipConfiguration;
import com.sub.sample.apis.health.MasterHealthCheck;
import com.sub.sample.apis.metrics.ElbStatisticsCollector;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author aniruddha.sharma
 *
 */
@Api(protocols = "http")
@ThreadSafe
@RestController
@RequestMapping("/elb-healthcheck")
@ParametersAreNonnullByDefault
public class HealthCheckController extends BaseController {

   private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckController.class);

   private final MasterHealthCheck masterHealthCheck;
   private final ElbStatisticsCollector elbStatisticsCollector;

   public HealthCheckController(MasterHealthCheck masterHealthCheck, ElbStatisticsCollector elbStatisticsCollector) {

      checkNotNull(masterHealthCheck);
      checkNotNull(elbStatisticsCollector);

      this.masterHealthCheck = masterHealthCheck;
      this.elbStatisticsCollector = elbStatisticsCollector;
   }

   @ApiOperation(nickname = "elb-healthcheck", value = "Performs a shallow health check of the application",
         notes = "This response is used by the ELB for various parts of its operation")
   @ApiResponses({
         @ApiResponse(code = HttpStatus.OK_200, message = "The application is healthy and all health checks are good",
               response = ElbHealthCheckResponse.class),
         @ApiResponse(code = HttpStatus.INTERNAL_SERVER_ERROR_500,
               message = "The application is unhealthy or has to be taken out of rotation from the VIP",
               response = ElbHealthCheckResponse.class) })
   @Timed
   @ExceptionMetered
   @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
   public ResponseEntity<ElbHealthCheckResponse> getElbHealthCheckResults() {

      ElbHealthCheckResponse response = new ElbHealthCheckResponse();
      response.setCapacity(elbStatisticsCollector.getCapacity());
      response.setRequests(elbStatisticsCollector.getTotalRequestsSinceUptime());
      response.setUptime(elbStatisticsCollector.getUptimeInSeconds());

      return healthBasedResponse(response, isHealthy(masterHealthCheck.doShallowCheckWithVipStatus()));
   }

   @ApiOperation(nickname = "elb-healthcheck/shallow", value = "Performs a shallow health check of the application.",
         notes = "Returns whether the service is healthy or not"
               + "This returns the results of the last run by the periodic health checker"
               + "Will not return 200 if the service is supposed to be OUT_OF_ROTATION")
   @ApiResponses({
         @ApiResponse(code = HttpStatus.OK_200, message = "The application is healthy and all health checks are good",
               response = HealthCheckResponse.class),
         @ApiResponse(code = HttpStatus.INTERNAL_SERVER_ERROR_500,
               message = "The application is unhealthy or has to be taken out of rotation from the VIP",
               response = HealthCheckResponse.class) })
   @Timed
   @ExceptionMetered
   @RequestMapping(method = RequestMethod.GET, path = "/shallow", produces = MediaType.APPLICATION_JSON)
   public ResponseEntity<HealthCheckResponse> getShallowHealthCheckResults() {

      return toHttpResponse(masterHealthCheck.doShallowCheckWithVipStatus());
   }

   @ApiOperation(nickname = "elb-healthcheck", value = "Change the status of the host with respect to the VIP",
         notes = "This can be used to bring the service back in rotation or take it out of rotation")
   @ApiResponses({ @ApiResponse(code = HttpStatus.OK_200, message = "The service's VIP status has been updated"),
         @ApiResponse(code = HttpStatus.BAD_REQUEST_400, message = "Unrecognized VipStatus value"),
         @ApiResponse(code = HttpStatus.INTERNAL_SERVER_ERROR_500, message = "Internal server error") })
   @Timed
   @ExceptionMetered
   @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON,
         produces = MediaType.APPLICATION_JSON)
   public ResponseEntity<String> changeVipStatus(@RequestBody @Valid VipConfiguration vipConfiguration) {

      LOGGER.info("Setting vip status to: {}", vipConfiguration);
      masterHealthCheck.setVipStatus(vipConfiguration.getVipStatus());
      return ok(null);
   }

   @ApiOperation(nickname = "elb-healthcheck/deep",
         value = "Performs a deep health check of the application. The VIP status check is not done",
         notes = "This performs all the health checks as part of this API operation, except the VIP status check"
               + "It returns 200 if all the checks succeed. Otherwise, it returns 500")
   @ApiResponses({
         @ApiResponse(code = HttpStatus.OK_200, message = "The application is healthy and all health checks are good",
               response = HealthCheckResponse.class),
         @ApiResponse(code = HttpStatus.INTERNAL_SERVER_ERROR_500, message = "The application is unhealthy",
               response = HealthCheckResponse.class) })
   @Timed
   @ExceptionMetered
   @RequestMapping(method = RequestMethod.GET, path = "/deep", produces = MediaType.APPLICATION_JSON)
   public ResponseEntity<HealthCheckResponse> getDeepHealthCheckResults() {

      return toHttpResponse(masterHealthCheck.doDeepCheck());
   }

   private static boolean isHealthy(Map<String, Result> healthCheckResults) {

      return healthCheckResults.values().stream().allMatch(r -> r.isHealthy());
   }

   private static ResponseEntity<HealthCheckResponse> toHttpResponse(Map<String, Result> healthCheckResults) {

      Map<String, HealthStatus> componentStatuses = healthCheckResults.entrySet().stream()
            .collect(Collectors.toMap(r -> r.getKey(), r -> HealthStatus.fromCheck(r.getValue().isHealthy())));
      boolean isSystemHealthy = isHealthy(healthCheckResults);

      HealthCheckResponse response = new HealthCheckResponse();
      response.setComponentStatuses(componentStatuses);
      response.setServiceStatus(HealthStatus.fromCheck(isSystemHealthy));
      return healthBasedResponse(response, isSystemHealthy);
   }

   private static <T> ResponseEntity<T> healthBasedResponse(T response, boolean isHealthy) {

      org.springframework.http.HttpStatus responseCode = org.springframework.http.HttpStatus.OK;
      if (!isHealthy) {

         responseCode = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
         LOGGER.warn("Health check failed: {}", response);
      }

      return new ResponseEntity<>(response, responseCode);
   }
}
