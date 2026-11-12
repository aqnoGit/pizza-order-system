package com.pizzaria.service;

import com.pizzaria.dto.response.PedidoFilaResponse;
import com.pizzaria.entity.Comanda;
import com.pizzaria.entity.ItemPedido;
import com.pizzaria.entity.Mesa;
import com.pizzaria.entity.Pedido;
import com.pizzaria.entity.Produto;
import com.pizzaria.entity.StatusPedido;
import com.pizzaria.mapper.PedidoFilaMapper;
import com.pizzaria.repository.ComandaRepository;
import com.pizzaria.repository.MesaRepository;
import com.pizzaria.repository.PedidoRepository;
import com.pizzaria.repository.ProdutoRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FilaPedidosService {

    private final PedidoRepository pedidoRepository;
    private final ComandaRepository comandaRepository;
    private final MesaRepository mesaRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoFilaMapper pedidoFilaMapper;

    @Transactional(readOnly = true)
    public List<PedidoFilaResponse> listarFila() {
        List<Pedido> pedidos = pedidoRepository.findByStatusOrderByRegistradoEmAsc(StatusPedido.REGISTRADO);
        Map<UUID, Integer> numerosDasMesasPorComanda = numerosDasMesasPorComanda(pedidos);
        Map<UUID, String> nomesDosProdutos = nomesDosProdutos(pedidos);
        return pedidos.stream()
                .map(pedido -> pedidoFilaMapper.paraResponse(pedido,
                        numerosDasMesasPorComanda.get(pedido.getComandaId()), nomesDosProdutos))
                .toList();
    }

    private Map<UUID, Integer> numerosDasMesasPorComanda(List<Pedido> pedidos) {
        Set<UUID> comandaIds = pedidos.stream().map(Pedido::getComandaId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<Comanda> comandas = comandaRepository.findAllById(comandaIds);
        Map<UUID, Integer> numerosPorMesa = mesaRepository
                .findAllById(comandas.stream().map(Comanda::getMesaId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Mesa::getId, Mesa::getNumero));
        return comandas.stream().filter(comanda -> numerosPorMesa.containsKey(comanda.getMesaId()))
                .collect(Collectors.toMap(Comanda::getId, comanda -> numerosPorMesa.get(comanda.getMesaId())));
    }

    private Map<UUID, String> nomesDosProdutos(List<Pedido> pedidos) {
        Set<UUID> produtoIds = pedidos.stream().flatMap(pedido -> pedido.getItens().stream())
                .map(ItemPedido::getProdutoId).collect(Collectors.toSet());
        return produtoRepository.findAllById(produtoIds).stream()
                .collect(Collectors.toMap(Produto::getId, Produto::getNome, (primeiro, segundo) -> primeiro));
    }
}
