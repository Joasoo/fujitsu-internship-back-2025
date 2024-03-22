package com.fujitsu.trialtask.deliveryfee.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "code_item")
public class CodeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code")
    String code;

    @Column(name = "code_class")
    String codeClass;
}
