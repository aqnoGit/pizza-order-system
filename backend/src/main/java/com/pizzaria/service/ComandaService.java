package com.pizzaria.service;

import com.pizzaria.dto.request.CriarComandaRequest;
import com.pizzaria.dto.response.ComandaResponse;
import com.pizzaria.entity.Comanda;
import com.pizzaria.exception.RecursoNaoEncontradoException;
import com.pizzaria.mapper.ComandaMapper;
import com.pizzaria.repository.ComandaRepository;
import com.pizzaria.repository.MesaRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComandaService {

    public static final String MENSAGEM_MESA_NAO_ENCONTRADA = "Mesa nao encontrada";

    private final ComandaRepository comandaRepository;
    private final MesaRepository mesaRepository;
    private final ComandaMapper comandaMapper;

    @Transactional
    public ComandaResponse criar(CriarComandaRequest requisicao, UUID garcomId) {
        if (!mesaRepository.existsById(requisicao.mesaId())) {
            throw new RecursoNaoEncontradoException(MENSAGEM_MESA_NAO_ENCONTRADA);
        }
        Comanda comanda = comandaRepository.save(Comanda.abrir(requisicao.mesaId(), garcomId));
        return comandaMapper.paraResponse(comanda);
    }
}
