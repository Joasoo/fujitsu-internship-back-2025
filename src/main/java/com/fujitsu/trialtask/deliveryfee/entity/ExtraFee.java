package com.fujitsu.trialtask.deliveryfee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "extra_fee")
public class ExtraFee {
    // TODO: Composite PK (vehicleId && code)
    @Column(name = "vehicle_id")
    Long vehicleId;

    @Column(name = "code")
    String code;

    @Column(name = "amount")
    BigDecimal amount;
}
