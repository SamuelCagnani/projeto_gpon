package br.edu.gpon.model;

import br.edu.gpon.util.Constantes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkBudgetTest {

    private static final double DELTA = 0.01;

    @Test
    @DisplayName("Calcular P_rx no downstream com splitter 1:32, 8km, 4 conectores, 6 fusoes")
    void calcularPRxDownstream() {
        LinkBudget lb = new LinkBudget(
                5.0, null, 8.0, 32, 4, 6, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        assertEquals(LinkBudget.Variavel.P_RX, lb.getVariavelFaltante());
        double atenuacaoEsperada = 0.35 * 8.0
                + Constantes.atenuacaoSplitter(32)
                + 4 * Constantes.PERDA_POR_CONECTOR_DB
                + 6 * Constantes.PERDA_POR_FUSAO_DB
                + 3.0;
        double pRxEsperado = 5.0 - atenuacaoEsperada;
        assertEquals(pRxEsperado, lb.getResultado(), DELTA);
    }

    @Test
    @DisplayName("Calcular P_rx no upstream com os mesmos parametros")
    void calcularPRxUpstream() {
        LinkBudget lb = new LinkBudget(
                5.0, null, 8.0, 32, 4, 6, 3.0,
                ComprimentoOnda.UPSTREAM_1310
        );
        lb.calcular();

        double atenuacaoEsperada = 0.40 * 8.0
                + Constantes.atenuacaoSplitter(32)
                + 4 * Constantes.PERDA_POR_CONECTOR_DB
                + 6 * Constantes.PERDA_POR_FUSAO_DB
                + 3.0;
        double pRxEsperado = 5.0 - atenuacaoEsperada;
        assertEquals(pRxEsperado, lb.getResultado(), DELTA);
        assertTrue(lb.getResultado() < 5.0 - 0.35 * 8.0 - Constantes.atenuacaoSplitter(32)
                - 4 * 0.5 - 6 * 0.1 - 3.0);
    }

    @Test
    @DisplayName("Calcular P_tx necessario para alcancar sensibilidade")
    void calcularPTx() {
        LinkBudget lb = new LinkBudget(
                null, -28.0, 10.0, 16, 2, 4, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        assertEquals(LinkBudget.Variavel.P_TX, lb.getVariavelFaltante());
        double atenuacao = 0.35 * 10.0
                + Constantes.atenuacaoSplitter(16)
                + 2 * Constantes.PERDA_POR_CONECTOR_DB
                + 4 * Constantes.PERDA_POR_FUSAO_DB
                + 3.0;
        double pTxEsperado = -28.0 + atenuacao;
        assertEquals(pTxEsperado, lb.getResultado(), DELTA);
    }

    @Test
    @DisplayName("Calcular distancia maxima suportada")
    void calcularDistancia() {
        double pTx = 5.0;
        double pRx = -28.0;
        int splitRatio = 8;
        int conectores = 2;
        int fusoes = 3;
        double margem = 3.0;
        ComprimentoOnda onda = ComprimentoOnda.DOWNSTREAM_1490;

        LinkBudget lb = new LinkBudget(
                pTx, pRx, null, splitRatio, conectores, fusoes, margem, onda
        );
        lb.calcular();

        assertEquals(LinkBudget.Variavel.DISTANCIA, lb.getVariavelFaltante());
        double perdaFixa = Constantes.atenuacaoSplitter(splitRatio)
                + conectores * Constantes.PERDA_POR_CONECTOR_DB
                + fusoes * Constantes.PERDA_POR_FUSAO_DB
                + margem;
        double distanciaEsperada = (pTx - pRx - perdaFixa) / onda.getAtenuacaoDbPorKm();
        assertEquals(distanciaEsperada, lb.getResultado(), DELTA);
        assertTrue(lb.getResultado() > 0);
    }

    @Test
    @DisplayName("Calcular atenuacao do splitter (dB) faltante")
    void calcularSplitter() {
        LinkBudget lb = new LinkBudget(
                5.0, -28.0, 5.0, null, 2, 3, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        assertEquals(LinkBudget.Variavel.SPLITTER, lb.getVariavelFaltante());
        double perdaSemSplitter = 0.35 * 5.0
                + 2 * Constantes.PERDA_POR_CONECTOR_DB
                + 3 * Constantes.PERDA_POR_FUSAO_DB
                + 3.0;
        double atenuacaoSplitterEsperada = 5.0 - (-28.0) - perdaSemSplitter;
        assertEquals(atenuacaoSplitterEsperada, lb.getResultado(), DELTA);
        assertTrue(lb.getResultado() > 0);
    }

    @Test
    @DisplayName("Calcular numero maximo de conectores")
    void calcularConectores() {
        LinkBudget lb = new LinkBudget(
                5.0, -28.0, 5.0, 16, null, 3, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        assertEquals(LinkBudget.Variavel.CONECTORES, lb.getVariavelFaltante());
        double perdaSemConectores = 0.35 * 5.0
                + Constantes.atenuacaoSplitter(16)
                + 3 * Constantes.PERDA_POR_FUSAO_DB
                + 3.0;
        double conectoresEsperados = (5.0 - (-28.0) - perdaSemConectores)
                / Constantes.PERDA_POR_CONECTOR_DB;
        assertEquals(conectoresEsperados, lb.getResultado(), DELTA);
        assertTrue(lb.getResultado() > 0);
    }

    @Test
    @DisplayName("Calcular numero maximo de fusoes")
    void calcularFusoes() {
        LinkBudget lb = new LinkBudget(
                5.0, -28.0, 5.0, 16, 2, null, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        assertEquals(LinkBudget.Variavel.FUSOES, lb.getVariavelFaltante());
        double perdaSemFusoes = 0.35 * 5.0
                + Constantes.atenuacaoSplitter(16)
                + 2 * Constantes.PERDA_POR_CONECTOR_DB
                + 3.0;
        double fusoesEsperadas = (5.0 - (-28.0) - perdaSemFusoes)
                / Constantes.PERDA_POR_FUSAO_DB;
        assertEquals(fusoesEsperadas, lb.getResultado(), DELTA);
        assertTrue(lb.getResultado() > 0);
    }

    @Test
    @DisplayName("Calcular margem de seguranca disponivel")
    void calcularMargem() {
        LinkBudget lb = new LinkBudget(
                5.0, -24.0, 5.0, 8, 2, 3, null,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        assertEquals(LinkBudget.Variavel.MARGEM, lb.getVariavelFaltante());
        double atenuacao = 0.35 * 5.0
                + Constantes.atenuacaoSplitter(8)
                + 2 * Constantes.PERDA_POR_CONECTOR_DB
                + 3 * Constantes.PERDA_POR_FUSAO_DB;
        double margemEsperada = 5.0 - (-24.0) - atenuacao;
        assertEquals(margemEsperada, lb.getResultado(), DELTA);
    }

    @Test
    @DisplayName("Erro: nenhuma variavel faltante (todos preenchidos)")
    void erroNenhumaFaltante() {
        LinkBudget lb = new LinkBudget(
                5.0, -28.0, 10.0, 16, 2, 3, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, lb::calcular);
        assertTrue(ex.getMessage().contains("Nenhuma"));
    }

    @Test
    @DisplayName("Erro: multiplas variaveis faltantes")
    void erroMultiplasFaltantes() {
        LinkBudget lb = new LinkBudget(
                null, null, 10.0, 16, 2, 3, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, lb::calcular);
        assertTrue(ex.getMessage().contains("Múltiplas"));
        assertTrue(ex.getMessage().contains("2"));
    }

    @Test
    @DisplayName("Erro: distancia calculada negativa")
    void erroDistanciaNegativa() {
        LinkBudget lb = new LinkBudget(
                1.0, -10.0, null, 64, 20, 10, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, lb::calcular);
        assertTrue(ex.getMessage().contains("Distância calculada negativa"));
    }

    @Test
    @DisplayName("Erro: splitter calculado negativo")
    void erroSplitterNegativo() {
        LinkBudget lb = new LinkBudget(
                -5.0, 0.0, 30.0, null, 2, 3, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, lb::calcular);
        assertTrue(ex.getMessage().contains("splitter"));
    }

    @Test
    @DisplayName("Caso de borda: distancia zero")
    void distanciaZero() {
        LinkBudget lb = new LinkBudget(
                5.0, null, 0.0, 4, 0, 0, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        double perda = Constantes.atenuacaoSplitter(4) + 3.0;
        double pRxEsperado = 5.0 - perda;
        assertEquals(pRxEsperado, lb.getResultado(), DELTA);
    }

    @Test
    @DisplayName("Caso de borda: sem conectores e sem fusoes")
    void semConectoresEFusoes() {
        LinkBudget lb = new LinkBudget(
                3.0, null, 10.0, 2, 0, 0, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        double perda = 0.35 * 10.0 + Constantes.atenuacaoSplitter(2) + 3.0;
        double pRxEsperado = 3.0 - perda;
        assertEquals(pRxEsperado, lb.getResultado(), DELTA);
    }

    @Test
    @DisplayName("Downstream vs Upstream devem ter resultados diferentes")
    void downstreamVsUpstream() {
        LinkBudget lbDown = new LinkBudget(
                5.0, null, 20.0, 8, 2, 4, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        LinkBudget lbUp = new LinkBudget(
                5.0, null, 20.0, 8, 2, 4, 3.0,
                ComprimentoOnda.UPSTREAM_1310
        );

        lbDown.calcular();
        lbUp.calcular();

        assertNotEquals(lbDown.getResultado(), lbUp.getResultado());
        assertTrue(lbUp.getResultado() < lbDown.getResultado(),
                "Upstream deve ter P_rx menor que downstream (maior atenuacao)");
    }

    @Test
    @DisplayName("Splitter 1:1 (sem divisao) deve ter atenuacao zero")
    void splitterUmParaUm() {
        LinkBudget lb = new LinkBudget(
                5.0, null, 5.0, 1, 0, 0, 0.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        double perda = 0.35 * 5.0 + 0.0;
        double pRxEsperado = 5.0 - perda;
        assertEquals(pRxEsperado, lb.getResultado(), DELTA);
        assertEquals(0.0, Constantes.atenuacaoSplitter(1), DELTA);
    }

    @Test
    @DisplayName("Metodo calcularAtenuacaoTotal deve retornar todas as perdas somadas")
    void calcularAtenuacaoTotal() {
        LinkBudget lb = new LinkBudget(
                5.0, -28.0, 10.0, 16, 2, 4, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        double atenuacaoTotal = lb.calcularAtenuacaoTotal();

        double esperada = 0.35 * 10.0
                + Constantes.atenuacaoSplitter(16)
                + 2 * Constantes.PERDA_POR_CONECTOR_DB
                + 4 * Constantes.PERDA_POR_FUSAO_DB
                + 3.0;
        assertEquals(esperada, atenuacaoTotal, DELTA);
    }

    @Test
    @DisplayName("Margem pode ser negativa (enlace inviavel)")
    void margemNegativa() {
        LinkBudget lb = new LinkBudget(
                1.0, -27.0, 60.0, 64, 4, 8, null,
                ComprimentoOnda.UPSTREAM_1310
        );
        lb.calcular();

        assertTrue(lb.getResultado() < 0,
                "Margem deve ser negativa quando enlace e inviavel, mas foi: " + lb.getResultado());
    }

    @Test
    @DisplayName("Conectores podem ser fracionarios (valor maximo teorico)")
    void conectoresFracionarios() {
        LinkBudget lb = new LinkBudget(
                5.0, -28.0, 5.0, 4, null, 0, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();

        assertTrue(lb.getResultado() > 0);
    }

    @Test
    @DisplayName("Getter de comprimento de onda")
    void getterComprimentoOnda() {
        LinkBudget lb = new LinkBudget(
                5.0, -28.0, 10.0, 16, 2, 3, 3.0,
                ComprimentoOnda.UPSTREAM_1310
        );
        assertEquals(ComprimentoOnda.UPSTREAM_1310, lb.getComprimentoOnda());
        assertEquals(1310, lb.getComprimentoOnda().getComprimentoNm());
        assertEquals(0.40, lb.getComprimentoOnda().getAtenuacaoDbPorKm(), DELTA);
    }

    @Test
    @DisplayName("Verificar atribuicao correta dos valores no construtor")
    void atributosConstrutor() {
        LinkBudget lb = new LinkBudget(
                5.0, -28.0, 10.0, 16, 2, 3, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        assertEquals(5.0, lb.getpTx());
        assertEquals(-28.0, lb.getpRx());
        assertEquals(10.0, lb.getDistancia());
        assertEquals(16, lb.getSplitRatio());
        assertEquals(2, lb.getConectores());
        assertEquals(3, lb.getFusoes());
        assertEquals(3.0, lb.getMargem());
    }
}
