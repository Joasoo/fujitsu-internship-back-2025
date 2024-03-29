package com.fujitsu.trialtask.deliveryfee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "code_item")
public class CodeItem {
    @Id
    @Column(name = "code")
    String code;

    @Column(name = "code_class")
    String codeClass;
}
