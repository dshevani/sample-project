package com.ekart.sample.apis.dtos.health;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author vijay.daniel
 *
 */
@ApiModel(value = "status", description = "The status of the service/health check component")
public enum HealthStatus {

                          @ApiModelProperty(value = "The component is healthy") HEALTHY,

                          @ApiModelProperty(value = "The component is unhealthy") UNHEALTHY;

   public static HealthStatus fromCheck(boolean isHealthy) {

      return isHealthy ? HEALTHY : UNHEALTHY;
   }
}
