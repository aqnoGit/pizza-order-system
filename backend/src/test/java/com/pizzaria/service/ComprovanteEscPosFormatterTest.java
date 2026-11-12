package com.pizzaria.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.pizzaria.dto.response.ComprovanteResponse;
import com.pizzaria.dto.response.ItemComprovanteResponse;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ComprovanteEscPosFormatterTest {

    private static final Charset CODIFICACAO_IMPRESSORA = Charset.forName("Cp850");
    private static final byte ESC = 0x1B;
    private static final byte INICIALIZAR = '@';
    private static final byte GS = 0x1D;
    private static final byte CORTE = 'V';

    private final ComprovanteEscPosFormatter formatter = new ComprovanteEscPosFormatter();

    @Test
    void deveGerarBytesEscPosComCabecalhoItensETotal() {
        ComprovanteResponse comprovante = comprovante(List.of(
                new ItemComprovanteResponse("Pizza Calabresa", 2, new BigDecimal("30.00"), new BigDecimal("60.00"))),
                new BigDecimal("60.00"));

        byte[] bytes = formatter.formatar(comprovante);
        String texto = new String(bytes, CODIFICACAO_IMPRESSORA);

        assertThat(bytes[0]).isEqualTo(ESC);
        assertThat(bytes[1]).isEqualTo(INICIALIZAR);
        assertThat(texto).contains("Pizzaria Teste", "Mesa 7", "12/11/2026 19:30", "2x Pizza Calabresa",
                "R$ 60,00", "TOTAL");
        assertThat(contem(bytes, GS, CORTE)).isTrue();
    }

    @Test
    void deveGerarCabecalhoETotalZeradoQuandoNaoHaItens() {
        byte[] bytes = formatter.formatar(comprovante(List.of(), BigDecimal.ZERO));
        String texto = new String(bytes, CODIFICACAO_IMPRESSORA);

        assertThat(texto).contains("Pizzaria Teste", "Mesa 7", "TOTAL", "R$ 0,00");
    }

    @Test
    void deveQuebrarNomeLongoSemUltrapassarAsColunasDoPapel() {
        String nomeLongo = "Pizza Especial da Casa com Queijo Catupiry Azeitonas Pretas e Muito Oregano Fresco";
        ItemComprovanteResponse item = new ItemComprovanteResponse(nomeLongo, 1, new BigDecimal("55.90"),
                new BigDecimal("55.90"));

        List<String> linhas = formatter.linhasDoItem(item);

        assertThat(linhas).hasSizeGreaterThan(1);
        assertThat(linhas).allSatisfy(linha -> assertThat(linha).hasSizeLessThanOrEqualTo(
                ComprovanteEscPosFormatter.COLUNAS_80MM));
        assertThat(linhas.get(linhas.size() - 1)).endsWith("R$ 55,90");
        assertThat(linhas.get(0)).startsWith("1x Pizza Especial");
    }

    @Test
    void deveAlinharValorNaExtremidadeDireitaDeItemCurto() {
        ItemComprovanteResponse item = new ItemComprovanteResponse("Suco", 3, new BigDecimal("8.00"),
                new BigDecimal("24.00"));

        List<String> linhas = formatter.linhasDoItem(item);

        assertThat(linhas).singleElement().satisfies(linha -> {
            assertThat(linha).hasSize(ComprovanteEscPosFormatter.COLUNAS_80MM);
            assertThat(linha).startsWith("3x Suco").endsWith("R$ 24,00");
        });
    }

    @Test
    void deveCodificarAcentosNaTabelaDaImpressora() {
        ComprovanteResponse comprovante = new ComprovanteResponse("Pizzaria Açaí", 1, List.of(),
                BigDecimal.ZERO, LocalDateTime.of(2026, 11, 12, 19, 30));

        String texto = new String(formatter.formatar(comprovante), CODIFICACAO_IMPRESSORA);

        assertThat(texto).contains("Pizzaria Açaí");
    }

    private ComprovanteResponse comprovante(List<ItemComprovanteResponse> itens, BigDecimal total) {
        return new ComprovanteResponse("Pizzaria Teste", 7, itens, total, LocalDateTime.of(2026, 11, 12, 19, 30));
    }

    private boolean contem(byte[] bytes, byte primeiro, byte segundo) {
        for (int i = 0; i < bytes.length - 1; i++) {
            if (bytes[i] == primeiro && bytes[i + 1] == segundo) {
                return true;
            }
        }
        return false;
    }
}
