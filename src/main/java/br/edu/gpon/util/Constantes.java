package br.edu.gpon.util;

import java.util.Map;

public final class Constantes {

    private Constantes() {
    }

    public static final double PERDA_POR_CONECTOR_DB = 0.5;
    public static final double PERDA_POR_FUSAO_DB = 0.1;
    public static final double MARGEM_PADRAO_DB = 3.0;
    public static final double DISTANCIA_MAXIMA_KM = 60.0;
    public static final double DISTANCIA_RECOMENDADA_KM = 20.0;

    public static final Map<Integer, Double> SPLITTER_ATENUACAO = Map.of(
            1, 0.0,
            2, 3.0,
            4, 6.0,
            8, 9.0,
            16, 12.0,
            32, 15.0,
            64, 18.0
    );

    public static double atenuacaoSplitter(int splitRatio) {
        return 10.0 * Math.log10(splitRatio);
    }

    public static final double ATENUACAO_FIBRA_1490NM = 0.35;
    public static final double ATENUACAO_FIBRA_1310NM = 0.40;

    public static final double P_TX_OLT_B_PLUS_MIN = 1.5;
    public static final double P_TX_OLT_B_PLUS_MAX = 5.0;
    public static final double P_TX_OLT_C_PLUS_MIN = 3.0;
    public static final double P_TX_OLT_C_PLUS_MAX = 7.0;
    public static final double P_TX_OLT_C_PLUS_PLUS_MIN = 6.0;
    public static final double P_TX_OLT_C_PLUS_PLUS_MAX = 10.0;

    public static final double P_TX_ONU_B_PLUS_MIN = 0.5;
    public static final double P_TX_ONU_B_PLUS_MAX = 5.0;
    public static final double P_TX_ONU_C_PLUS_MIN = 0.5;
    public static final double P_TX_ONU_C_PLUS_MAX = 5.0;
    public static final double P_TX_ONU_C_PLUS_PLUS_MIN = 0.5;
    public static final double P_TX_ONU_C_PLUS_PLUS_MAX = 7.0;

    public static final double SENSIBILIDADE_B_PLUS = -28.0;
    public static final double SENSIBILIDADE_C_PLUS = -31.0;
    public static final double SENSIBILIDADE_C_PLUS_PLUS = -34.0;

    public static final double ATENUACAO_MAXIMA_B_PLUS = 28.0;
    public static final double ATENUACAO_MAXIMA_C_PLUS = 32.0;
    public static final double ATENUACAO_MAXIMA_C_PLUS_PLUS = 35.0;
}
