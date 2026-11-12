package com.pizzaria.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.pizzaria.dto.response.PedidoResponse;
import com.pizzaria.entity.ItemPedido;
import com.pizzaria.entity.Pedido;
import com.pizzaria.entity.Produto;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PedidoMapperTest {

    private final PedidoMapper mapper = new PedidoMapper();

    @Test
    void mapeiaPedidoEItensParaResponse() {
        UUID comandaId = UUID.randomUUID();
        Produto produto = new Produto(UUID.randomUUID(), "Calabresa", null, new BigDecimal("40.00"), "PIZZA", true);
        Pedido pedido = Pedido.presencial(comandaId, List.of(ItemPedido.de(produto, 2)));

        PedidoResponse resposta = mapper.paraResponse(pedido);

        assertThat(resposta.id()).isEqualTo(pedido.getId());
        assertThat(resposta.comandaId()).isEqualTo(comandaId);
        assertThat(resposta.origem()).isEqualTo(pedido.getOrigem());
        assertThat(resposta.valorTotal()).isEqualByComparingTo("80.00");
        assertThat(resposta.itens()).singleElement().satisfies(item -> {
            assertThat(item.produtoId()).isEqualTo(produto.getId());
            assertThat(item.quantidade()).isEqualTo(2);
            assertThat(item.subtotal()).isEqualByComparingTo("80.00");
        });
    }
}
