package com.pizzaria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.pizzaria.config.PizzariaProperties;
import com.pizzaria.dto.response.ComprovanteResponse;
import com.pizzaria.entity.Comanda;
import com.pizzaria.entity.ItemPedido;
import com.pizzaria.entity.Mesa;
import com.pizzaria.entity.Pedido;
import com.pizzaria.entity.Produto;
import com.pizzaria.entity.StatusMesa;
import com.pizzaria.exception.RecursoNaoEncontradoException;
import com.pizzaria.mapper.ComprovanteMapper;
import com.pizzaria.repository.ComandaRepository;
import com.pizzaria.repository.MesaRepository;
import com.pizzaria.repository.PedidoRepository;
import com.pizzaria.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComprovanteServiceTest {

    private static final Instant INSTANTE_FIXO = Instant.parse("2026-11-12T20:30:00Z");

    @Mock
    private ComandaRepository comandaRepository;

    @Mock
    private MesaRepository mesaRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    private ComprovanteService comprovanteService;

    private final Produto calabresa = new Produto(UUID.randomUUID(), "Calabresa", null, new BigDecimal("42.50"),
            "PIZZA", true);
    private final Produto refrigerante = new Produto(UUID.randomUUID(), "Refrigerante", null, new BigDecimal("8.00"),
            "BEBIDA", true);
    private final Mesa mesa = new Mesa(UUID.randomUUID(), 5, StatusMesa.OCUPADA);
    private final Comanda comanda = Comanda.abrir(mesa.getId(), UUID.randomUUID());

    @BeforeEach
    void criarServico() {
        comprovanteService = new ComprovanteService(comandaRepository, mesaRepository, pedidoRepository,
                produtoRepository, new ComprovanteMapper(), new PizzariaProperties("Pede Pizza"),
                Clock.fixed(INSTANTE_FIXO, ZoneOffset.UTC));
    }

    @Test
    void geraComprovanteComMesaItensSubtotaisTotalEDataHora() {
        Pedido pedido = Pedido.presencial(comanda.getId(),
                List.of(ItemPedido.de(calabresa, 2), ItemPedido.de(refrigerante, 3)));
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));
        when(mesaRepository.findById(mesa.getId())).thenReturn(Optional.of(mesa));
        when(pedidoRepository.findByComandaId(comanda.getId())).thenReturn(List.of(pedido));
        when(produtoRepository.findAllById(Set.of(calabresa.getId(), refrigerante.getId())))
                .thenReturn(List.of(calabresa, refrigerante));

        ComprovanteResponse comprovante = comprovanteService.gerar(comanda.getId());

        assertThat(comprovante.nomePizzaria()).isEqualTo("Pede Pizza");
        assertThat(comprovante.numeroMesa()).isEqualTo(5);
        assertThat(comprovante.emitidoEm()).isEqualTo(LocalDateTime.of(2026, 11, 12, 20, 30));
        assertThat(comprovante.itens()).hasSize(2);
        assertThat(comprovante.itens().get(0).nome()).isEqualTo("Calabresa");
        assertThat(comprovante.itens().get(0).quantidade()).isEqualTo(2);
        assertThat(comprovante.itens().get(0).precoUnitario()).isEqualByComparingTo("42.50");
        assertThat(comprovante.itens().get(0).subtotal()).isEqualByComparingTo("85.00");
        assertThat(comprovante.itens().get(1).subtotal()).isEqualByComparingTo("24.00");
        assertThat(comprovante.valorTotal()).isEqualByComparingTo("109.00");
    }

    @Test
    void somaItensDeVariosPedidosDaMesmaComanda() {
        Pedido primeiro = Pedido.presencial(comanda.getId(), List.of(ItemPedido.de(calabresa, 1)));
        Pedido segundo = Pedido.presencial(comanda.getId(), List.of(ItemPedido.de(calabresa, 2)));
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));
        when(mesaRepository.findById(mesa.getId())).thenReturn(Optional.of(mesa));
        when(pedidoRepository.findByComandaId(comanda.getId())).thenReturn(List.of(primeiro, segundo));
        when(produtoRepository.findAllById(Set.of(calabresa.getId()))).thenReturn(List.of(calabresa));

        ComprovanteResponse comprovante = comprovanteService.gerar(comanda.getId());

        assertThat(comprovante.itens()).hasSize(2);
        assertThat(comprovante.valorTotal()).isEqualByComparingTo("127.50");
    }

    @Test
    void geraComprovanteComListaVaziaETotalZeroParaComandaSemItens() {
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));
        when(mesaRepository.findById(mesa.getId())).thenReturn(Optional.of(mesa));
        when(pedidoRepository.findByComandaId(comanda.getId())).thenReturn(List.of());
        when(produtoRepository.findAllById(Set.of())).thenReturn(List.of());

        ComprovanteResponse comprovante = comprovanteService.gerar(comanda.getId());

        assertThat(comprovante.itens()).isEmpty();
        assertThat(comprovante.valorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void lancaNaoEncontradoQuandoComandaNaoExiste() {
        UUID id = UUID.randomUUID();
        when(comandaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comprovanteService.gerar(id)).isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage(ComandaService.MENSAGEM_COMANDA_NAO_ENCONTRADA);
    }

    @Test
    void lancaNaoEncontradoQuandoMesaDaComandaNaoExiste() {
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));
        when(mesaRepository.findById(mesa.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comprovanteService.gerar(comanda.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage(ComandaService.MENSAGEM_MESA_NAO_ENCONTRADA);
    }
}
