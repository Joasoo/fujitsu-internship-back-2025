package com.fujitsu.trialtask.deliveryfee.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DeliveryFeeDto {
    private Long cityId;
    private Long vehicleId;
    private BigDecimal baseFee;
    private BigDecimal extraFee;
    private BigDecimal totalFee;
}
