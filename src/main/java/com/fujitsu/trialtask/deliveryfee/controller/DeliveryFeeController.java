package com.fujitsu.trialtask.deliveryfee.controller;


import com.fujitsu.trialtask.deliveryfee.dto.DeliveryFeeDto;
import com.fujitsu.trialtask.deliveryfee.service.DeliveryFeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/delivery/fee")
@RequiredArgsConstructor
public class DeliveryFeeController {
    private final DeliveryFeeService deliveryService;

    @GetMapping("/city/{cityId}/vehicle/{vehicleId}")
    public DeliveryFeeDto getDeliveryFee(@PathVariable("cityId") Long cityId, @PathVariable("vehicleId") Long vehicleId) {
        return deliveryService.getDeliveryFee(cityId, vehicleId);
    }
}
