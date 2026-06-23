# Passo a Passo — Como Rodar a Calculadora GPON

## Pré-requisitos

- **JDK 21+** (já instalado)
- **Maven** (caminho abaixo)

O Maven está disponível via IntelliJ IDEA em:

```
/home/samuel/.local/share/JetBrains/Toolbox/apps/intellij-idea/plugins/maven/lib/maven3/bin/mvn
```

Crie um alias para facilitar:

```bash
alias mvn='/home/samuel/.local/share/JetBrains/Toolbox/apps/intellij-idea/plugins/maven/lib/maven3/bin/mvn'
```

## 1. Compilar

```bash
mvn clean compile
```

## 2. Executar os testes (54 testes)

```bash
mvn test
```

Saída esperada:

```
Tests run: 54, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 3. Abrir a interface gráfica

```bash
mvn javafx:run
```

A janela da calculadora será aberta (tema escuro, 720×680).

## Alternativa: via IntelliJ IDEA

1. Abra a pasta do projeto no IntelliJ
2. Aguarde o Maven baixar as dependências
3. Clique com botão direito em `src/main/java/br/edu/gpon/MainApp.java`
4. Selecione **Run 'MainApp.main()'**
