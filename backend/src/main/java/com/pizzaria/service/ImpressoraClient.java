package com.pizzaria.service;

import com.pizzaria.config.ImpressoraProperties;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImpressoraClient {

    private final ImpressoraProperties propriedades;

    public void enviar(byte[] conteudo) {
        try (Socket conexao = new Socket()) {
            conexao.connect(new InetSocketAddress(propriedades.host(), propriedades.porta()),
                    propriedades.timeoutConexaoMilissegundos());
            OutputStream saida = conexao.getOutputStream();
            saida.write(conteudo);
            saida.flush();
        } catch (IOException erro) {
            throw new UncheckedIOException("Falha ao enviar comprovante para a impressora", erro);
        }
    }
}
