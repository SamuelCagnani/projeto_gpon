package br.edu.gpon.controller;

import br.edu.gpon.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.UnaryOperator;

public class MainController {

    @FXML private ToggleButton btnDownstream;
    @FXML private ToggleButton btnUpstream;
    @FXML private ComboBox<ClasseGPON> cmbClasseGPON;
    @FXML private TextField fieldPTx;
    @FXML private TextField fieldPRx;
    @FXML private TextField fieldDistancia;
    @FXML private ComboBox<String> cmbSplitter;
    @FXML private TextField fieldConectores;
    @FXML private TextField fieldFusoes;
    @FXML private TextField fieldMargem;
    @FXML private Button btnCalcular;
    @FXML private Button btnLimpar;
    @FXML private VBox boxResultado;
    @FXML private Label labelVariavel;
    @FXML private Label labelValor;
    @FXML private Label labelAtenuacao;
    @FXML private ListView<String> listAlertas;

    private static final Background ERROR_BG = new Background(
            new BackgroundFill(Color.web("#f38ba8", 0.15), new CornerRadii(4), Insets.EMPTY));
    private static final Background NORMAL_BG = null;

    @FXML
    public void initialize() {
        configurarGrupoDirecao();
        configurarComboClasses();
        configurarComboSplitter();
        configurarFormatadores();
        configurarEstiloListaAlertas();
    }

    private void configurarGrupoDirecao() {
        ToggleGroup grupo = new ToggleGroup();
        btnDownstream.setToggleGroup(grupo);
        btnUpstream.setToggleGroup(grupo);
        btnDownstream.setUserData(ComprimentoOnda.DOWNSTREAM_1490);
        btnUpstream.setUserData(ComprimentoOnda.UPSTREAM_1310);
        btnDownstream.setSelected(true);
    }

    private void configurarComboClasses() {
        cmbClasseGPON.setItems(FXCollections.observableArrayList(ClasseGPON.values()));
        cmbClasseGPON.setValue(ClasseGPON.B_PLUS);
        cmbClasseGPON.setConverter(new StringConverter<>() {
            @Override
            public String toString(ClasseGPON c) {
                return c == null ? "" : c.toString();
            }

            @Override
            public ClasseGPON fromString(String s) {
                return null;
            }
        });
    }

    private void configurarComboSplitter() {
        cmbSplitter.setItems(FXCollections.observableArrayList(
                "", "1:1", "1:2", "1:4", "1:8", "1:16", "1:32", "1:64"
        ));
        cmbSplitter.setValue("1:8");
    }

    private void configurarFormatadores() {
        fieldPTx.setTextFormatter(criarFormatadorDecimal());
        fieldPRx.setTextFormatter(criarFormatadorDecimal());
        fieldDistancia.setTextFormatter(criarFormatadorDecimal());
        fieldConectores.setTextFormatter(criarFormatadorInteiro());
        fieldFusoes.setTextFormatter(criarFormatadorInteiro());
        fieldMargem.setTextFormatter(criarFormatadorDecimal());
    }

    private TextFormatter<String> criarFormatadorDecimal() {
        UnaryOperator<TextFormatter.Change> filtro = change -> {
            String novo = change.getControlNewText();
            if (novo.isEmpty()) return change;
            if (novo.equals("-")) return change;
            if (novo.matches("^-?\\d*\\.?\\d*$")) return change;
            return null;
        };
        return new TextFormatter<>(filtro);
    }

    private TextFormatter<String> criarFormatadorInteiro() {
        UnaryOperator<TextFormatter.Change> filtro = change -> {
            String novo = change.getControlNewText();
            if (novo.isEmpty()) return change;
            if (novo.matches("^\\d+$")) return change;
            return null;
        };
        return new TextFormatter<>(filtro);
    }

    private ComprimentoOnda getDirecaoSelecionada() {
        Toggle selecionado = ((ToggleGroup) btnDownstream.getToggleGroup()).getSelectedToggle();
        return (ComprimentoOnda) selecionado.getUserData();
    }

    @FXML
    private void onCalcular() {
        limparResultados();
        limparEstilos();

        try {
            int faltantes = contarCamposVazios();
            if (faltantes == 0) {
                mostrarErro("Nenhuma variável faltante. Deixe exatamente 1 campo vazio para calcular.");
                return;
            }
            if (faltantes > 1) {
                mostrarErro("Múltiplas variáveis faltantes (" + faltantes
                        + "). Deixe exatamente 1 campo vazio.");
                return;
            }

            Integer splitRatio = parseSplitRatio(cmbSplitter.getValue());
            if (splitRatio == null && !isCampoVazio(cmbSplitter)) {
                mostrarErro("Selecione um splitter válido.");
                return;
            }

            LinkBudget lb = new LinkBudget(
                    parseDouble(fieldPTx),
                    parseDouble(fieldPRx),
                    parseDouble(fieldDistancia),
                    splitRatio,
                    parseInteger(fieldConectores),
                    parseInteger(fieldFusoes),
                    parseDouble(fieldMargem),
                    getDirecaoSelecionada()
            );

            lb.calcular();

            preencherCampoFaltante(lb);
            exibirResultado(lb);
            validarEExibirAlertas(lb);

        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
        } catch (Exception e) {
            mostrarErro("Erro inesperado: " + e.getMessage());
        }
    }

    private int contarCamposVazios() {
        int count = 0;
        if (isCampoVazio(fieldPTx)) count++;
        if (isCampoVazio(fieldPRx)) count++;
        if (isCampoVazio(fieldDistancia)) count++;
        if (isCampoVazio(cmbSplitter)) count++;
        if (isCampoVazio(fieldConectores)) count++;
        if (isCampoVazio(fieldFusoes)) count++;
        if (isCampoVazio(fieldMargem)) count++;
        return count;
    }

    private boolean isCampoVazio(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private boolean isCampoVazio(ComboBox<?> combo) {
        return combo.getValue() == null || (combo.getValue() instanceof String s && s.trim().isEmpty());
    }

    private Double parseDouble(TextField field) {
        if (isCampoVazio(field)) return null;
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(TextField field) {
        if (isCampoVazio(field)) return null;
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseSplitRatio(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.replace("1:", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void preencherCampoFaltante(LinkBudget lb) {
        LinkBudget.Variavel v = lb.getVariavelFaltante();
        Double resultado = lb.getResultado();

        switch (v) {
            case P_TX -> fieldPTx.setText(formatarDecimal(resultado));
            case P_RX -> fieldPRx.setText(formatarDecimal(resultado));
            case DISTANCIA -> fieldDistancia.setText(formatarDecimal(resultado));
            case SPLITTER -> {
                cmbSplitter.setValue("1:— (" + formatarDecimal(resultado) + " dB)");
            }
            case CONECTORES -> fieldConectores.setText(String.valueOf((int) Math.round(resultado)));
            case FUSOES -> fieldFusoes.setText(String.valueOf((int) Math.round(resultado)));
            case MARGEM -> fieldMargem.setText(formatarDecimal(resultado));
        }
    }

    private String formatarDecimal(Double valor) {
        return String.format("%.2f", valor);
    }

    private void exibirResultado(LinkBudget lb) {
        labelVariavel.setText(lb.getVariavelFaltante().getDescricao());
        labelValor.setText(formatarDecimal(lb.getResultado()) + " " + lb.getVariavelFaltante().getUnidade());

        String direcao = lb.getComprimentoOnda() == ComprimentoOnda.DOWNSTREAM_1490 ? "Downstream" : "Upstream";
        labelAtenuacao.setText("Atenuação total do enlace: " + formatarDecimal(lb.calcularAtenuacaoTotal())
                + " dB | " + direcao + " | " + lb.getComprimentoOnda());
    }

    private void validarEExibirAlertas(LinkBudget lb) {
        ClasseGPON classe = cmbClasseGPON.getValue();
        if (classe == null) classe = ClasseGPON.B_PLUS;

        List<Alerta> alertas = Validador.validar(lb, classe);

        listAlertas.getItems().clear();
        for (Alerta a : alertas) {
            listAlertas.getItems().add(a.toString());
        }
    }

    @FXML
    private void onLimpar() {
        fieldPTx.clear();
        fieldPRx.clear();
        fieldDistancia.clear();
        cmbSplitter.setValue("1:8");
        fieldConectores.clear();
        fieldFusoes.clear();
        fieldMargem.clear();

        labelVariavel.setText("—");
        labelValor.setText("—");
        labelAtenuacao.setText("");
        listAlertas.getItems().clear();

        limparEstilos();
    }

    private void limparResultados() {
        labelVariavel.setText("—");
        labelValor.setText("—");
        labelAtenuacao.setText("");
        listAlertas.getItems().clear();
    }

    private void limparEstilos() {
        fieldPTx.setBackground(NORMAL_BG);
        fieldPRx.setBackground(NORMAL_BG);
        fieldDistancia.setBackground(NORMAL_BG);
        fieldConectores.setBackground(NORMAL_BG);
        fieldFusoes.setBackground(NORMAL_BG);
        fieldMargem.setBackground(NORMAL_BG);
    }

    private void mostrarErro(String mensagem) {
        listAlertas.getItems().clear();
        listAlertas.getItems().add("[ERRO] " + mensagem);
    }

    private void configurarEstiloListaAlertas() {
        listAlertas.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTextFill(null);
                } else {
                    setText(item);
                    if (item.startsWith("[ERRO]")) {
                        setTextFill(Color.web("#f38ba8"));
                    } else if (item.startsWith("[AVISO]")) {
                        setTextFill(Color.web("#f9e2af"));
                    } else if (item.startsWith("[INFO]")) {
                        setTextFill(Color.web("#a6e3a1"));
                    }
                }
            }
        });
    }
}
