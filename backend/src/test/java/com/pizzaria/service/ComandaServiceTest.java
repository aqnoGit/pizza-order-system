package com.pizzaria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pizzaria.dto.request.CriarComandaRequest;
import com.pizzaria.dto.response.ComandaDetalhesResponse;
import com.pizzaria.dto.response.ComandaResponse;
import com.pizzaria.entity.Comanda;
import com.pizzaria.entity.Mesa;
import com.pizzaria.entity.StatusComanda;
import com.pizzaria.entity.StatusMesa;
import com.pizzaria.exception.ConflitoException;
import com.pizzaria.exception.RecursoNaoEncontradoException;
import com.pizzaria.mapper.ComandaMapper;
import com.pizzaria.repository.ComandaRepository;
import com.pizzaria.repository.MesaRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComandaServiceTest {

    @Mock
    private ComandaRepository comandaRepository;

    @Mock
    private MesaRepository mesaRepository;

    @Spy
    private ComandaMapper comandaMapper = new ComandaMapper();

    @InjectMocks
    private ComandaService comandaService;

    private final UUID mesaId = UUID.randomUUID();
    private final UUID garcomId = UUID.randomUUID();
    private final Mesa mesa = new Mesa(mesaId, 1, StatusMesa.LIVRE);

    @Test
    void criaComandaAbertaComValorZeradoEGarcomDoToken() {
        when(mesaRepository.findById(mesaId)).thenReturn(Optional.of(mesa));
        when(comandaRepository.save(any(Comanda.class))).thenAnswer(chamada -> chamada.getArgument(0));

        ComandaResponse resposta = comandaService.criar(new CriarComandaRequest(mesaId), garcomId);

        assertThat(resposta.mesaId()).isEqualTo(mesaId);
        assertThat(resposta.garcomId()).isEqualTo(garcomId);
        assertThat(resposta.status()).isEqualTo(StatusComanda.ABERTA);
        assertThat(resposta.valorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resposta.abertaEm()).isNotNull();
        assertThat(resposta.fechadaEm()).isNull();
    }

    @Test
    void mudaAMesaParaOcupadaAoCriarAComanda() {
        when(mesaRepository.findById(mesaId)).thenReturn(Optional.of(mesa));
        when(comandaRepository.save(any(Comanda.class))).thenAnswer(chamada -> chamada.getArgument(0));

        comandaService.criar(new CriarComandaRequest(mesaId), garcomId);

        assertThat(mesa.getStatus()).isEqualTo(StatusMesa.OCUPADA);
    }

    @Test
    void lancaNaoEncontradoQuandoAMesaNaoExiste() {
        when(mesaRepository.findById(mesaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comandaService.criar(new CriarComandaRequest(mesaId), garcomId))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage(ComandaService.MENSAGEM_MESA_NAO_ENCONTRADA);
        verify(comandaRepository, never()).save(any());
    }

    @Test
    void mantemAMesaLivreQuandoJaExisteComandaAberta() {
        when(mesaRepository.findById(mesaId)).thenReturn(Optional.of(mesa));
        when(comandaRepository.existsByMesaIdAndStatus(mesaId, StatusComanda.ABERTA)).thenReturn(true);

        assertThatThrownBy(() -> comandaService.criar(new CriarComandaRequest(mesaId), garcomId))
                .isInstanceOf(ConflitoException.class);
        assertThat(mesa.getStatus()).isEqualTo(StatusMesa.LIVRE);
    }

    @Test
    void lancaConflitoQuandoAMesaJaTemComandaAberta() {
        when(mesaRepository.findById(mesaId)).thenReturn(Optional.of(mesa));
        when(comandaRepository.existsByMesaIdAndStatus(mesaId, StatusComanda.ABERTA)).thenReturn(true);

        assertThatThrownBy(() -> comandaService.criar(new CriarComandaRequest(mesaId), garcomId))
                .isInstanceOf(ConflitoException.class)
                .hasMessage(ComandaService.MENSAGEM_MESA_COM_COMANDA_ABERTA);
        verify(comandaRepository, never()).save(any());
    }

    @Test
    void buscaComandaExistenteSemItens() {
        Comanda comanda = Comanda.abrir(mesaId, garcomId);
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));

        ComandaDetalhesResponse resposta = comandaService.buscarPorId(comanda.getId());

        assertThat(resposta.id()).isEqualTo(comanda.getId());
        assertThat(resposta.mesaId()).isEqualTo(mesaId);
        assertThat(resposta.status()).isEqualTo(StatusComanda.ABERTA);
        assertThat(resposta.itens()).isEmpty();
    }

    @Test
    void lancaNaoEncontradoQuandoAComandaNaoExiste() {
        UUID id = UUID.randomUUID();
        when(comandaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comandaService.buscarPorId(id))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage(ComandaService.MENSAGEM_COMANDA_NAO_ENCONTRADA);
    }

    @Test
    void fechaComandaAbertaRegistrandoADataDeFechamento() {
        Comanda comanda = Comanda.abrir(mesaId, garcomId);
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));

        ComandaResponse resposta = comandaService.fechar(comanda.getId());

        assertThat(resposta.status()).isEqualTo(StatusComanda.FECHADA);
        assertThat(resposta.fechadaEm()).isNotNull();
    }

    @Test
    void fechaComandaGravandoOValorTotalSomadoDosItens() {
        Comanda comanda = Comanda.abrir(mesaId, garcomId);
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));
        when(comandaRepository.somarValorDosItens(comanda.getId())).thenReturn(new BigDecimal("87.50"));

        ComandaResponse resposta = comandaService.fechar(comanda.getId());

        assertThat(resposta.valorTotal()).isEqualByComparingTo("87.50");
    }

    @Test
    void fechaComandaSemItensComValorTotalZero() {
        Comanda comanda = Comanda.abrir(mesaId, garcomId);
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));
        when(comandaRepository.somarValorDosItens(comanda.getId())).thenReturn(BigDecimal.ZERO);

        ComandaResponse resposta = comandaService.fechar(comanda.getId());

        assertThat(resposta.valorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void naoSomaOsItensQuandoAComandaJaEstaFechada() {
        Comanda comanda = Comanda.abrir(mesaId, garcomId);
        comanda.fechar(BigDecimal.ZERO);
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));

        assertThatThrownBy(() -> comandaService.fechar(comanda.getId())).isInstanceOf(ConflitoException.class);
        verify(comandaRepository, never()).somarValorDosItens(any());
    }

    @Test
    void lancaConflitoAoFecharComandaJaFechada() {
        Comanda comanda = Comanda.abrir(mesaId, garcomId);
        comanda.fechar(BigDecimal.ZERO);
        when(comandaRepository.findById(comanda.getId())).thenReturn(Optional.of(comanda));

        assertThatThrownBy(() -> comandaService.fechar(comanda.getId()))
                .isInstanceOf(ConflitoException.class)
                .hasMessage(ComandaService.MENSAGEM_COMANDA_JA_FECHADA);
    }

    @Test
    void lancaNaoEncontradoAoFecharComandaInexistente() {
        UUID id = UUID.randomUUID();
        when(comandaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comandaService.fechar(id))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage(ComandaService.MENSAGEM_COMANDA_NAO_ENCONTRADA);
    }
}
