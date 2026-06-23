package br.edu.gpon.model;

import br.edu.gpon.util.Constantes;

public class Equipamento {

    private final String nome;
    private final TipoEquipamento tipo;
    private final Double potenciaTxMin;
    private final Double potenciaTxMax;
    private final Double sensibilidade;
    private final Double perdaNominal;

    public Equipamento(String nome, TipoEquipamento tipo, Double potenciaTxMin,
                       Double potenciaTxMax, Double sensibilidade, Double perdaNominal) {
        this.nome = nome;
        this.tipo = tipo;
        this.potenciaTxMin = potenciaTxMin;
        this.potenciaTxMax = potenciaTxMax;
        this.sensibilidade = sensibilidade;
        this.perdaNominal = perdaNominal;
    }

    public static Equipamento olt(double potenciaTxMin, double potenciaTxMax) {
        return new Equipamento("OLT", TipoEquipamento.OLT,
                potenciaTxMin, potenciaTxMax, null, null);
    }

    public static Equipamento onu(double sensibilidade) {
        return new Equipamento("ONU", TipoEquipamento.ONU,
                null, null, sensibilidade, null);
    }

    public static Equipamento fibra() {
        return new Equipamento("Fibra G.652", TipoEquipamento.FIBRA,
                null, null, null, null);
    }

    public static Equipamento splitter(int splitRatio) {
        double atenuacao = Constantes.atenuacaoSplitter(splitRatio);
        return new Equipamento("Splitter 1:" + splitRatio, TipoEquipamento.SPLITTER,
                null, null, null, atenuacao);
    }

    public static Equipamento conector() {
        return new Equipamento("Conector", TipoEquipamento.CONECTOR,
                null, null, null, Constantes.PERDA_POR_CONECTOR_DB);
    }

    public static Equipamento fusao() {
        return new Equipamento("Fusão", TipoEquipamento.FUSAO,
                null, null, null, Constantes.PERDA_POR_FUSAO_DB);
    }

    public String getNome() {
        return nome;
    }

    public TipoEquipamento getTipo() {
        return tipo;
    }

    public Double getPotenciaTxMin() {
        return potenciaTxMin;
    }

    public Double getPotenciaTxMax() {
        return potenciaTxMax;
    }

    public Double getSensibilidade() {
        return sensibilidade;
    }

    public Double getPerdaNominal() {
        return perdaNominal;
    }

    @Override
    public String toString() {
        return nome;
    }
}
