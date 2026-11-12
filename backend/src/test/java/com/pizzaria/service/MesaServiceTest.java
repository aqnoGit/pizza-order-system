package com.pizzaria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pizzaria.dto.response.MesaResponse;
import com.pizzaria.entity.Mesa;
import com.pizzaria.entity.StatusMesa;
import com.pizzaria.mapper.MesaMapper;
import com.pizzaria.repository.MesaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MesaServiceTest {

    @Mock
    private MesaRepository mesaRepository;

    @Spy
    private MesaMapper mesaMapper = new MesaMapper();

    @InjectMocks
    private MesaService mesaService;

    @Test
    void listaTodasAsMesasComStatusGravado() {
        Mesa livre = new Mesa(UUID.randomUUID(), 1, StatusMesa.LIVRE);
        Mesa ocupada = new Mesa(UUID.randomUUID(), 2, StatusMesa.OCUPADA);
        when(mesaRepository.findAllByOrderByNumeroAsc()).thenReturn(List.of(livre, ocupada));

        List<MesaResponse> resposta = mesaService.listar();

        assertThat(resposta).containsExactly(
                new MesaResponse(livre.getId(), 1, StatusMesa.LIVRE),
                new MesaResponse(ocupada.getId(), 2, StatusMesa.OCUPADA));
    }

    @Test
    void devolveListaVaziaQuandoNaoHaMesas() {
        when(mesaRepository.findAllByOrderByNumeroAsc()).thenReturn(List.of());

        assertThat(mesaService.listar()).isEmpty();
    }
}
