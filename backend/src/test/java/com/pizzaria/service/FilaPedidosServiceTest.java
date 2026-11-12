package com.pizzaria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pizzaria.dto.response.PedidoFilaResponse;
import com.pizzaria.entity.Comanda;
import com.pizzaria.entity.ItemPedido;
import com.pizzaria.entity.Mesa;
import com.pizzaria.entity.Pedido;
import com.pizzaria.entity.Produto;
import com.pizzaria.entity.StatusMesa;
import com.pizzaria.entity.StatusPedido;
import com.pizzaria.mapper.PedidoFilaMapper;
import com.pizzaria.repository.ComandaRepository;
import com.pizzaria.repository.MesaRepository;
import com.pizzaria.repository.PedidoRepository;
import com.pizzaria.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilaPedidosServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ComandaRepository comandaRepository;

    @Mock
    private MesaRepository mesaRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Spy
    private PedidoFilaMapper pedidoFilaMapper = new PedidoFilaMapper();

    @InjectMocks
    private FilaPedidosService filaPedidosService;

    private final Produto produto = new Produto(UUID.randomUUID(), "Calabresa", null, new BigDecimal("42.50"),
            "PIZZA", true);
    private final Mesa mesa = new Mesa(UUID.randomUUID(), 5, StatusMesa.OCUPADA);
    private final Comanda comanda = Comanda.abrir(mesa.getId(), UUID.randomUUID());

    @Test
    void devolveFilaComNumeroDaMesaENomeDoProduto() {
        Pedido pedido = Pedido.presencial(comanda.getId(), List.of(ItemPedido.de(produto, 2)));
        when(pedidoRepository.findByStatusOrderByRegistradoEmAsc(StatusPedido.REGISTRADO))
                .thenReturn(List.of(pedido));
        when(comandaRepository.findAllById(Set.of(comanda.getId()))).thenReturn(List.of(comanda));
        when(mesaRepository.findAllById(Set.of(mesa.getId()))).thenReturn(List.of(mesa));
        when(produtoRepository.findAllById(Set.of(produto.getId()))).thenReturn(List.of(produto));

        List<PedidoFilaResponse> fila = filaPedidosService.listarFila();

        assertThat(fila).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(pedido.getId());
            assertThat(item.numeroMesa()).isEqualTo(5);
            assertThat(item.itens().get(0).nomeProduto()).isEqualTo("Calabresa");
        });
    }

    @Test
    void devolveListaVaziaQuandoNaoHaPedidosRegistrados() {
        when(pedidoRepository.findByStatusOrderByRegistradoEmAsc(StatusPedido.REGISTRADO)).thenReturn(List.of());
        when(comandaRepository.findAllById(Set.of())).thenReturn(List.of());
        when(mesaRepository.findAllById(Set.of())).thenReturn(List.of());
        when(produtoRepository.findAllById(Set.of())).thenReturn(List.of());

        assertThat(filaPedidosService.listarFila()).isEmpty();
    }
}
