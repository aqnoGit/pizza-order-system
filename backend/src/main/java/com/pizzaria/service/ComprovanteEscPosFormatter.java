package com.pizzaria.service;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.pizzaria.dto.response.ComprovanteResponse;
import com.pizzaria.dto.response.ItemComprovanteResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ComprovanteEscPosFormatter {

    static final int COLUNAS_80MM = 48;
    private static final int LINHAS_ANTES_DO_CORTE = 4;
    private static final String SEPARADOR = "-".repeat(COLUNAS_80MM);
    private static final String TITULO_TOTAL = "TOTAL";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale LOCALE_BRASIL = Locale.forLanguageTag("pt-BR");

    public byte[] formatar(ComprovanteResponse comprovante) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try (EscPos impressora = new EscPos(saida)) {
            impressora.initializePrinter();
            escreverCabecalho(impressora, comprovante);
            escreverItens(impressora, comprovante.itens());
            escreverTotal(impressora, comprovante.valorTotal());
            impressora.feed(LINHAS_ANTES_DO_CORTE);
            impressora.cut(EscPos.CutMode.FULL);
        } catch (IOException erro) {
            throw new UncheckedIOException(erro);
        }
        return saida.toByteArray();
    }

    private void escreverCabecalho(EscPos impressora, ComprovanteResponse comprovante) throws IOException {
        Style centralizado = new Style().setJustification(EscPosConst.Justification.Center);
        impressora.writeLF(new Style(centralizado).setBold(true), comprovante.nomePizzaria());
        impressora.writeLF(centralizado, "Mesa " + comprovante.numeroMesa());
        impressora.writeLF(centralizado, comprovante.emitidoEm().format(FORMATO_DATA));
        impressora.writeLF(SEPARADOR);
    }

    private void escreverItens(EscPos impressora, List<ItemComprovanteResponse> itens) throws IOException {
        for (ItemComprovanteResponse item : itens) {
            for (String linha : linhasDoItem(item)) {
                impressora.writeLF(linha);
            }
        }
        impressora.writeLF(SEPARADOR);
    }

    private void escreverTotal(EscPos impressora, BigDecimal valorTotal) throws IOException {
        impressora.writeLF(new Style().setBold(true), alinharNasExtremidades(TITULO_TOTAL, moeda(valorTotal)));
    }

    List<String> linhasDoItem(ItemComprovanteResponse item) {
        String valor = moeda(item.subtotal());
        String descricao = item.quantidade() + "x " + item.nome();
        int larguraDaDescricao = COLUNAS_80MM - valor.length() - 1;
        List<String> pedacos = quebrar(descricao, larguraDaDescricao);
        List<String> linhas = new ArrayList<>(pedacos.subList(0, pedacos.size() - 1));
        linhas.add(alinharNasExtremidades(pedacos.get(pedacos.size() - 1), valor));
        return linhas;
    }

    private List<String> quebrar(String texto, int largura) {
        List<String> pedacos = new ArrayList<>();
        for (int inicio = 0; inicio < texto.length(); inicio += largura) {
            pedacos.add(texto.substring(inicio, Math.min(inicio + largura, texto.length())));
        }
        return pedacos;
    }

    private String alinharNasExtremidades(String esquerda, String direita) {
        int espacos = COLUNAS_80MM - esquerda.length() - direita.length();
        return esquerda + " ".repeat(espacos) + direita;
    }

    private String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(LOCALE_BRASIL).format(valor).replace(' ', ' ');
    }
}
