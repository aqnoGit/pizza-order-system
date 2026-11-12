package com.pizzaria.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.pizzaria.dto.response.ComandaDetalhesResponse;
import com.pizzaria.dto.response.ComandaResponse;
import com.pizzaria.entity.Comanda;
import com.pizzaria.entity.StatusComanda;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ComandaMapperTest {

    private final ComandaMapper comandaMapper = new ComandaMapper();

    @Test
    void copiaTodosOsCamposDaComanda() {
        LocalDateTime abertaEm = LocalDateTime.now();
        Comanda comanda = new Comanda(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), StatusComanda.ABERTA,
                abertaEm, null, BigDecimal.ZERO);

        ComandaResponse resposta = comandaMapper.paraResponse(comanda);

        assertThat(resposta).isEqualTo(new ComandaResponse(comanda.getId(), comanda.getMesaId(),
                comanda.getGarcomId(), StatusComanda.ABERTA, abertaEm, null, BigDecimal.ZERO));
    }

    @Test
    void detalhesCopiamOsCamposEIniciamSemItens() {
        LocalDateTime abertaEm = LocalDateTime.now();
        Comanda comanda = new Comanda(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), StatusComanda.ABERTA,
                abertaEm, null, BigDecimal.ZERO);

        ComandaDetalhesResponse resposta = comandaMapper.paraDetalhes(comanda);

        assertThat(resposta).isEqualTo(new ComandaDetalhesResponse(comanda.getId(), comanda.getMesaId(),
                comanda.getGarcomId(), StatusComanda.ABERTA, abertaEm, null, BigDecimal.ZERO, List.of()));
    }
}
