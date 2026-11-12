package com.pizzaria.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.pizzaria.dto.response.PedidoFilaResponse;
import com.pizzaria.entity.ItemPedido;
import com.pizzaria.entity.OrigemPedido;
import com.pizzaria.entity.Pedido;
import com.pizzaria.entity.Produto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PedidoFilaMapperTest {

    private final PedidoFilaMapper mapper = new PedidoFilaMapper();
    private final Produto produto = new Produto(UUID.randomUUID(), "Calabresa", null, new BigDecimal("42.50"),
            "PIZZA", true);

    @Test
    void mapeiaOrigemMesaEItensComNomeDoProduto() {
        Pedido pedido = Pedido.presencial(UUID.randomUUID(), List.of(ItemPedido.de(produto, 2, "sem cebola")));

        PedidoFilaResponse resposta = mapper.paraResponse(pedido, 7, Map.of(produto.getId(), "Calabresa"));

        assertThat(resposta.id()).isEqualTo(pedido.getId());
        assertThat(resposta.origem()).isEqualTo(OrigemPedido.PRESENCIAL);
        assertThat(resposta.numeroMesa()).isEqualTo(7);
        assertThat(resposta.registradoEm()).isEqualTo(pedido.getRegistradoEm());
        assertThat(resposta.itens()).singleElement().satisfies(item -> {
            assertThat(item.produtoId()).isEqualTo(produto.getId());
            assertThat(item.nomeProduto()).isEqualTo("Calabresa");
            assertThat(item.quantidade()).isEqualTo(2);
            assertThat(item.observacao()).isEqualTo("sem cebola");
        });
    }

    @Test
    void aceitaNumeroDeMesaNulo() {
        Pedido pedido = Pedido.presencial(UUID.randomUUID(), List.of(ItemPedido.de(produto, 1)));

        PedidoFilaResponse resposta = mapper.paraResponse(pedido, null, Map.of(produto.getId(), "Calabresa"));

        assertThat(resposta.numeroMesa()).isNull();
    }
}
