package com.pizzaria.entity;

import com.pizzaria.exception.ConflitoException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pedidos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pedido {

    public static final String MENSAGEM_TRANSICAO_INVALIDA = "Transicao de status invalida: de ";

    @Id
    private UUID id;

    @Column(name = "comanda_id")
    private UUID comandaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemPedido origem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    @Column(name = "inicio_preparo")
    private LocalDateTime inicioPreparo;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "pedido_id", nullable = false)
    private List<ItemPedido> itens = new ArrayList<>();

    private Pedido(UUID id, UUID comandaId, OrigemPedido origem, List<ItemPedido> itens) {
        this.id = id;
        this.comandaId = comandaId;
        this.origem = origem;
        this.status = StatusPedido.REGISTRADO;
        this.registradoEm = LocalDateTime.now();
        this.itens = new ArrayList<>(itens);
        this.valorTotal = itens.stream().map(ItemPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static Pedido presencial(UUID comandaId, List<ItemPedido> itens) {
        return new Pedido(UUID.randomUUID(), comandaId, OrigemPedido.PRESENCIAL, itens);
    }

    public void avancarPara(StatusPedido novoStatus) {
        if (!status.permiteAvancarPara(novoStatus)) {
            throw new ConflitoException(MENSAGEM_TRANSICAO_INVALIDA + status + " para " + novoStatus);
        }
        this.status = novoStatus;
        if (novoStatus == StatusPedido.EM_PREPARO) {
            this.inicioPreparo = LocalDateTime.now();
        }
    }
}
