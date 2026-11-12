package com.pizzaria.mapper;

import com.pizzaria.dto.response.ItemFilaResponse;
import com.pizzaria.dto.response.PedidoFilaResponse;
import com.pizzaria.entity.ItemPedido;
import com.pizzaria.entity.Pedido;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PedidoFilaMapper {

    public PedidoFilaResponse paraResponse(Pedido pedido, Integer numeroMesa, Map<UUID, String> nomesDosProdutos) {
        return new PedidoFilaResponse(pedido.getId(), pedido.getOrigem(), numeroMesa, pedido.getRegistradoEm(),
                pedido.getItens().stream().map(item -> paraItemResponse(item, nomesDosProdutos)).toList());
    }

    private ItemFilaResponse paraItemResponse(ItemPedido item, Map<UUID, String> nomesDosProdutos) {
        return new ItemFilaResponse(item.getProdutoId(), nomesDosProdutos.get(item.getProdutoId()),
                item.getQuantidade(), item.getObservacao());
    }
}
