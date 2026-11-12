package com.pizzaria.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pizzaria.exception.ConflitoException;
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
    void valorTotalSomaPrecoVezesQuantidadeDeItensDiferentes() {
        Produto pizza = new Produto(UUID.randomUUID(), "Calabresa", null, new BigDecimal("39.90"), "PIZZA", true);
        Produto bebida = new Produto(UUID.randomUUID(), "Guarana", null, new BigDecimal("6.50"), "BEBIDA", true);
        Produto sobremesa = new Produto(UUID.randomUUID(), "Pudim", null, new BigDecimal("12.25"), "SOBREMESA", true);

        Pedido pedido = Pedido.presencial(UUID.randomUUID(),
                List.of(ItemPedido.de(pizza, 2), ItemPedido.de(bebida, 3), ItemPedido.de(sobremesa, 4)));

        assertThat(pedido.getValorTotal()).isEqualByComparingTo("148.30");
    }

    @Test
    void valorTotalDeItemUnicoEhPrecoVezesQuantidade() {
        Produto pizza = new Produto(UUID.randomUUID(), "Calabresa", null, new BigDecimal("40.00"), "PIZZA", true);

        Pedido pedido = Pedido.presencial(UUID.randomUUID(), List.of(ItemPedido.de(pizza, 3)));

        assertThat(pedido.getValorTotal()).isEqualByComparingTo("120.00");
    }

    @Test
    void itemCongelaOPrecoDoProdutoNoMomentoDaCriacao() {
        Produto produto = new Produto(UUID.randomUUID(), "Calabresa", null, new BigDecimal("40.00"), "PIZZA", true);

        ItemPedido item = ItemPedido.de(produto, 3);

        assertThat(item.getPrecoUnitario()).isEqualByComparingTo("40.00");
        assertThat(item.subtotal()).isEqualByComparingTo("120.00");
    }

    @Test
    void avancaPelaSequenciaCompletaDeStatus() {
        Pedido pedido = pedidoRegistrado();

        pedido.avancarPara(StatusPedido.EM_PREPARO);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.EM_PREPARO);
        pedido.avancarPara(StatusPedido.PRONTO);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PRONTO);
        pedido.avancarPara(StatusPedido.ENTREGUE);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENTREGUE);
    }

    @Test
    void sequenciaDeStatusEhFixa() {
        assertThat(StatusPedido.values()).containsExactly(StatusPedido.REGISTRADO, StatusPedido.EM_PREPARO,
                StatusPedido.PRONTO, StatusPedido.ENTREGUE);
    }

    @Test
    void naoPulaEtapas() {
        Pedido pedido = pedidoRegistrado();

        assertThatThrownBy(() -> pedido.avancarPara(StatusPedido.PRONTO)).isInstanceOf(ConflitoException.class);
        assertThatThrownBy(() -> pedido.avancarPara(StatusPedido.ENTREGUE)).isInstanceOf(ConflitoException.class);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.REGISTRADO);
    }

    @Test
    void naoVolta() {
        Pedido pedido = pedidoRegistrado();
        pedido.avancarPara(StatusPedido.EM_PREPARO);

        assertThatThrownBy(() -> pedido.avancarPara(StatusPedido.REGISTRADO)).isInstanceOf(ConflitoException.class);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.EM_PREPARO);
    }

    @Test
    void naoRepeteOStatusAtual() {
        Pedido pedido = pedidoRegistrado();

        assertThatThrownBy(() -> pedido.avancarPara(StatusPedido.REGISTRADO)).isInstanceOf(ConflitoException.class);
    }

    @Test
    void pedidoEntregueNaoMudaMais() {
        Pedido pedido = pedidoRegistrado();
        pedido.avancarPara(StatusPedido.EM_PREPARO);
        pedido.avancarPara(StatusPedido.PRONTO);
        pedido.avancarPara(StatusPedido.ENTREGUE);

        for (StatusPedido destino : StatusPedido.values()) {
            assertThatThrownBy(() -> pedido.avancarPara(destino)).isInstanceOf(ConflitoException.class);
        }
    }

    private Pedido pedidoRegistrado() {
        Produto pizza = new Produto(UUID.randomUUID(), "Calabresa", null, new BigDecimal("40.00"), "PIZZA", true);
        return Pedido.presencial(UUID.randomUUID(), List.of(ItemPedido.de(pizza, 1)));
    }
}
