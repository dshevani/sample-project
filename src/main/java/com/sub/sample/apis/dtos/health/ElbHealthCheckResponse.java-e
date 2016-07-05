package com.sub.sample.apis.dtos.health;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * NOTE: Using boxed types to avoid 0 default values
 * 
 * @author vijay.daniel
 *
 */
@ApiModel(value = "response", description = "Response tailored for ELBs in the new Chennai DC")
public class ElbHealthCheckResponse {

   @ApiModelProperty(name = "uptime", value = "Time in seconds since the service was started")
   @JsonProperty(value = "uptime")
   private Long uptime;

   @ApiModelProperty(name = "requests", value = "Number of requests served since the service was started")
   @JsonProperty(value = "requests")
   private Long requests;

   @ApiModelProperty(name = "capacity",
         value = "An abstract number that indicates the capacity of the server. "
               + "The range is from 0 to 100. 0 means the server cannot handle any more requests. "
               + "100 means there is no load currently and the server has complete free capacity")
   @JsonProperty(value = "capacity")
   private Long capacity;

   public ElbHealthCheckResponse() {

      // For JSON deserialization
   }

   public ElbHealthCheckResponse(Long uptime, Long requests, Long capacity) {

      this.uptime = uptime;
      this.requests = requests;
      this.capacity = capacity;
   }

   public Long getUptime() {

      return uptime;
   }

   public void setUptime(Long uptime) {

      this.uptime = uptime;
   }

   public Long getRequests() {

      return requests;
   }

   public void setRequests(Long requests) {

      this.requests = requests;
   }

   public Long getCapacity() {

      return capacity;
   }

   public void setCapacity(Long capacity) {

      this.capacity = capacity;
   }

   @Override
   public String toString() {

      return "ElbHealthCheckResponse [uptime=" + uptime + ", requests=" + requests + ", capacity=" + capacity + "]";
   }
}
