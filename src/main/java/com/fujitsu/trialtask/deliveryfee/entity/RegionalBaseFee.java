package com.fujitsu.trialtask.deliveryfee.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "regional_base_fee")
public class RegionalBaseFee {
    @Id
    @Column(name = "city_id")
    private Long cityId;

    @Column(name = "car")
    private BigDecimal car;

    @Column(name = "bike")
    private BigDecimal bike;

    @Column(name = "scooter")
    private BigDecimal scooter;
}
