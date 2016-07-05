package com.ekart.sample.apis.dtos.health;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author vijay.daniel
 *
 */
@ApiModel(value = "vip_configuration", description = "VIP configuration for this host")
@ParametersAreNonnullByDefault
public class VipConfiguration {

   @ApiModelProperty(name = "vip_status", value = "The status of this service with respect to the VIP")
   @JsonProperty(value = "vip_status", required = true)
   @NotNull
   private VipStatus vipStatus;

   public VipConfiguration() {

      // For Json deserialization
   }

   public VipConfiguration(VipStatus vipStatus) {

      this.vipStatus = vipStatus;
   }

   public VipStatus getVipStatus() {

      return vipStatus;
   }

   public void setVipStatus(VipStatus vipStatus) {

      this.vipStatus = vipStatus;
   }

   @Override
   public String toString() {

      return "VipConfiguration [vipStatus=" + vipStatus + "]";
   }

}
