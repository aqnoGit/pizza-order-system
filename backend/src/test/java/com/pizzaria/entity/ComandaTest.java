package com.pizzaria.entity;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pizzaria.exception.ComandaFechadaException;
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
        comanda.fechar(BigDecimal.TEN);

        assertThatThrownBy(comanda::exigirAberta)
                .isInstanceOf(ComandaFechadaException.class)
                .hasMessage(Comanda.MENSAGEM_COMANDA_FECHADA);
    }
}
