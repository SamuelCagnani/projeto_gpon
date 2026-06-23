package br.edu.gpon.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorTest {

    // =========================================================================
    // Cenários válidos
    // =========================================================================

    @Test
    @DisplayName("Cenario completamente dentro dos limites - sem erros")
    void cenarioValido() {
        LinkBudget lb = new LinkBudget(
                3.0, -26.0, 5.0, 8, 2, 3, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );

        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);

        long erros = alertas.stream().filter(a -> a.getTipo() == TipoAlerta.ERRO).count();
        assertEquals(0, erros, "Nao deve haver erros: " + alertas);
    }

    @Test
    @DisplayName("Classe C++ suporta valores maiores")
    void classeCMaisMais() {
        LinkBudget lb = new LinkBudget(
                8.0, -32.0, 8.0, 16, 2, 3, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );

        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.C_PLUS_PLUS);
        long erros = alertas.stream().filter(a -> a.getTipo() == TipoAlerta.ERRO).count();
        assertEquals(0, erros, "C++ deve aceitar P_tx=8dBm: " + alertas);
    }

    // =========================================================================
    // Validação de P_tx
    // =========================================================================

    @Test
    @DisplayName("P_tx acima do maximo da classe -> ERRO")
    void pTxAcimaMaximo() {
        LinkBudget lb = new LinkBudget(
                6.0, -28.0, 5.0, 8, 2, 3, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.ERRO && a.getCampoAfetado().equals("P_tx")));
    }

    @Test
    @DisplayName("P_tx abaixo do minimo da classe -> ERRO")
    void pTxAbaixoMinimo() {
        LinkBudget lb = new LinkBudget(
                0.0, -28.0, 5.0, 8, 2, 3, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.ERRO && a.getCampoAfetado().equals("P_tx")));
    }

    @Test
    @DisplayName("Upstream tem range de P_tx diferente (ONU)")
    void pTxUpstreamRangeOnu() {
        LinkBudget lb = new LinkBudget(
                6.0, -28.0, 5.0, 8, 2, 3, 3.0,
                ComprimentoOnda.UPSTREAM_1310
        );
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.ERRO && a.getCampoAfetado().equals("P_tx")),
                "ONU Classe B+ max 5.0 dBm, 6.0 deve gerar erro");
    }

    // =========================================================================
    // Validação de P_rx
    // =========================================================================

    @Test
    @DisplayName("P_rx abaixo da sensibilidade -> ERRO")
    void pRxAbaixoSensibilidade() {
        LinkBudget lb = new LinkBudget(
                3.0, -30.0, 10.0, 32, 4, 6, 3.0,
                ComprimentoOnda.UPSTREAM_1310
        );
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.ERRO && a.getCampoAfetado().equals("P_rx")),
                "P_rx=-30 abaixo da sensibilidade -28: " + alertas);
    }

    @Test
    @DisplayName("P_rx proximo da sensibilidade -> AVISO")
    void pRxProximoSensibilidade() {
        LinkBudget lb = new LinkBudget(
                3.0, -27.0, 5.0, 4, 1, 1, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.AVISO && a.getCampoAfetado().equals("P_rx")),
                "P_rx=-27 esta a 1dB da sensibilidade (-28): " + alertas);
    }

    // =========================================================================
    // Validação de distância
    // =========================================================================

    @Test
    @DisplayName("Distancia > 60km -> ERRO")
    void distanciaAcimaMaxima() {
        LinkBudget lb = new LinkBudget(
                3.0, -28.0, 65.0, 2, 0, 0, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.ERRO && a.getCampoAfetado().equals("Distância")));
    }

    @Test
    @DisplayName("Distancia entre 20 e 60km -> AVISO")
    void distanciaAcimaRecomendacao() {
        LinkBudget lb = new LinkBudget(
                3.0, -28.0, 25.0, 2, 0, 0, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.AVISO && a.getCampoAfetado().equals("Distância")));
    }

    // =========================================================================
    // Validação de atenuação total (após calcular)
    // =========================================================================

    @Test
    @DisplayName("Atenuacao total excede limite da classe -> ERRO")
    void atenuacaoAcimaLimite() {
        LinkBudget lb = new LinkBudget(
                3.0, -28.0, 40.0, 64, 4, 8, null,
                ComprimentoOnda.UPSTREAM_1310
        );
        lb.calcular();
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.ERRO && a.getCampoAfetado().equals("Atenuação Total")));
    }

    @Test
    @DisplayName("Atenuacao total > 80% do limite -> AVISO")
    void atenuacaoProximaLimite() {
        LinkBudget lb = new LinkBudget(
                3.0, -20.0, 15.0, 16, 4, 5, null,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.AVISO && a.getCampoAfetado().equals("Atenuação Total")),
                "Alertas: " + alertas);
    }

    // =========================================================================
    // Validação de splitter
    // =========================================================================

    @Test
    @DisplayName("Splitter invalido (1:100) -> ERRO")
    void splitterInvalido() {
        LinkBudget lb = new LinkBudget(
                3.0, -28.0, 5.0, 100, 2, 3, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.ERRO && a.getCampoAfetado().equals("Splitter")));
    }

    @Test
    @DisplayName("Splitters validos nao geram alerta")
    void splittersValidos() {
        for (int ratio : new int[]{2, 4, 8, 16, 32, 64}) {
            LinkBudget lb = new LinkBudget(
                    3.0, -28.0, 5.0, ratio, 2, 3, 3.0,
                    ComprimentoOnda.DOWNSTREAM_1490
            );
            List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
            boolean erroSplitter = alertas.stream()
                    .anyMatch(a -> a.getTipo() == TipoAlerta.ERRO && "Splitter".equals(a.getCampoAfetado()));
            assertFalse(erroSplitter, "Splitter 1:" + ratio + " deve ser valido: " + alertas);
        }
    }

    // =========================================================================
    // Validação de margem
    // =========================================================================

    @Test
    @DisplayName("Margem negativa -> ERRO")
    void margemNegativa() {
        LinkBudget lb = new LinkBudget(
                3.0, -28.0, 5.0, 8, 2, 3, -2.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.ERRO && a.getCampoAfetado().equals("Margem")));
    }

    @Test
    @DisplayName("Margem < 1dB -> AVISO")
    void margemBaixa() {
        LinkBudget lb = new LinkBudget(
                3.0, -28.0, 5.0, 8, 2, 3, 0.5,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.AVISO && a.getCampoAfetado().equals("Margem")));
    }

    @Test
    @DisplayName("Margem > 5dB -> INFO (superdimensionamento)")
    void margemElevada() {
        LinkBudget lb = new LinkBudget(
                3.0, -28.0, 5.0, 8, 2, 3, 6.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a -> a.getTipo() == TipoAlerta.INFO),
                "Deve ter INFO de superdimensionamento: " + alertas);
    }

    // =========================================================================
    // Validação após cálculo (variável faltante resolvida)
    // =========================================================================

    @Test
    @DisplayName("Apos calcular P_rx, verifica sensibilidade")
    void aposCalcularPRx() {
        LinkBudget lb = new LinkBudget(
                1.5, null, 20.0, 64, 5, 8, 3.0,
                ComprimentoOnda.UPSTREAM_1310
        );
        lb.calcular();
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertFalse(alertas.isEmpty());
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.ERRO && a.getCampoAfetado().equals("P_rx")),
                "P_rx calculado deve estar abaixo da sensibilidade: " + alertas);
    }

    @Test
    @DisplayName("Apos calcular P_tx, valida range da classe")
    void aposCalcularPTx() {
        LinkBudget lb = new LinkBudget(
                null, -28.0, 20.0, 64, 4, 8, 3.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.ERRO),
                "Deve haver alertas de erro para o cenario: " + alertas);
    }

    @Test
    @DisplayName("Apos calcular distancia, valida limites")
    void aposCalcularDistancia() {
        LinkBudget lb = new LinkBudget(
                5.0, -28.0, null, 32, 1, 2, 1.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        lb.calcular();
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a ->
                a.getTipo() == TipoAlerta.AVISO && a.getCampoAfetado().equals("Distância")),
                "Distância calculada deve exceder recomendação: " + alertas);
    }

    // =========================================================================
    // Validação de alertas múltiplos simultâneos
    // =========================================================================

    @Test
    @DisplayName("Multiplos problemas geram multiplos alertas")
    void multiplosAlertas() {
        LinkBudget lb = new LinkBudget(
                10.0, -30.0, 70.0, 100, 2, 3, -1.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        long erros = alertas.stream().filter(a -> a.getTipo() == TipoAlerta.ERRO).count();
        assertTrue(erros >= 4, "Deve ter erros de P_tx, P_rx, distancia e splitter: " + alertas);
    }

    // =========================================================================
    // ClasseGPON enum
    // =========================================================================

    @Test
    @DisplayName("ClasseGPON valores corretos")
    void classeGponValores() {
        assertEquals("B+", ClasseGPON.B_PLUS.getRotulo());
        assertEquals(28.0, ClasseGPON.B_PLUS.getAtenuacaoMaxima());
        assertEquals(1.5, ClasseGPON.B_PLUS.getpTxOltMin());
        assertEquals(5.0, ClasseGPON.B_PLUS.getpTxOltMax());
        assertEquals(-28.0, ClasseGPON.B_PLUS.getSensibilidade());

        assertEquals(32.0, ClasseGPON.C_PLUS.getAtenuacaoMaxima());
        assertEquals(35.0, ClasseGPON.C_PLUS_PLUS.getAtenuacaoMaxima());
    }

    @Test
    @DisplayName("ClasseGPON getPTxMin/Max respeita direcao")
    void classeGponDirecao() {
        assertEquals(1.5, ClasseGPON.B_PLUS.getPTxMin(ComprimentoOnda.DOWNSTREAM_1490));
        assertEquals(5.0, ClasseGPON.B_PLUS.getPTxMax(ComprimentoOnda.DOWNSTREAM_1490));
        assertEquals(0.5, ClasseGPON.B_PLUS.getPTxMin(ComprimentoOnda.UPSTREAM_1310));
        assertEquals(5.0, ClasseGPON.B_PLUS.getPTxMax(ComprimentoOnda.UPSTREAM_1310));
    }

    // =========================================================================
    // Alertas sem erros retornam INFO
    // =========================================================================

    @Test
    @DisplayName("Quando nao ha erros, retorna alerta INFO")
    void alertaInfoSemErros() {
        LinkBudget lb = new LinkBudget(
                3.0, -26.0, 3.0, 4, 1, 1, 2.0,
                ComprimentoOnda.DOWNSTREAM_1490
        );
        List<Alerta> alertas = Validador.validar(lb, ClasseGPON.B_PLUS);
        assertTrue(alertas.stream().anyMatch(a -> a.getTipo() == TipoAlerta.INFO),
                "Deve conter INFO de sucesso: " + alertas);
    }
}
