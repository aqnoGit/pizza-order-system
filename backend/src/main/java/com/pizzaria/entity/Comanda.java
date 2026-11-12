package com.pizzaria.entity;

import com.pizzaria.exception.ComandaFechadaException;
import com.pizzaria.exception.ObservacaoInvalidaException;
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

    public static final int TAMANHO_MAXIMO_OBSERVACAO = 500;

    public static final String MENSAGEM_OBSERVACAO_MUITO_LONGA = "Observacao deve ter no maximo 500 caracteres";

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

    @Column(length = TAMANHO_MAXIMO_OBSERVACAO)
    private String observacao;

    public Comanda(UUID id, UUID mesaId, UUID garcomId, StatusComanda status, LocalDateTime abertaEm,
            LocalDateTime fechadaEm, BigDecimal valorTotal, String observacao) {
        this.id = id;
        this.mesaId = mesaId;
        this.garcomId = garcomId;
        this.status = status;
        this.abertaEm = abertaEm;
        this.fechadaEm = fechadaEm;
        this.valorTotal = valorTotal;
        this.observacao = observacao;
    }

    public static Comanda abrir(UUID mesaId, UUID garcomId) {
        return new Comanda(UUID.randomUUID(), mesaId, garcomId, StatusComanda.ABERTA, LocalDateTime.now(), null,
                BigDecimal.ZERO, null);
    }

    public boolean estaFechada() {
        return status == StatusComanda.FECHADA;
    }

    public void exigirAberta() {
        if (estaFechada()) {
            throw new ComandaFechadaException(MENSAGEM_COMANDA_FECHADA);
        }
    }

    public void somarAoTotal(BigDecimal valor) {
        this.valorTotal = this.valorTotal.add(valor);
    }

    public void fechar(BigDecimal valorTotal, String observacao) {
        if (observacao != null && observacao.length() > TAMANHO_MAXIMO_OBSERVACAO) {
            throw new ObservacaoInvalidaException(MENSAGEM_OBSERVACAO_MUITO_LONGA);
        }
        this.observacao = observacao;
        this.valorTotal = valorTotal;
        this.status = StatusComanda.FECHADA;
        this.fechadaEm = LocalDateTime.now();
    }
}
