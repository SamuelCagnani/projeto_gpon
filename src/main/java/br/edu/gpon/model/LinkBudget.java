package br.edu.gpon.model;

import br.edu.gpon.util.Constantes;

public class LinkBudget {

    public enum Variavel {
        P_TX("Potência de Transmissão", "dBm"),
        P_RX("Potência Recebida", "dBm"),
        DISTANCIA("Distância", "km"),
        SPLITTER("Splitter", "dB"),
        CONECTORES("Conectores", "un"),
        FUSOES("Fusões", "un"),
        MARGEM("Margem de Segurança", "dB");

        private final String descricao;
        private final String unidade;

        Variavel(String descricao, String unidade) {
            this.descricao = descricao;
            this.unidade = unidade;
        }

        public String getDescricao() {
            return descricao;
        }

        public String getUnidade() {
            return unidade;
        }
    }

    private Double pTx;
    private Double pRx;
    private Double distancia;
    private Integer splitRatio;
    private Integer conectores;
    private Integer fusoes;
    private Double margem;
    private final ComprimentoOnda comprimentoOnda;

    private Variavel variavelFaltante;
    private Double resultado;

    public LinkBudget(Double pTx, Double pRx, Double distancia, Integer splitRatio,
                      Integer conectores, Integer fusoes, Double margem,
                      ComprimentoOnda comprimentoOnda) {
        this.pTx = pTx;
        this.pRx = pRx;
        this.distancia = distancia;
        this.splitRatio = splitRatio;
        this.conectores = conectores;
        this.fusoes = fusoes;
        this.margem = margem;
        this.comprimentoOnda = comprimentoOnda;
    }

    public void calcular() {
        validarCampos();
        detectarVariavelFaltante();
        executarCalculo();
    }

    private void validarCampos() {
        int faltantes = 0;

        if (pTx == null) faltantes++;
        if (pRx == null) faltantes++;
        if (distancia == null) faltantes++;
        if (splitRatio == null) faltantes++;
        if (conectores == null) faltantes++;
        if (fusoes == null) faltantes++;
        if (margem == null) faltantes++;

        if (faltantes == 0) {
            throw new IllegalArgumentException(
                    "Nenhuma variável faltante. Deixe exatamente 1 campo vazio para calcular.");
        }
        if (faltantes > 1) {
            throw new IllegalArgumentException(
                    "Múltiplas variáveis faltantes (" + faltantes + "). Deixe exatamente 1 campo vazio.");
        }
    }

    private void detectarVariavelFaltante() {
        if (pTx == null) variavelFaltante = Variavel.P_TX;
        else if (pRx == null) variavelFaltante = Variavel.P_RX;
        else if (distancia == null) variavelFaltante = Variavel.DISTANCIA;
        else if (splitRatio == null) variavelFaltante = Variavel.SPLITTER;
        else if (conectores == null) variavelFaltante = Variavel.CONECTORES;
        else if (fusoes == null) variavelFaltante = Variavel.FUSOES;
        else variavelFaltante = Variavel.MARGEM;
    }

    private void executarCalculo() {
        switch (variavelFaltante) {
            case P_TX -> calcularPTx();
            case P_RX -> calcularPRx();
            case DISTANCIA -> calcularDistancia();
            case SPLITTER -> calcularSplitter();
            case CONECTORES -> calcularConectores();
            case FUSOES -> calcularFusoes();
            case MARGEM -> calcularMargem();
        }
    }

    private void calcularPTx() {
        double atenuacao = calcularAtenuacaoTotal();
        resultado = pRx + atenuacao;
    }

    private void calcularPRx() {
        double atenuacao = calcularAtenuacaoTotal();
        resultado = pTx - atenuacao;
    }

    private void calcularDistancia() {
        double perdaFixa = Constantes.atenuacaoSplitter(splitRatio)
                + conectores * Constantes.PERDA_POR_CONECTOR_DB
                + fusoes * Constantes.PERDA_POR_FUSAO_DB
                + margem;
        double saldoPotencia = pTx - pRx - perdaFixa;
        resultado = saldoPotencia / comprimentoOnda.getAtenuacaoDbPorKm();

        if (resultado < 0) {
            throw new IllegalArgumentException(
                    "Distância calculada negativa (" + String.format("%.2f", resultado)
                    + " km). Verifique os valores fornecidos: a potência disponível é insuficiente.");
        }
    }

    private void calcularSplitter() {
        double perdaSemSplitter = comprimentoOnda.getAtenuacaoDbPorKm() * distancia
                + conectores * Constantes.PERDA_POR_CONECTOR_DB
                + fusoes * Constantes.PERDA_POR_FUSAO_DB
                + margem;
        double atenuacaoSplitter = pTx - pRx - perdaSemSplitter;
        resultado = atenuacaoSplitter;

        if (atenuacaoSplitter < 0) {
            throw new IllegalArgumentException(
                    "Atenuação do splitter calculada negativa (" + String.format("%.2f", atenuacaoSplitter)
                    + " dB). Verifique os valores fornecidos.");
        }
    }

    private void calcularConectores() {
        double perdaSemConectores = comprimentoOnda.getAtenuacaoDbPorKm() * distancia
                + Constantes.atenuacaoSplitter(splitRatio)
                + fusoes * Constantes.PERDA_POR_FUSAO_DB
                + margem;
        double saldoPotencia = pTx - pRx - perdaSemConectores;
        resultado = saldoPotencia / Constantes.PERDA_POR_CONECTOR_DB;

        if (resultado < 0) {
            throw new IllegalArgumentException(
                    "Número de conectores calculado negativo (" + String.format("%.2f", resultado)
                    + "). Verifique os valores fornecidos.");
        }
    }

    private void calcularFusoes() {
        double perdaSemFusoes = comprimentoOnda.getAtenuacaoDbPorKm() * distancia
                + Constantes.atenuacaoSplitter(splitRatio)
                + conectores * Constantes.PERDA_POR_CONECTOR_DB
                + margem;
        double saldoPotencia = pTx - pRx - perdaSemFusoes;
        resultado = saldoPotencia / Constantes.PERDA_POR_FUSAO_DB;

        if (resultado < 0) {
            throw new IllegalArgumentException(
                    "Número de fusões calculado negativo (" + String.format("%.2f", resultado)
                    + "). Verifique os valores fornecidos.");
        }
    }

    private void calcularMargem() {
        double atenuacao = comprimentoOnda.getAtenuacaoDbPorKm() * distancia
                + Constantes.atenuacaoSplitter(splitRatio)
                + conectores * Constantes.PERDA_POR_CONECTOR_DB
                + fusoes * Constantes.PERDA_POR_FUSAO_DB;
        resultado = pTx - pRx - atenuacao;
    }

    public double calcularAtenuacaoTotal() {
        double atenuacaoFibra = comprimentoOnda.getAtenuacaoDbPorKm() * distancia;
        double atenuacaoSplitter = Constantes.atenuacaoSplitter(splitRatio);
        double atenuacaoConectores = conectores * Constantes.PERDA_POR_CONECTOR_DB;
        double atenuacaoFusoes = fusoes * Constantes.PERDA_POR_FUSAO_DB;

        return atenuacaoFibra + atenuacaoSplitter + atenuacaoConectores + atenuacaoFusoes + margem;
    }

    public Variavel getVariavelFaltante() {
        return variavelFaltante;
    }

    public Double getResultado() {
        return resultado;
    }

    public ComprimentoOnda getComprimentoOnda() {
        return comprimentoOnda;
    }

    public Double getpTx() {
        return pTx;
    }

    public void setpTx(Double pTx) {
        this.pTx = pTx;
    }

    public Double getpRx() {
        return pRx;
    }

    public void setpRx(Double pRx) {
        this.pRx = pRx;
    }

    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia(Double distancia) {
        this.distancia = distancia;
    }

    public Integer getSplitRatio() {
        return splitRatio;
    }

    public void setSplitRatio(Integer splitRatio) {
        this.splitRatio = splitRatio;
    }

    public Integer getConectores() {
        return conectores;
    }

    public void setConectores(Integer conectores) {
        this.conectores = conectores;
    }

    public Integer getFusoes() {
        return fusoes;
    }

    public void setFusoes(Integer fusoes) {
        this.fusoes = fusoes;
    }

    public Double getMargem() {
        return margem;
    }

    public void setMargem(Double margem) {
        this.margem = margem;
    }

    public Double getResultadoEmPotenciaRecebida() {
        if (variavelFaltante == Variavel.P_RX) {
            return resultado;
        }
        if (resultado == null) {
            return null;
        }

        Double pTxCalc = (variavelFaltante == Variavel.P_TX) ? resultado : this.pTx;
        Double pRxCalc = (variavelFaltante == Variavel.P_RX) ? resultado : this.pRx;
        Double distCalc = (variavelFaltante == Variavel.DISTANCIA) ? resultado : this.distancia;
        Integer splitCalc = (variavelFaltante == Variavel.SPLITTER) ? 1 : this.splitRatio;
        Integer conCalc = (variavelFaltante == Variavel.CONECTORES) ? resultado.intValue() : this.conectores;
        Integer fusCalc = (variavelFaltante == Variavel.FUSOES) ? resultado.intValue() : this.fusoes;
        Double margCalc = (variavelFaltante == Variavel.MARGEM) ? resultado : this.margem;

        if (pTxCalc == null || pRxCalc != null
                && distCalc != null && splitCalc != null
                && conCalc != null && fusCalc != null && margCalc != null) {
        }

        if (pTxCalc != null && distCalc != null && splitCalc != null
                && conCalc != null && fusCalc != null && margCalc != null) {
            double atenuacao = comprimentoOnda.getAtenuacaoDbPorKm() * distCalc
                    + Constantes.atenuacaoSplitter(splitCalc)
                    + conCalc * Constantes.PERDA_POR_CONECTOR_DB
                    + fusCalc * Constantes.PERDA_POR_FUSAO_DB
                    + margCalc;
            return pTxCalc - atenuacao;
        }

        return null;
    }
}
