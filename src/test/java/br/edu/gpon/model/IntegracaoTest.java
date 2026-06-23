package br.edu.gpon.model;

import br.edu.gpon.util.Constantes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntegracaoTest {

    private static final double DELTA = 0.01;

    @Test
    @DisplayName("Cenario 1: Provedor regional — downstream, splitter 1:32, 8 km, 4 conectores, 6 fusoes")
    void cenarioProvedorRegionalDownstream() {
        LinkBudget lb = new LinkBudget(
                4.5, null, 8.0, 32, 4, 6, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        assertEquals(LinkBudget.Variavel.P_RX, lb.getVariavelFaltante());

        double atenuacaoFibra = 0.35 * 8.0;
        double atenuacaoSplitter = Constantes.atenuacaoSplitter(32);
        double atenuacaoConectores = 4 * Constantes.PERDA_POR_CONECTOR_DB;
        double atenuacaoFusoes = 6 * Constantes.PERDA_POR_FUSAO_DB;
        double margem = 3.0;
        double atenuacaoTotal = atenuacaoFibra + atenuacaoSplitter + atenuacaoConectores + atenuacaoFusoes + margem;

        assertEquals(atenuacaoTotal, lb.calcularAtenuacaoTotal(), DELTA);
        assertEquals(4.5 - atenuacaoTotal, lb.getResultado(), DELTA);

        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        long erros = alertas.stream().filter(a -> a.getTipo() == TipoAlerta.ERRO).count();
        assertEquals(0, erros, "Cenario valido nao deve ter erros: " + alertas);
    }

    @Test
    @DisplayName("Cenario 2: Enlace longo — downstream, splitter 1:8, 15 km, calcular distancia maxima")
    void cenarioEnlaceLongo() {
        LinkBudget lb = new LinkBudget(
                5.0, -28.0, null, 8, 3, 4, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        assertEquals(LinkBudget.Variavel.DISTANCIA, lb.getVariavelFaltante());

        double perdaFixa = Constantes.atenuacaoSplitter(8)
                + 3 * Constantes.PERDA_POR_CONECTOR_DB
                + 4 * Constantes.PERDA_POR_FUSAO_DB
                + 3.0;
        double distanciaEsperada = (5.0 - (-28.0) - perdaFixa) / 0.35;
        assertEquals(distanciaEsperada, lb.getResultado(), DELTA);

        assertTrue(lb.getDistancia() > 0 && lb.getDistancia() <= 60);
    }

    @Test
    @DisplayName("Cenario 3: Upstream classe C++ — splitter 1:16, 10 km, calcular P_rx")
    void cenarioUpstreamClasseCMaisMais() {
        LinkBudget lb = new LinkBudget(
                6.0, null, 10.0, 16, 2, 3, 3.0,
                ComprimentoOnda.UPSTREAM_1310
        );
        lb.calcular();

        double atenuacao = 0.40 * 10.0
                + Constantes.atenuacaoSplitter(16)
                + 2 * Constantes.PERDA_POR_CONECTOR_DB
                + 3 * Constantes.PERDA_POR_FUSAO_DB
                + 3.0;
        double pRxEsperado = 6.0 - atenuacao;

        assertEquals(pRxEsperado, lb.getResultado(), DELTA);

        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.C_PLUS_PLUS);
        long erros = alertas.stream().filter(a -> a.getTipo() == TipoAlerta.ERRO).count();
        assertEquals(0, erros, "Cenario C++ valido: " + alertas);
    }

    @Test
    @DisplayName("Cenario 4: Enlace inviavel — splitter 1:64, 30 km, downstream B+")
    void cenarioEnlaceInviavel() {
        LinkBudget lb = new LinkBudget(
                4.0, null, 30.0, 64, 4, 6, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        long erros = alertas.stream().filter(a -> a.getTipo() == TipoAlerta.ERRO).count();
        assertTrue(erros >= 2, "Enlace inviavel deve ter multiplos erros: " + alertas);

        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.ERRO && a.getCampoAfetado().equals("P_rx")));
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.ERRO && a.getCampoAfetado().equals("Atenuação Total")));
    }

    @Test
    @DisplayName("Cenario 5: Calcular P_tx necessario para enlace classe C+")
    void cenarioCalcularPTxNecessario() {
        LinkBudget lb = new LinkBudget(
                null, -31.0, 12.0, 32, 2, 4, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        assertEquals(LinkBudget.Variavel.P_TX, lb.getVariavelFaltante());

        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.C_PLUS);
        assertTrue(alertas.stream().anyMatch(a -> a.getTipo() == TipoAlerta.ERRO
                && a.getCampoAfetado().equals("P_tx")),
                "P_tx calculado deve exceder max C+: " + alertas);
    }

    @Test
    @DisplayName("Cenario 6: Margem minima — enlace no limite da classe")
    void cenarioMargemMinima() {
        LinkBudget lb = new LinkBudget(
                3.5, -14.0, 10.0, 16, 2, 2, null,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        assertEquals(LinkBudget.Variavel.MARGEM, lb.getVariavelFaltante());
        assertTrue(lb.getMargem() < 1.0, "Margem deve ser minima");

        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.AVISO && a.getCampoAfetado().equals("Margem")),
                "Deve gerar AVISO de margem baixa: " + alertas);
    }

    @Test
    @DisplayName("Cenario 7: Maximo de conectores suportado com splitter 1:64")
    void cenarioMaximoConectores() {
        LinkBudget lb = new LinkBudget(
                5.0, -28.0, 3.0, 64, null, 0, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        assertEquals(LinkBudget.Variavel.CONECTORES, lb.getVariavelFaltante());
        assertTrue(lb.getConectores() >= 0);
    }

    @Test
    @DisplayName("Cenario 8: Fluxo completo — calcular, validar, verificar coerencia")
    void cenarioFluxoCompleto() {
        Double pTx = 4.5;
        Double distancia = 6.0;
        Integer splitter = 16;
        Integer conectores = 2;
        Integer fusoes = 3;
        Double margem = 3.0;

        LinkBudget lb = new LinkBudget(
                pTx, null, distancia, splitter, conectores, fusoes, margem,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        Double pRx = lb.getpRx();
        assertNotNull(pRx);

        double atenuacaoTotal = lb.calcularAtenuacaoTotal();
        assertEquals(pTx - pRx, atenuacaoTotal, DELTA,
                "P_tx - P_rx deve ser igual a atenuacao total");

        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertFalse(alertas.isEmpty());

        double pRxVerificacao = pTx - atenuacaoTotal;
        assertEquals(pRx, pRxVerificacao, DELTA);
    }

    @Test
    @DisplayName("Cenario 9: Downstream vs Upstream — mesmo enlace, resultados diferentes")
    void cenarioDownstreamVsUpstreamCompleto() {
        LinkBudget lbDown = new LinkBudget(
                4.5, null, 12.0, 16, 2, 4, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        LinkBudget lbUp = new LinkBudget(
                4.5, null, 12.0, 16, 2, 4, 3.0,
                ComprimentoOnda.UPSTREAM_1310
        );

        lbDown.calcular();
        lbUp.calcular();

        double diff = lbDown.getpRx() - lbUp.getpRx();
        assertEquals((0.40 - 0.35) * 12.0, diff, DELTA,
                "Diferença deve ser exatamente (0.40-0.35)*12 = 0.6 dB");

        List<Alerta> alertasDown = Validador.validar(lbDown, ClasseGPON.B_PLUS);
        List<Alerta> alertasUp = Validador.validar(lbUp, ClasseGPON.B_PLUS);

        assertFalse(alertasDown.isEmpty());
        assertFalse(alertasUp.isEmpty());
    }

    @Test
    @DisplayName("Cenario 10: Todos os splitters geram atenuacao correta")
    void cenarioTodosSplitters() {
        int[] ratios = {2, 4, 8, 16, 32, 64};
        for (int ratio : ratios) {
            double esperado = 10.0 * Math.log10(ratio);
            assertEquals(esperado, Constantes.atenuacaoSplitter(ratio), DELTA,
                    "Splitter 1:" + ratio + " deve ter " + esperado + " dB");
        }
    }
}
