package com.ekart.sample.apis.dtos.health;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author vijay.daniel
 *
 */
@ApiModel(value = "response", description = "The result of a health check operation")
public class HealthCheckResponse {

   @ApiModelProperty(name = "service_status", value = "Overall health of the service")
   @JsonProperty(value = "service_status")
   private HealthStatus serviceStatus;

   @ApiModelProperty(name = "component_statuses", value = "Component-level breakdown of health")
   @JsonProperty(value = "component_statuses")
   private Map<String, HealthStatus> componentStatuses;

   public HealthCheckResponse() {

      // For Json deserialization
   }

   public HealthCheckResponse(HealthStatus serviceStatus, Map<String, HealthStatus> componentStatuses) {

      this.serviceStatus = serviceStatus;
      this.componentStatuses = componentStatuses;
   }

   public HealthStatus getServiceStatus() {

      return serviceStatus;
   }

   public void setServiceStatus(HealthStatus serviceStatus) {

      this.serviceStatus = serviceStatus;
   }

   public Map<String, HealthStatus> getComponentStatuses() {

      return componentStatuses;
   }

   public void setComponentStatuses(Map<String, HealthStatus> componentStatuses) {

      this.componentStatuses = componentStatuses;
   }

   @Override
   public String toString() {

      return "HealthCheckResponse [serviceStatus=" + serviceStatus + ", componentStatuses=" + componentStatuses + "]";
   }
}
