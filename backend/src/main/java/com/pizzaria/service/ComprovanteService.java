package com.pizzaria.service;

import com.pizzaria.config.PizzariaProperties;
import com.pizzaria.dto.response.ComprovanteResponse;
import com.pizzaria.entity.Comanda;
import com.pizzaria.entity.ItemPedido;
import com.pizzaria.entity.Mesa;
import com.pizzaria.entity.Produto;
import com.pizzaria.exception.RecursoNaoEncontradoException;
import com.pizzaria.mapper.ComprovanteMapper;
import com.pizzaria.repository.ComandaRepository;
import com.pizzaria.repository.MesaRepository;
import com.pizzaria.repository.PedidoRepository;
import com.pizzaria.repository.ProdutoRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComprovanteService {

    private final ComandaRepository comandaRepository;
    private final MesaRepository mesaRepository;
    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ComprovanteMapper comprovanteMapper;
    private final PizzariaProperties pizzariaProperties;
    private final ComprovanteEscPosFormatter formatadorEscPos;
    private final ImpressoraClient impressoraClient;
    private final Clock relogio;

    @Transactional(readOnly = true)
    public ComprovanteResponse gerar(UUID comandaId) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(ComandaService.MENSAGEM_COMANDA_NAO_ENCONTRADA));
        Mesa mesa = mesaRepository.findById(comanda.getMesaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(ComandaService.MENSAGEM_MESA_NAO_ENCONTRADA));
        List<ItemPedido> itens = pedidoRepository.findByComandaId(comandaId).stream()
                .flatMap(pedido -> pedido.getItens().stream()).toList();
        return comprovanteMapper.paraResponse(pizzariaProperties.nome(), mesa.getNumero(), itens,
                nomesDosProdutos(itens), LocalDateTime.now(relogio));
    }

    public void imprimir(UUID comandaId) {
        impressoraClient.enviar(formatadorEscPos.formatar(gerar(comandaId)));
    }

    private Map<UUID, String> nomesDosProdutos(List<ItemPedido> itens) {
        return produtoRepository.findAllById(itens.stream().map(ItemPedido::getProdutoId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Produto::getId, Produto::getNome, (primeiro, segundo) -> primeiro));
    }
}
