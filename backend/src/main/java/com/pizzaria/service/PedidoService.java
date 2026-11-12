package com.pizzaria.service;

import com.pizzaria.dto.request.CriarPedidoRequest;
import com.pizzaria.dto.request.ItemPedidoRequest;
import com.pizzaria.dto.response.PedidoProntoResponse;
import com.pizzaria.dto.response.PedidoResponse;
import com.pizzaria.entity.Comanda;
import com.pizzaria.entity.ItemPedido;
import com.pizzaria.entity.Pedido;
import com.pizzaria.entity.Produto;
import com.pizzaria.entity.StatusPedido;
import com.pizzaria.exception.ProdutoIndisponivelException;
import com.pizzaria.exception.RecursoNaoEncontradoException;
import com.pizzaria.mapper.PedidoMapper;
import com.pizzaria.repository.ComandaRepository;
import com.pizzaria.repository.MesaRepository;
import com.pizzaria.repository.PedidoRepository;
import com.pizzaria.repository.ProdutoRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PedidoService {

    public static final String MENSAGEM_PRODUTO_NAO_ENCONTRADO = "Produto nao encontrado";

    public static final String MENSAGEM_PEDIDO_NAO_ENCONTRADO = "Pedido nao encontrado";

    public static final String MENSAGEM_PRODUTO_INDISPONIVEL = "Produto indisponivel";

    public static final String TOPICO_FILA_PEDIDOS = "/topic/pedidos/fila";

    public static final String TOPICO_GARCOM_PEDIDOS_PRONTOS = "/topic/garcom/pedidos-prontos";

    private final ComandaRepository comandaRepository;
    private final MesaRepository mesaRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;
    private final SimpMessagingTemplate mensageria;

    @Transactional
    public PedidoResponse criarPresencial(UUID comandaId, CriarPedidoRequest requisicao) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(ComandaService.MENSAGEM_COMANDA_NAO_ENCONTRADA));
        comanda.exigirAberta();
        List<ItemPedido> itens = requisicao.itens().stream().map(this::montarItem).toList();
        Pedido pedido = pedidoRepository.save(Pedido.presencial(comandaId, itens));
        comanda.somarAoTotal(pedido.getValorTotal());
        PedidoResponse resposta = pedidoMapper.paraResponse(pedido);
        mensageria.convertAndSend(TOPICO_FILA_PEDIDOS, resposta);
        return resposta;
    }

    @Transactional
    public PedidoResponse atualizarStatus(UUID pedidoId, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MENSAGEM_PEDIDO_NAO_ENCONTRADO));
        pedido.avancarPara(novoStatus);
        if (novoStatus == StatusPedido.PRONTO) {
            notificarGarcomPedidoPronto(pedido);
        }
        return pedidoMapper.paraResponse(pedido);
    }

    private void notificarGarcomPedidoPronto(Pedido pedido) {
        if (pedido.getComandaId() == null) {
            return;
        }
        comandaRepository.findById(pedido.getComandaId())
                .flatMap(comanda -> mesaRepository.findById(comanda.getMesaId()))
                .ifPresent(mesa -> mensageria.convertAndSend(TOPICO_GARCOM_PEDIDOS_PRONTOS,
                        new PedidoProntoResponse(pedido.getId(), mesa.getNumero())));
    }

    private ItemPedido montarItem(ItemPedidoRequest item) {
        Produto produto = produtoRepository.findById(item.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(MENSAGEM_PRODUTO_NAO_ENCONTRADO));
        if (!produto.isDisponivel()) {
            throw new ProdutoIndisponivelException(MENSAGEM_PRODUTO_INDISPONIVEL);
        }
        return ItemPedido.de(produto, item.quantidade(), item.observacao());
    }
}
