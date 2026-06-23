package br.edu.gpon.model;

import br.edu.gpon.util.Constantes;

public enum ClasseGPON {

    B_PLUS("B+",
            Constantes.ATENUACAO_MAXIMA_B_PLUS,
            Constantes.P_TX_OLT_B_PLUS_MIN, Constantes.P_TX_OLT_B_PLUS_MAX,
            Constantes.P_TX_ONU_B_PLUS_MIN, Constantes.P_TX_ONU_B_PLUS_MAX,
            Constantes.SENSIBILIDADE_B_PLUS),

    C_PLUS("C+",
            Constantes.ATENUACAO_MAXIMA_C_PLUS,
            Constantes.P_TX_OLT_C_PLUS_MIN, Constantes.P_TX_OLT_C_PLUS_MAX,
            Constantes.P_TX_ONU_C_PLUS_MIN, Constantes.P_TX_ONU_C_PLUS_MAX,
            Constantes.SENSIBILIDADE_C_PLUS),

    C_PLUS_PLUS("C++",
            Constantes.ATENUACAO_MAXIMA_C_PLUS_PLUS,
            Constantes.P_TX_OLT_C_PLUS_PLUS_MIN, Constantes.P_TX_OLT_C_PLUS_PLUS_MAX,
            Constantes.P_TX_ONU_C_PLUS_PLUS_MIN, Constantes.P_TX_ONU_C_PLUS_PLUS_MAX,
            Constantes.SENSIBILIDADE_C_PLUS_PLUS);

    private final String rotulo;
    private final double atenuacaoMaxima;
    private final double pTxOltMin;
    private final double pTxOltMax;
    private final double pTxOnuMin;
    private final double pTxOnuMax;
    private final double sensibilidade;

    ClasseGPON(String rotulo, double atenuacaoMaxima,
               double pTxOltMin, double pTxOltMax,
               double pTxOnuMin, double pTxOnuMax,
               double sensibilidade) {
        this.rotulo = rotulo;
        this.atenuacaoMaxima = atenuacaoMaxima;
        this.pTxOltMin = pTxOltMin;
        this.pTxOltMax = pTxOltMax;
        this.pTxOnuMin = pTxOnuMin;
        this.pTxOnuMax = pTxOnuMax;
        this.sensibilidade = sensibilidade;
    }

    public String getRotulo() {
        return rotulo;
    }

    public double getAtenuacaoMaxima() {
        return atenuacaoMaxima;
    }

    public double getpTxOltMin() {
        return pTxOltMin;
    }

    public double getpTxOltMax() {
        return pTxOltMax;
    }

    public double getpTxOnuMin() {
        return pTxOnuMin;
    }

    public double getpTxOnuMax() {
        return pTxOnuMax;
    }

    public double getSensibilidade() {
        return sensibilidade;
    }

    public double getPTxMin(ComprimentoOnda direcao) {
        return direcao == ComprimentoOnda.DOWNSTREAM_1490 ? pTxOltMin : pTxOnuMin;
    }

    public double getPTxMax(ComprimentoOnda direcao) {
        return direcao == ComprimentoOnda.DOWNSTREAM_1490 ? pTxOltMax : pTxOnuMax;
    }

    @Override
    public String toString() {
        return "Classe " + rotulo;
    }
}
