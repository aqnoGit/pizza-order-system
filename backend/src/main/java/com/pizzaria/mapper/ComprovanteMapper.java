package com.pizzaria.mapper;

import com.pizzaria.dto.response.ComprovanteResponse;
import com.pizzaria.dto.response.ItemComprovanteResponse;
import com.pizzaria.entity.ItemPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ComprovanteMapper {

    public ComprovanteResponse paraResponse(String nomePizzaria, Integer numeroMesa, List<ItemPedido> itens,
            Map<UUID, String> nomesDosProdutos, LocalDateTime emitidoEm) {
        List<ItemComprovanteResponse> itensDoComprovante = itens.stream()
                .map(item -> paraItem(item, nomesDosProdutos)).toList();
        BigDecimal valorTotal = itensDoComprovante.stream().map(ItemComprovanteResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ComprovanteResponse(nomePizzaria, numeroMesa, itensDoComprovante, valorTotal, emitidoEm);
    }

    private ItemComprovanteResponse paraItem(ItemPedido item, Map<UUID, String> nomesDosProdutos) {
        return new ItemComprovanteResponse(nomesDosProdutos.get(item.getProdutoId()), item.getQuantidade(),
                item.getPrecoUnitario(), item.subtotal());
    }
}
