package com.pizzaria.service;

import com.pizzaria.dto.response.MesaResponse;
import com.pizzaria.mapper.MesaMapper;
import com.pizzaria.repository.MesaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MesaService {

    private final MesaRepository mesaRepository;
    private final MesaMapper mesaMapper;

    @Transactional(readOnly = true)
    public List<MesaResponse> listar() {
        return mesaRepository.findAllByOrderByNumeroAsc().stream()
                .map(mesaMapper::paraResponse)
                .toList();
    }
}
