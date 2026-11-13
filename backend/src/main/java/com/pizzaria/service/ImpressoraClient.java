package com.pizzaria.service;

import com.pizzaria.config.ImpressoraProperties;
import com.pizzaria.exception.ImpressoraIndisponivelException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImpressoraClient {

    private static final String MENSAGEM_INDISPONIVEL =
            "Impressora indisponivel. O comprovante continua disponivel para reimpressao";

    private final ImpressoraProperties propriedades;

    public void enviar(byte[] conteudo) {
        try (Socket conexao = new Socket()) {
            conexao.connect(new InetSocketAddress(propriedades.host(), propriedades.porta()),
                    propriedades.timeoutMilissegundos());
            escreverComLimiteDeTempo(conexao, conteudo);
        } catch (IOException erro) {
            throw new ImpressoraIndisponivelException(MENSAGEM_INDISPONIVEL, erro);
        }
    }

    private void escreverComLimiteDeTempo(Socket conexao, byte[] conteudo) throws IOException {
        CompletableFuture<Void> escrita = CompletableFuture.runAsync(() -> escrever(conexao, conteudo));
        try {
            escrita.get(propriedades.timeoutMilissegundos(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException erro) {
            throw new IOException("Impressora nao respondeu dentro do tempo limite", erro);
        } catch (ExecutionException erro) {
            throw new IOException("Falha ao escrever na impressora", erro.getCause());
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new IOException("Envio para a impressora interrompido", erro);
        }
    }

    private void escrever(Socket conexao, byte[] conteudo) {
        try {
            var saida = conexao.getOutputStream();
            saida.write(conteudo);
            saida.flush();
        } catch (IOException erro) {
            throw new UncheckedIOException(erro);
        }
    }
}
