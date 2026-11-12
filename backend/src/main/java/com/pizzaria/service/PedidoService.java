package com.pizzaria.service;

import com.pizzaria.dto.request.CriarPedidoRequest;
import com.pizzaria.dto.request.ItemPedidoRequest;
import com.pizzaria.dto.response.PedidoResponse;
import com.pizzaria.entity.Comanda;
import com.pizzaria.entity.ItemPedido;
import com.pizzaria.entity.Pedido;
import com.pizzaria.entity.Produto;
import com.pizzaria.exception.ProdutoIndisponivelException;
import com.pizzaria.exception.RecursoNaoEncontradoException;
import com.pizzaria.mapper.PedidoMapper;
import com.pizzaria.repository.ComandaRepository;
import com.pizzaria.repository.PedidoRepository;
import com.pizzaria.repository.ProdutoRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PedidoService {

    public static final String MENSAGEM_PRODUTO_NAO_ENCONTRADO = "Produto nao encontrado";

    public static final String MENSAGEM_PRODUTO_INDISPONIVEL = "Produto indisponivel";

    private final ComandaRepository comandaRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;

    @Transactional
    public PedidoResponse criarPresencial(UUID comandaId, CriarPedidoRequest requisicao) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(ComandaService.MENSAGEM_COMANDA_NAO_ENCONTRADA));
        comanda.exigirAberta();
        List<ItemPedido> itens = requisicao.itens().stream().map(this::montarItem).toList();
        Pedido pedido = pedidoRepository.save(Pedido.presencial(comandaId, itens));
        comanda.somarAoTotal(pedido.getValorTotal());
        return pedidoMapper.paraResponse(pedido);
    }

    private ItemPedido montarItem(ItemPedidoRequest item) {
        Produto produto = produtoRepository.findById(item.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(MENSAGEM_PRODUTO_NAO_ENCONTRADO));
        if (!produto.isDisponivel()) {
            throw new ProdutoIndisponivelException(MENSAGEM_PRODUTO_INDISPONIVEL);
        }
        return ItemPedido.de(produto, item.quantidade());
    }
}
