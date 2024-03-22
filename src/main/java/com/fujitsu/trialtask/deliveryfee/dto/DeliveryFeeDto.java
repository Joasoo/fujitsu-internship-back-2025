package com.fujitsu.trialtask.deliveryfee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DeliveryFeeDto {
    private Long vehicleId;
    private Long cityId;
    private BigDecimal baseFee;
    private BigDecimal extraFee;
    private BigDecimal totalFee;
}
