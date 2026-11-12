package com.pizzaria.mapper;

import com.pizzaria.dto.response.ComandaDetalhesResponse;
import com.pizzaria.dto.response.ComandaResponse;
import com.pizzaria.entity.Comanda;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ComandaMapper {

    public ComandaResponse paraResponse(Comanda comanda) {
        return new ComandaResponse(comanda.getId(), comanda.getMesaId(), comanda.getGarcomId(), comanda.getStatus(),
                comanda.getAbertaEm(), comanda.getFechadaEm(), comanda.getValorTotal(), comanda.getObservacao());
    }

    public ComandaDetalhesResponse paraDetalhes(Comanda comanda) {
        return new ComandaDetalhesResponse(comanda.getId(), comanda.getMesaId(), comanda.getGarcomId(),
                comanda.getStatus(), comanda.getAbertaEm(), comanda.getFechadaEm(), comanda.getValorTotal(),
                List.of());
    }
}
