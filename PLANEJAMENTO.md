# Planejamento de Sprints — Calculadora GPON Link Budget

**Projeto Interdisciplinar:** Propagação de Ondas Eletromagnéticas + Engenharia de Software
**Equipe:** Dupla
**Stack:** Java 17+ | JavaFX | Maven | JUnit 5

---

## Arquitetura do Projeto (MVC + Domínio)

```
src/main/java/br/edu/gpon/
├── model/
│   ├── Equipamento.java          # Classe de domínio: OLT, ONU, fibra, splitter, conectores
│   ├── LinkBudget.java           # Motor de cálculo: fórmula e isolamento de variável faltante
│   ├── Validador.java            # Regras de validação baseadas na ITU-T G.984
│   ├── Alerta.java               # Mensagem de alerta com severidade (INFO, AVISO, ERRO)
│   ├── TipoAlerta.java           # Enum de severidade
│   ├── ComprimentoOnda.java      # Enum DOWNSTREAM_1490 / UPSTREAM_1310 com α (dB/km)
│   └── TipoEquipamento.java      # Enum OLT, ONU, FIBRA, SPLITTER, CONECTOR, FUSAO
├── controller/
│   └── MainController.java       # Controller JavaFX: liga UI ao modelo (padrão MVC)
├── view/
│   ├── main.fxml                 # Layout da interface gráfica
│   └── styles.css                # Estilização da UI
├── util/
│   └── Constantes.java           # Valores de referência ITU-T G.984, G.652
└── MainApp.java                  # Entry point da aplicação JavaFX
```

### Fórmula Central do Link Budget

```
P_rx = P_tx - (α × d) - A_splitter - (N_con × P_con) - (N_fus × P_fus) - M
```

| Variável | Descrição | Unidade |
|----------|-----------|---------|
| P_rx | Potência recebida | dBm |
| P_tx | Potência transmitida | dBm |
| α | Atenuação da fibra (1490nm: 0.35 dB/km, 1310nm: 0.40 dB/km) | dB/km |
| d | Distância do enlace | km |
| A_splitter | Atenuação do splitter (1:N → 10×log₁₀(N) ≈ 3×log₂(N) dB) | dB |
| N_con | Número de conectores no enlace | un |
| P_con | Perda por conector (~0.5 dB) | dB |
| N_fus | Número de fusões no enlace | un |
| P_fus | Perda por fusão (~0.1 dB) | dB |
| M | Margem de segurança (~3 dB) | dB |

### Tabela de Splitters

| Razão | Atenuação (dB) |
|-------|----------------|
| 1:2   | 3              |
| 1:4   | 6              |
| 1:8   | 9              |
| 1:16  | 12             |
| 1:32  | 15             |
| 1:64  | 18             |

### Classes GPON de Referência (ITU-T G.984)

| Classe | Atenuação Máx. (dB) | P_tx OLT (dBm) | Sensibilidade ONU (dBm) |
|--------|---------------------|----------------|-------------------------|
| B+     | 28                  | +1.5 a +5      | -28                     |
| C+     | 32                  | +3 a +7        | -31                     |
| C++    | 35                  | +6 a +10       | -34                     |

---

## Sprint 1: Fundação — Setup e Modelo de Domínio

**Duração estimada:** 3 dias
**Objetivo:** Projeto compilável, classes de domínio funcionais, cálculo operante.

### Issues

| ID | Título | Descrição | Prioridade | Responsável |
|----|--------|-----------|------------|-------------|
| 1.1 | Configurar projeto Maven com JavaFX | Criar `pom.xml` com JavaFX 17+, JUnit 5, Maven Compiler Plugin. Estrutura de diretórios conforme arquitetura. Garantir `mvn clean compile` funcional | 🔴 Alta | B |
| 1.2 | Implementar `ComprimentoOnda` e `Constantes` | Enum `ComprimentoOnda` (DOWNSTREAM_1490 com α=0.35, UPSTREAM_1310 com α=0.40). Classe `Constantes` com perdas padrão (conector 0.5dB, fusão 0.1dB, margem 3dB) e tabela de splitters | 🟡 Média | A |
| 1.3 | Implementar `Equipamento` | Atributos: nome, tipo (enum TipoEquipamento), potenciaTxMin, potenciaTxMax, sensibilidade, perdaNominal. Métodos fábrica estáticos: `Equipamento.olt()`, `Equipamento.onu()`, `Equipamento.fibra()`, etc. | 🔴 Alta | A |
| 1.4 | Implementar `LinkBudget` | Classe com todos os campos da fórmula. Campo `variavelFaltante` (enum com P_TX, P_RX, DISTANCIA, SPLITTER, CONECTORES, FUSOES, MARGEM). Método `calcular()` que detecta qual campo é nulo, isola algebricamente e retorna o resultado. Suporte a downstream e upstream via `ComprimentoOnda`. Tratar divisão por zero e valores negativos | 🔴 Alta | B |
| 1.5 | Testes unitários — `LinkBudget` | Testar cada variável como faltante. Verificar precisão: cenário manual com splitter 1:8, 5km, downstream → P_rx esperado. Testar edge cases (distância zero, splitter 1:1, todos conectores zero). Testar exceções (duas variáveis faltantes → erro, nenhuma faltante → erro) | 🔴 Alta | A + B |

---

## Sprint 2: Validação e Regras de Negócio

**Duração estimada:** 2 dias
**Objetivo:** Validador funcional com alertas baseados na ITU-T G.984.

### Issues

| ID | Título | Descrição | Prioridade | Responsável |
|----|--------|-----------|------------|-------------|
| 2.1 | Implementar `TipoAlerta` e `Alerta` | Enum `TipoAlerta`: INFO, AVISO, ERRO. Classe `Alerta`: mensagem (String), tipo, campoAfetado (String). Método `toString()` formatado para exibição | 🟡 Média | A |
| 2.2 | Implementar `Validador` | Método `validar(LinkBudget)` que retorna `List<Alerta>`. Validações: P_tx dentro do range da classe GPON (B+/C+/C++), P_rx >= sensibilidade, distância ≤ 20km (alerta) ou ≤ 60km (erro), atenuação total ≤ limite da classe, splitter dentro dos padrões (1:2 a 1:64). Validações distintas para downstream e upstream | 🔴 Alta | A |
| 2.3 | Testes unitários — `Validador` | Cenário válido (0 alertas de erro). P_tx acima do máximo → ERRO. Distância > 60km → ERRO. Distância entre 20 e 60km → AVISO. P_rx abaixo da sensibilidade → ERRO. Splitter inválido (ex: 1:100) → ERRO. Validar classes B+, C+, C++ individualmente | 🔴 Alta | B |

---

## Sprint 3: Interface Gráfica JavaFX

**Duração estimada:** 4 dias
**Objetivo:** GUI completa e funcional, integrada ao motor de cálculo.

### Issues

| ID | Título | Descrição | Prioridade | Responsável |
|----|--------|-----------|------------|-------------|
| 3.1 | Criar layout `main.fxml` | Campos numéricos com `TextField`: P_tx, P_rx, sensibilidade, distância, nº conectores, nº fusões, margem. `ComboBox` para splitter (valores 1:2 a 1:64). `ToggleButton` ou `RadioButton` downstream/upstream. `ComboBox` para classe GPON (B+, C+, C++). Botão "Calcular". Área de resultado (label). Área de alertas (`ListView` ou `TextArea`). Labels descritivas com unidades | 🔴 Alta | A |
| 3.2 | Implementar `MainController` — lógica do campo faltante | No evento do botão "Calcular": detectar qual campo está vazio (entre P_tx, P_rx, distância, splitter, conectores, fusões, margem). Se exatamente 1 campo vazio → instanciar `LinkBudget`, chamar `calcular()`, exibir resultado e alertas. Se 0 ou 2+ vazios → exibir erro amigável na área de alertas. Limpar resultados anteriores a cada novo cálculo | 🔴 Alta | B |
| 3.3 | Exibir resultados e alertas na UI | Resultado: label formatado com valor + unidade (ex: "Potência recebida: -24.5 dBm"). Alertas: exibidos como lista colorida (verde=INFO, amarelo=AVISO, vermelho=ERRO). Ícones visuais (✓, ⚠, ✗) | 🔴 Alta | B |
| 3.4 | Validação em tempo real dos campos | `TextFormatter` com filtro para aceitar apenas números, sinal negativo e ponto decimal. Feedback visual imediato: borda do campo fica vermelha se valor fora do range esperado (ex: P_tx > 10 dBm). Tooltip com range esperado ao passar o mouse | 🟡 Média | A |
| 3.5 | Estilização CSS e polimento | Arquivo `styles.css`: tema limpo (cores sóbrias), espaçamento consistente, fonte monoespaçada para valores numéricos. Tooltips informativos. Atalho de teclado: Enter no último campo dispara "Calcular". Título da janela: "Calculadora GPON — Link Budget" | 🟢 Baixa | A |

---

## Sprint 4: Integração, Testes e Documentação

**Duração estimada:** 2 dias
**Objetivo:** Sistema completo, testado e documentado.

### Issues

| ID | Título | Descrição | Prioridade | Responsável |
|----|--------|-----------|------------|-------------|
| 4.1 | Testes de integração (cenários reais) | Cenário 1: Downstream, splitter 1:32, 8km, 4 conectores, 6 fusões, margem 3dB → P_rx esperado. Cenário 2: Upstream, splitter 1:8, 15km → P_rx esperado. Cenário 3: Dados de projeto real (ex: provedor regional). Verificar se alertas disparam corretamente em cenários inválidos | 🔴 Alta | B |
| 4.2 | Tratamento de exceções robusto | Capturar `NumberFormatException` nos campos (se bypassar o TextFormatter). Capturar `IllegalArgumentException` do LinkBudget (ex: distância negativa). Diálogos de erro amigáveis (`Alert` JavaFX) com mensagens em português claro, sem stacktrace. Log de erros em console para debugging | 🟡 Média | A |
| 4.3 | Diagramas UML | **Diagrama de Caso de Uso**: 1 ator (Engenheiro de Rede), 3 casos de uso (Inserir Parâmetros, Calcular Variável Faltante, Visualizar Alertas). **Diagrama de Classes**: classes do modelo (Equipamento, LinkBudget, Validador, Alerta), controller (MainController), enums. Relacionamentos: associação, composição, dependência. Exportar como PNG/SVG | 🔴 Alta | A + B |
| 4.4 | Documentação final (README.md) | Como executar (`mvn javafx:run`). Pré-requisitos (JDK 17+, Maven). Explicação da fórmula Link Budget. Referências ITU-T G.984 e G.652. Screenshots da interface. Estrutura do projeto. Decisões de design (por que MVC, por que JavaFX) | 🟡 Média | A + B |

---

## Distribuição de Tarefas por Sprint (Dupla)

| Sprint | Pessoa A | Pessoa B |
|--------|----------|----------|
| **1** | 1.2 Constantes + 1.3 Equipamento | 1.1 Maven + 1.4 LinkBudget |
| **1** | 1.5 Testes LinkBudget (parcial) | 1.5 Testes LinkBudget (parcial) |
| **2** | 2.1 Alerta + 2.2 Validador | 2.3 Testes Validador |
| **3** | 3.1 main.fxml + 3.4 validação em tempo real + 3.5 CSS | 3.2 MainController + 3.3 exibição resultados |
| **4** | 4.2 Exceções + 4.3 UML (parcial) | 4.1 Testes integração + 4.3 UML (parcial) |
| **4** | 4.4 README (parcial) | 4.4 README (parcial) |

---

## Definição de Pronto (DoD — Definition of Done)

Para cada issue ser considerada concluída:

1. Código implementado conforme especificação
2. Testes unitários/integração passando (`mvn test`)
3. `mvn clean compile` sem erros nem warnings relevantes
4. Código revisado pelo outro membro da dupla
5. Código commitado com mensagem clara referenciando a issue

---

## Marcos do Projeto

| Marco | Data estimada | Entregável |
|-------|---------------|------------|
| Fim Sprint 1 | Dia 3 | `LinkBudget` calculando corretamente para todas as variáveis |
| Fim Sprint 2 | Dia 5 | Validador gerando alertas conforme ITU-T G.984 |
| Fim Sprint 3 | Dia 9 | Interface gráfica completa e funcional |
| Fim Sprint 4 | Dia 11 | Projeto finalizado: testes passando, UML documentado, README pronto |
