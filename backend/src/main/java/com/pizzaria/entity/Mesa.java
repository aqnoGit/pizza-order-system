package com.pizzaria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mesas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mesa {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Integer numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMesa status;

    public Mesa(UUID id, Integer numero, StatusMesa status) {
        this.id = id;
        this.numero = numero;
        this.status = status;
    }
}
