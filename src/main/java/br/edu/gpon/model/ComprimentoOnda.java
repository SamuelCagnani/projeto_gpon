package br.edu.gpon.model;

public enum ComprimentoOnda {

    DOWNSTREAM_1490(1490, 0.35),
    UPSTREAM_1310(1310, 0.40);

    private final int comprimentoNm;
    private final double atenuacaoDbPorKm;

    ComprimentoOnda(int comprimentoNm, double atenuacaoDbPorKm) {
        this.comprimentoNm = comprimentoNm;
        this.atenuacaoDbPorKm = atenuacaoDbPorKm;
    }

    public int getComprimentoNm() {
        return comprimentoNm;
    }

    public double getAtenuacaoDbPorKm() {
        return atenuacaoDbPorKm;
    }

    @Override
    public String toString() {
        return comprimentoNm + " nm (" + atenuacaoDbPorKm + " dB/km)";
    }
}
