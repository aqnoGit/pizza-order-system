package com.pizzaria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pizzaria.dto.request.CriarPedidoRequest;
import com.pizzaria.dto.request.ItemPedidoRequest;
import com.pizzaria.dto.response.PedidoResponse;
import com.pizzaria.entity.Comanda;
import com.pizzaria.entity.ItemPedido;
import com.pizzaria.entity.OrigemPedido;
import com.pizzaria.entity.Pedido;
import com.pizzaria.entity.Produto;
import com.pizzaria.entity.StatusPedido;
import com.pizzaria.exception.ComandaFechadaException;
import com.pizzaria.exception.ConflitoException;
import com.pizzaria.exception.ProdutoIndisponivelException;
import com.pizzaria.exception.RecursoNaoEncontradoException;
import com.pizzaria.mapper.PedidoMapper;
import com.pizzaria.repository.ComandaRepository;
import com.pizzaria.repository.PedidoRepository;
import com.pizzaria.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private ComandaRepository comandaRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private SimpMessagingTemplate mensageria;

    @Spy
    private PedidoMapper pedidoMapper = new PedidoMapper();

    @InjectMocks
    private PedidoService pedidoService;

    private final Comanda comanda = Comanda.abrir(UUID.randomUUID(), UUID.randomUUID());
    private final Produto produto = new Produto(UUID.randomUUID(), "Calabresa", null, new BigDecimal("42.50"),
            "PIZZA", true);

    @Test
    void atualizaStatusDoPedidoParaOProximo() {
        Pedido pedido = Pedido.presencial(comanda.getId(), List.of(ItemPedido.de(produto, 1)));
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));

        PedidoResponse resposta = pedidoService.atualizarStatus(pedido.getId(), StatusPedido.EM_PREPARO);

        assertThat(resposta.status()).isEqualTo(StatusPedido.EM_PREPARO);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.EM_PREPARO);
    }

    @Test
    void rejeitaTransicaoQuePulaEtapaSemAlterarOStatus() {
        Pedido pedido = Pedido.presencial(comanda.getId(), List.of(ItemPedido.de(produto, 1)));
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.atualizarStatus(pedido.getId(), StatusPedido.PRONTO))
                .isInstanceOf(ConflitoException.class);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.REGISTRADO);
    }

    @Test
    void atualizarStatusDePedidoInexistenteLancaNaoEncontrado() {
        UUID pedidoId = UUID.randomUUID();
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.atualizarStatus(pedidoId, StatusPedido.EM_PREPARO))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage(PedidoService.MENSAGEM_PEDIDO_NAO_ENCONTRADO);
    }

    @Test
    void criaPedidoPresencialComPrecoVigenteDoProduto() {
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(chamada -> chamada.getArgument(0));

        PedidoResponse resposta = pedidoService.criarPresencial(comanda.getId(), requisicao(produto.getId(), 2));

        assertThat(resposta.origem()).isEqualTo(OrigemPedido.PRESENCIAL);
        assertThat(resposta.status()).isEqualTo(StatusPedido.REGISTRADO);
        assertThat(resposta.comandaId()).isEqualTo(comanda.getId());
        assertThat(resposta.valorTotal()).isEqualByComparingTo("85.00");
        assertThat(resposta.itens()).singleElement().satisfies(item -> {
            assertThat(item.precoUnitario()).isEqualByComparingTo("42.50");
            assertThat(item.quantidade()).isEqualTo(2);
        });
    }

    @Test
    void publicaPedidoCriadoNaFilaDaCozinha() {
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(chamada -> chamada.getArgument(0));

        PedidoResponse resposta = pedidoService.criarPresencial(comanda.getId(), requisicao(produto.getId(), 1));

        verify(mensageria, times(1)).convertAndSend(PedidoService.TOPICO_FILA_PEDIDOS, resposta);
    }

    @Test
    void somaValorDoPedidoAoTotalDaComanda() {
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(chamada -> chamada.getArgument(0));

        pedidoService.criarPresencial(comanda.getId(), requisicao(produto.getId(), 2));
        pedidoService.criarPresencial(comanda.getId(), requisicao(produto.getId(), 1));

        assertThat(comanda.getValorTotal()).isEqualByComparingTo("127.50");
    }

    @Test
    void naoAlteraTotalDaComandaQuandoProdutoIndisponivel() {
        Produto esgotado = new Produto(UUID.randomUUID(), "Atum", null, BigDecimal.TEN, "PIZZA", false);
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));
        when(produtoRepository.findById(esgotado.getId())).thenReturn(Optional.of(esgotado));

        assertThatThrownBy(() -> pedidoService.criarPresencial(comanda.getId(), requisicao(esgotado.getId(), 1)))
                .isInstanceOf(ProdutoIndisponivelException.class);

        assertThat(comanda.getValorTotal()).isEqualByComparingTo("0");
    }

    @Test
    void comandaInexistenteLancaNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(comandaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criarPresencial(id, requisicao(produto.getId(), 1)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage(ComandaService.MENSAGEM_COMANDA_NAO_ENCONTRADA);
    }

    @Test
    void comandaFechadaLancaExcecaoSemSalvar() {
        comanda.fechar(BigDecimal.ZERO, null);
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));

        assertThatThrownBy(() -> pedidoService.criarPresencial(comanda.getId(), requisicao(produto.getId(), 1)))
                .isInstanceOf(ComandaFechadaException.class);
        verify(pedidoRepository, never()).save(any());
        verify(mensageria, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void produtoInexistenteLancaNaoEncontrado() {
        UUID produtoInexistente = UUID.randomUUID();
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));
        when(produtoRepository.findById(produtoInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criarPresencial(comanda.getId(), requisicao(produtoInexistente, 1)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage(PedidoService.MENSAGEM_PRODUTO_NAO_ENCONTRADO);
        verify(pedidoRepository, never()).save(any());
        verify(mensageria, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void produtoIndisponivelLancaExcecao() {
        Produto esgotado = new Produto(UUID.randomUUID(), "Atum", null, BigDecimal.TEN, "PIZZA", false);
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));
        when(produtoRepository.findById(esgotado.getId())).thenReturn(Optional.of(esgotado));

        assertThatThrownBy(() -> pedidoService.criarPresencial(comanda.getId(), requisicao(esgotado.getId(), 1)))
                .isInstanceOf(ProdutoIndisponivelException.class)
                .hasMessage(PedidoService.MENSAGEM_PRODUTO_INDISPONIVEL);
        verify(pedidoRepository, never()).save(any());
        verify(mensageria, never()).convertAndSend(anyString(), any(Object.class));
    }

    private CriarPedidoRequest requisicao(UUID produtoId, int quantidade) {
        return new CriarPedidoRequest(List.of(new ItemPedidoRequest(produtoId, quantidade, null)));
    }
}
