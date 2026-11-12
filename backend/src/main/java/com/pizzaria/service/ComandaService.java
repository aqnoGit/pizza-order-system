package com.pizzaria.service;

import com.pizzaria.dto.request.CriarComandaRequest;
import com.pizzaria.dto.response.ComandaDetalhesResponse;
import com.pizzaria.dto.response.ComandaResponse;
import com.pizzaria.entity.Comanda;
import com.pizzaria.entity.Mesa;
import com.pizzaria.entity.StatusComanda;
import com.pizzaria.exception.ConflitoException;
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

    public static final String MENSAGEM_COMANDA_NAO_ENCONTRADA = "Comanda nao encontrada";

    public static final String MENSAGEM_MESA_COM_COMANDA_ABERTA = "Mesa ja possui uma comanda aberta";

    public static final String MENSAGEM_COMANDA_JA_FECHADA = "Comanda ja esta fechada";

    private final ComandaRepository comandaRepository;
    private final MesaRepository mesaRepository;
    private final ComandaMapper comandaMapper;

    @Transactional
    public ComandaResponse criar(CriarComandaRequest requisicao, UUID garcomId) {
        Mesa mesa = mesaRepository.findById(requisicao.mesaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(MENSAGEM_MESA_NAO_ENCONTRADA));
        if (comandaRepository.existsByMesaIdAndStatus(requisicao.mesaId(), StatusComanda.ABERTA)) {
            throw new ConflitoException(MENSAGEM_MESA_COM_COMANDA_ABERTA);
        }
        mesa.ocupar();
        Comanda comanda = comandaRepository.save(Comanda.abrir(requisicao.mesaId(), garcomId));
        return comandaMapper.paraResponse(comanda);
    }

    @Transactional(readOnly = true)
    public ComandaDetalhesResponse buscarPorId(UUID id) {
        Comanda comanda = comandaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MENSAGEM_COMANDA_NAO_ENCONTRADA));
        return comandaMapper.paraDetalhes(comanda);
    }

    @Transactional
    public ComandaResponse fechar(UUID id) {
        Comanda comanda = comandaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(MENSAGEM_COMANDA_NAO_ENCONTRADA));
        if (comanda.estaFechada()) {
            throw new ConflitoException(MENSAGEM_COMANDA_JA_FECHADA);
        }
        comanda.fechar();
        return comandaMapper.paraResponse(comanda);
    }
}
