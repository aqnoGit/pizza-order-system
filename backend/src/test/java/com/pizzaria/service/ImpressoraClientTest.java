package com.pizzaria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pizzaria.config.ImpressoraProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class ImpressoraClientTest {

    private static final int TIMEOUT_MILISSEGUNDOS = 1000;

    @Test
    void enviaOsBytesExatosParaOServidorTcpDaImpressora() throws Exception {
        byte[] conteudo = {27, 64, 72, 105, 10, 29, 86, 0};
        try (ServerSocket impressora = new ServerSocket(0)) {
            CompletableFuture<byte[]> recebido = CompletableFuture.supplyAsync(() -> lerTudo(impressora));
            ImpressoraClient cliente = new ImpressoraClient(
                    new ImpressoraProperties("localhost", impressora.getLocalPort(), TIMEOUT_MILISSEGUNDOS));

            cliente.enviar(conteudo);

            assertThat(recebido.get()).isEqualTo(conteudo);
        }
    }

    @Test
    void lancaErroQuandoImpressoraNaoEstaAcessivel() throws IOException {
        int portaFechada;
        try (ServerSocket reservada = new ServerSocket(0)) {
            portaFechada = reservada.getLocalPort();
        }
        ImpressoraClient cliente = new ImpressoraClient(
                new ImpressoraProperties("localhost", portaFechada, TIMEOUT_MILISSEGUNDOS));

        assertThatThrownBy(() -> cliente.enviar(new byte[] {1})).isInstanceOf(UncheckedIOException.class);
    }

    private byte[] lerTudo(ServerSocket servidor) {
        try (Socket conexao = servidor.accept()) {
            return conexao.getInputStream().readAllBytes();
        } catch (IOException erro) {
            throw new UncheckedIOException(erro);
        }
    }
}
