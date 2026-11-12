package com.pizzaria.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.pizzaria.dto.response.MesaResponse;
import com.pizzaria.entity.Mesa;
import com.pizzaria.entity.StatusMesa;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MesaMapperTest {

    private final MesaMapper mesaMapper = new MesaMapper();

    @Test
    void converteMesaParaResponse() {
        UUID id = UUID.randomUUID();

        MesaResponse resposta = mesaMapper.paraResponse(new Mesa(id, 7, StatusMesa.OCUPADA));

        assertThat(resposta).isEqualTo(new MesaResponse(id, 7, StatusMesa.OCUPADA));
    }
}
