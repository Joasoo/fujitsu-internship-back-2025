package com.fujitsu.trialtask.deliveryfee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "work_prohibition")
public class WorkProhibition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @JoinColumn(name = "vehicle_id")
    @ManyToOne
    private Vehicle vehicle;

    @NotNull
    @JoinColumn(name = "code_item")
    @ManyToOne
    private CodeItem codeItem;
}
