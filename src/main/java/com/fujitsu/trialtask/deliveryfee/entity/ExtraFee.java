package com.fujitsu.trialtask.deliveryfee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "extra_fee")
public class ExtraFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @JoinColumn(name = "vehicle_id")
    @ManyToOne
    private Vehicle vehicle;

    @NotNull
    @JoinColumn(name = "code")
    @ManyToOne
    private ExtraFeeCode code;

    @NotNull
    @Column(name = "fee_amount")
    private BigDecimal feeAmount;

}
