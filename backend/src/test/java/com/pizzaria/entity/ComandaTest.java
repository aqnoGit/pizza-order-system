package com.pizzaria.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pizzaria.exception.ComandaFechadaException;
import com.pizzaria.exception.ObservacaoInvalidaException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ComandaTest {

    @Test
    void naoDeveLancarExcecaoQuandoComandaEstaAberta() {
        Comanda comanda = Comanda.abrir(UUID.randomUUID(), UUID.randomUUID());

        assertThatCode(comanda::exigirAberta).doesNotThrowAnyException();
    }

    @Test
    void deveLancarExcecaoQuandoComandaEstaFechada() {
        Comanda comanda = Comanda.abrir(UUID.randomUUID(), UUID.randomUUID());
        comanda.fechar(BigDecimal.TEN, null);

        assertThatThrownBy(comanda::exigirAberta)
                .isInstanceOf(ComandaFechadaException.class)
                .hasMessage(Comanda.MENSAGEM_COMANDA_FECHADA);
    }

    @Test
    void fecharGravaAObservacaoInformada() {
        Comanda comanda = Comanda.abrir(UUID.randomUUID(), UUID.randomUUID());

        comanda.fechar(BigDecimal.TEN, "cliente pediu nota");

        assertThat(comanda.getObservacao()).isEqualTo("cliente pediu nota");
    }

    @Test
    void fecharAceitaObservacaoNula() {
        Comanda comanda = Comanda.abrir(UUID.randomUUID(), UUID.randomUUID());

        comanda.fechar(BigDecimal.TEN, null);

        assertThat(comanda.getObservacao()).isNull();
        assertThat(comanda.estaFechada()).isTrue();
    }

    @Test
    void fecharAceitaObservacaoComExatamente500Caracteres() {
        Comanda comanda = Comanda.abrir(UUID.randomUUID(), UUID.randomUUID());

        comanda.fechar(BigDecimal.TEN, "a".repeat(Comanda.TAMANHO_MAXIMO_OBSERVACAO));

        assertThat(comanda.estaFechada()).isTrue();
    }

    @Test
    void fecharRejeitaObservacaoComMaisDe500CaracteresEMantemAComandaAberta() {
        Comanda comanda = Comanda.abrir(UUID.randomUUID(), UUID.randomUUID());

        assertThatThrownBy(() -> comanda.fechar(BigDecimal.TEN, "a".repeat(Comanda.TAMANHO_MAXIMO_OBSERVACAO + 1)))
                .isInstanceOf(ObservacaoInvalidaException.class)
                .hasMessage(Comanda.MENSAGEM_OBSERVACAO_MUITO_LONGA);
        assertThat(comanda.estaFechada()).isFalse();
    }

    @Test
    void somarAoTotalAcumulaOsValoresInformados() {
        Comanda comanda = Comanda.abrir(UUID.randomUUID(), UUID.randomUUID());

        comanda.somarAoTotal(new BigDecimal("85.00"));
        comanda.somarAoTotal(new BigDecimal("14.50"));

        assertThat(comanda.getValorTotal()).isEqualByComparingTo("99.50");
    }
}
