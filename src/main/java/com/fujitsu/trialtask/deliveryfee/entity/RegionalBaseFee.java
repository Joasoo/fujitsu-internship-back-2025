package com.fujitsu.trialtask.deliveryfee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "regional_base_fee")
public class RegionalBaseFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @JoinColumn(name = "city_id")
    @ManyToOne
    private City city;

    @NotNull
    @JoinColumn(name = "vehicle_id")
    @ManyToOne
    private Vehicle vehicle;

    @NotNull
    @Column(name = "fee_amount")
    private BigDecimal feeAmount;
}
