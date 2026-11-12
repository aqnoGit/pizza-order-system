package com.pizzaria.entity;

import com.pizzaria.exception.ComandaFechadaException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comandas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comanda {

    public static final String MENSAGEM_COMANDA_FECHADA = "Comanda fechada nao aceita novos itens";

    @Id
    private UUID id;

    @Column(name = "mesa_id", nullable = false)
    private UUID mesaId;

    @Column(name = "garcom_id", nullable = false)
    private UUID garcomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusComanda status;

    @Column(name = "aberta_em", nullable = false)
    private LocalDateTime abertaEm;

    @Column(name = "fechada_em")
    private LocalDateTime fechadaEm;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    public Comanda(UUID id, UUID mesaId, UUID garcomId, StatusComanda status, LocalDateTime abertaEm,
            LocalDateTime fechadaEm, BigDecimal valorTotal) {
        this.id = id;
        this.mesaId = mesaId;
        this.garcomId = garcomId;
        this.status = status;
        this.abertaEm = abertaEm;
        this.fechadaEm = fechadaEm;
        this.valorTotal = valorTotal;
    }

    public static Comanda abrir(UUID mesaId, UUID garcomId) {
        return new Comanda(UUID.randomUUID(), mesaId, garcomId, StatusComanda.ABERTA, LocalDateTime.now(), null,
                BigDecimal.ZERO);
    }

    public boolean estaFechada() {
        return status == StatusComanda.FECHADA;
    }

    public void exigirAberta() {
        if (estaFechada()) {
            throw new ComandaFechadaException(MENSAGEM_COMANDA_FECHADA);
        }
    }

    public void fechar(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
        this.status = StatusComanda.FECHADA;
        this.fechadaEm = LocalDateTime.now();
    }
}
