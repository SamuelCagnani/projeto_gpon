package br.edu.gpon.model;

import br.edu.gpon.util.Constantes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Validador {

    private Validador() {
    }

    public static List<Alerta> validar(LinkBudget lb, ClasseGPON classe) {
        List<Alerta> alertas = new ArrayList<>();

        validarPTx(lb, classe, alertas);
        validarPotenciaRecebida(lb, classe, alertas);
        validarDistancia(lb, alertas);
        validarAtenuacaoTotal(lb, classe, alertas);
        validarSplitter(lb, alertas);
        validarMargem(lb, alertas);

        if (alertas.isEmpty()) {
            alertas.add(Alerta.info("Todos os parâmetros dentro dos limites esperados."));
        }

        return Collections.unmodifiableList(alertas);
    }

    private static void validarPTx(LinkBudget lb, ClasseGPON classe, List<Alerta> alertas) {
        Double pTx = lb.getpTx();
        if (pTx == null) return;

        double min = classe.getPTxMin(lb.getComprimentoOnda());
        double max = classe.getPTxMax(lb.getComprimentoOnda());

        if (pTx < min) {
            alertas.add(Alerta.erro(
                    String.format("Potência de transmissão %.2f dBm abaixo do mínimo (%.2f dBm) para %s",
                            pTx, min, classe.getRotulo()),
                    "P_tx"));
        } else if (pTx > max) {
            alertas.add(Alerta.erro(
                    String.format("Potência de transmissão %.2f dBm acima do máximo (%.2f dBm) para %s",
                            pTx, max, classe.getRotulo()),
                    "P_tx"));
        }
    }

    private static void validarPotenciaRecebida(LinkBudget lb, ClasseGPON classe, List<Alerta> alertas) {
        Double pRx = lb.getpRx();
        if (pRx == null) return;

        double sensibilidade = classe.getSensibilidade();

        if (pRx < sensibilidade) {
            alertas.add(Alerta.erro(
                    String.format("Potência recebida %.2f dBm abaixo da sensibilidade (%.2f dBm) para classe %s. Enlace inviável.",
                            pRx, sensibilidade, classe.getRotulo()),
                    "P_rx"));
        } else if (pRx < sensibilidade + 2.0) {
            alertas.add(Alerta.aviso(
                    String.format("Potência recebida %.2f dBm próxima da sensibilidade (%.2f dBm). Margem reduzida.",
                            pRx, sensibilidade),
                    "P_rx"));
        }
    }

    private static void validarDistancia(LinkBudget lb, List<Alerta> alertas) {
        Double distancia = lb.getDistancia();
        if (distancia == null) return;

        if (distancia > Constantes.DISTANCIA_MAXIMA_KM) {
            alertas.add(Alerta.erro(
                    String.format("Distância %.2f km excede o limite máximo de %.0f km.",
                            distancia, Constantes.DISTANCIA_MAXIMA_KM),
                    "Distância"));
        } else if (distancia > Constantes.DISTANCIA_RECOMENDADA_KM) {
            alertas.add(Alerta.aviso(
                    String.format("Distância %.2f km excede a recomendação de %.0f km. Requer validação adicional.",
                            distancia, Constantes.DISTANCIA_RECOMENDADA_KM),
                    "Distância"));
        }
        if (distancia < 0) {
            alertas.add(Alerta.erro(
                    "Distância não pode ser negativa.",
                    "Distância"));
        }
    }

    private static void validarAtenuacaoTotal(LinkBudget lb, ClasseGPON classe, List<Alerta> alertas) {
        if (lb.getVariavelFaltante() == null || lb.getResultado() == null) return;

        double atenuacaoTotal = lb.calcularAtenuacaoTotal();

        if (atenuacaoTotal > classe.getAtenuacaoMaxima()) {
            alertas.add(Alerta.erro(
                    String.format("Atenuação total %.2f dB excede o limite de %.0f dB da classe %s.",
                            atenuacaoTotal, classe.getAtenuacaoMaxima(), classe.getRotulo()),
                    "Atenuação Total"));
        } else if (atenuacaoTotal > classe.getAtenuacaoMaxima() * 0.8) {
            alertas.add(Alerta.aviso(
                    String.format("Atenuação total %.2f dB próxima do limite de %.0f dB da classe %s (%.0f%%).",
                            atenuacaoTotal, classe.getAtenuacaoMaxima(), classe.getRotulo(),
                            (atenuacaoTotal / classe.getAtenuacaoMaxima()) * 100),
                    "Atenuação Total"));
        }
    }

    private static void validarSplitter(LinkBudget lb, List<Alerta> alertas) {
        Integer splitRatio = lb.getSplitRatio();
        if (splitRatio == null) return;

        if (!Constantes.SPLITTER_ATENUACAO.containsKey(splitRatio)) {
            alertas.add(Alerta.erro(
                    String.format("Splitter 1:%d não é um valor padrão. Valores válidos: %s.",
                            splitRatio, Constantes.SPLITTER_ATENUACAO.keySet()),
                    "Splitter"));
        }
    }

    private static void validarMargem(LinkBudget lb, List<Alerta> alertas) {
        Double margem = lb.getMargem();
        if (margem == null) return;

        if (margem < 0) {
            alertas.add(Alerta.erro(
                    String.format("Margem de segurança negativa (%.2f dB). Enlace inviável.", margem),
                    "Margem"));
        } else if (margem < 1.0) {
            alertas.add(Alerta.aviso(
                    String.format("Margem de segurança baixa (%.2f dB). Recomendado ≥ 1 dB.", margem),
                    "Margem"));
        } else if (margem > 5.0) {
            alertas.add(Alerta.info(
                    String.format("Margem de segurança elevada (%.2f dB). Possível superdimensionamento.", margem)));
        }
    }
}
