package com.ekart.sample.apis.dtos.health;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author vijay.daniel
 *
 */
@ApiModel(value = "vip_status", description = "The status of this service with respect to the fronting VIP")
public enum VipStatus {

                       @ApiModelProperty(value = "The service should be put back in rotation behind the VIP. "
                             + "This means that the shallow health checks will now consider "
                             + "the vip status health check to be successful") IN_ROTATION,

                       @ApiModelProperty(value = "This service should be taken out of rotation from the VIP. "
                             + "Thyis means that the vip status health check will fail "
                             + "in the shallow health check") OUT_OF_ROTATION
}
