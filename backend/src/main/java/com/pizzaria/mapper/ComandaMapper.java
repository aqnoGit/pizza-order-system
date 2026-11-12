package com.pizzaria.mapper;

import com.pizzaria.dto.response.ComandaResponse;
import com.pizzaria.entity.Comanda;
import org.springframework.stereotype.Component;

@Component
public class ComandaMapper {

    public ComandaResponse paraResponse(Comanda comanda) {
        return new ComandaResponse(comanda.getId(), comanda.getMesaId(), comanda.getGarcomId(), comanda.getStatus(),
                comanda.getAbertaEm(), comanda.getFechadaEm(), comanda.getValorTotal());
    }
}
