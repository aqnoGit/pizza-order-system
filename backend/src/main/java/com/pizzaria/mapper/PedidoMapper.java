package com.pizzaria.mapper;

import com.pizzaria.dto.response.ItemPedidoResponse;
import com.pizzaria.dto.response.PedidoResponse;
import com.pizzaria.entity.ItemPedido;
import com.pizzaria.entity.Pedido;
import org.springframework.stereotype.Component;

@Component
public class PedidoMapper {

    public PedidoResponse paraResponse(Pedido pedido) {
        return new PedidoResponse(pedido.getId(), pedido.getComandaId(), pedido.getOrigem(), pedido.getStatus(),
                pedido.getRegistradoEm(), pedido.getValorTotal(),
                pedido.getItens().stream().map(this::paraItemResponse).toList());
    }

    private ItemPedidoResponse paraItemResponse(ItemPedido item) {
        return new ItemPedidoResponse(item.getProdutoId(), item.getQuantidade(), item.getPrecoUnitario(),
                item.subtotal());
    }
}
