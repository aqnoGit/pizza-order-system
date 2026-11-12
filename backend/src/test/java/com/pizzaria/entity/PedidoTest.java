package com.pizzaria.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PedidoTest {

    @Test
    void pedidoPresencialNasceRegistradoComValorTotalDosItens() {
        UUID comandaId = UUID.randomUUID();
        Produto pizza = new Produto(UUID.randomUUID(), "Calabresa", null, new BigDecimal("40.00"), "PIZZA", true);
        Produto bebida = new Produto(UUID.randomUUID(), "Guarana", null, new BigDecimal("6.50"), "BEBIDA", true);

        Pedido pedido = Pedido.presencial(comandaId, List.of(ItemPedido.de(pizza, 1), ItemPedido.de(bebida, 2)));

        assertThat(pedido.getOrigem()).isEqualTo(OrigemPedido.PRESENCIAL);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.REGISTRADO);
        assertThat(pedido.getComandaId()).isEqualTo(comandaId);
        assertThat(pedido.getValorTotal()).isEqualByComparingTo("53.00");
        assertThat(pedido.getItens()).hasSize(2);
    }

    @Test
    void itemCongelaOPrecoDoProdutoNoMomentoDaCriacao() {
        Produto produto = new Produto(UUID.randomUUID(), "Calabresa", null, new BigDecimal("40.00"), "PIZZA", true);

        ItemPedido item = ItemPedido.de(produto, 3);

        assertThat(item.getPrecoUnitario()).isEqualByComparingTo("40.00");
        assertThat(item.subtotal()).isEqualByComparingTo("120.00");
    }
}
