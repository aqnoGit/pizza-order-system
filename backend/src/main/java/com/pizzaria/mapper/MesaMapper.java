package com.pizzaria.mapper;

import com.pizzaria.dto.response.MesaResponse;
import com.pizzaria.entity.Mesa;
import org.springframework.stereotype.Component;

@Component
public class MesaMapper {

    public MesaResponse paraResponse(Mesa mesa) {
        return new MesaResponse(mesa.getId(), mesa.getNumero(), mesa.getStatus());
    }
}
