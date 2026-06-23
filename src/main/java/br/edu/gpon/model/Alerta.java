package br.edu.gpon.model;

public class Alerta {

    private final String mensagem;
    private final TipoAlerta tipo;
    private final String campoAfetado;

    public Alerta(String mensagem, TipoAlerta tipo, String campoAfetado) {
        this.mensagem = mensagem;
        this.tipo = tipo;
        this.campoAfetado = campoAfetado;
    }

    public static Alerta info(String mensagem) {
        return new Alerta(mensagem, TipoAlerta.INFO, null);
    }

    public static Alerta aviso(String mensagem, String campoAfetado) {
        return new Alerta(mensagem, TipoAlerta.AVISO, campoAfetado);
    }

    public static Alerta erro(String mensagem, String campoAfetado) {
        return new Alerta(mensagem, TipoAlerta.ERRO, campoAfetado);
    }

    public String getMensagem() {
        return mensagem;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public String getCampoAfetado() {
        return campoAfetado;
    }

    @Override
    public String toString() {
        String prefixo = switch (tipo) {
            case INFO -> "[INFO] ";
            case AVISO -> "[AVISO] ";
            case ERRO -> "[ERRO] ";
        };
        if (campoAfetado != null) {
            return prefixo + campoAfetado + ": " + mensagem;
        }
        return prefixo + mensagem;
    }
}
