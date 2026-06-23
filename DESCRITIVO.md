# Documento Descritivo do Projeto — Calculadora GPON Link Budget

**Projeto Interdisciplinar:** Propagação de Ondas Eletromagnéticas × Engenharia de Software
**Tecnologia:** Java 17 + JavaFX + Maven
**Equipe:** Dupla

---

## 1. Visão Geral do Projeto

### 1.1 Contexto do Domínio

GPON (Gigabit Passive Optical Network) é a tecnologia de rede óptica passiva predominante no mercado de telecomunicações. Em uma rede GPON, o sinal óptico trafega da OLT (Optical Line Terminal — central da operadora) até a ONU (Optical Network Unit — equipamento no cliente) através de uma infraestrutura inteiramente passiva: fibras ópticas, splitters (divisores de potência), conectores e fusões.

O principal desafio de engenharia é garantir que a potência do sinal que chega ao receptor seja suficiente para ser interpretada (acima da sensibilidade do equipamento), considerando todas as perdas acumuladas ao longo do caminho óptico. Esse cálculo é chamado de **Link Budget** (balanço do enlace).

### 1.2 Objetivo do Software

Desenvolver uma calculadora que, diferente de sistemas lineares, seja capaz de **isolar e calcular qualquer variável faltante** da equação de Link Budget. O engenheiro de rede fornece todos os parâmetros menos um, e o sistema determina o valor desconhecido — seja a distância máxima suportada, a potência de transmissão necessária, o splitter máximo permitido, entre outros.

---

## 2. Requisitos de Engenharia de Software

### 2.1 Requisitos Funcionais

| ID | Descrição | Origem |
|----|-----------|--------|
| **RF1** | O sistema deve calcular qualquer variável da fórmula de Link Budget, desde que as demais sejam fornecidas pelo usuário | Enunciado §7 |
| **RF2** | O sistema deve validar os dados de entrada com base nos padrões da norma ITU-T G.984 e gerar alertas quando valores estiverem fora das faixas esperadas | Enunciado §7 |
| **RF3** | O sistema deve suportar os dois comprimentos de onda do GPON: downstream (1490 nm) e upstream (1310 nm), com atenuações distintas para cada um | Enunciado §2 |
| **RF4** | A interface deve indicar visualmente qual campo está vazio (a variável a ser calculada) e preenchê-lo com o resultado após o cálculo | Derivado do RF1 |
| **RF5** | O sistema deve validar entradas em tempo real (durante a digitação), impedindo caracteres não numéricos e sinalizando valores fora de faixa | Enunciado §Dicas |
| **RF6** | O sistema deve exibir alertas categorizados por severidade (informação, aviso, erro) com base nos limites da ITU-T G.984 | Enunciado §3 |

### 2.2 Requisitos Não Funcionais

| ID | Descrição | Categoria |
|----|-----------|-----------|
| **RNF1** | A arquitetura deve separar a interface de usuário da lógica matemática de propagação (padrão MVC ou equivalente) | Arquitetural |
| **RNF2** | Se o usuário não preencher dados suficientes para fechar a equação, o software deve retornar um erro amigável — nunca travar | Confiabilidade |
| **RNF3** | O diagrama de classes deve contemplar as entidades: `LinkBudget`, `Equipamento`, `Validador` | Documentação |
| **RNF4** | A interface deve usar campos numéricos com validação em tempo real | Usabilidade |
| **RNF5** | O código deve ser modular, com responsabilidades bem definidas entre classes | Manutenibilidade |

---

## 3. Arquitetura de Software

### 3.1 Padrão Arquitetural: MVC (Model-View-Controller)

A escolha do MVC atende diretamente ao RNF1 (separação lógica ↔ interface) e é o padrão natural do JavaFX:

```
┌──────────────────────────────────────────────────────────┐
│                      VIEW (JavaFX FXML)                   │
│  main.fxml + styles.css                                   │
│  Responsabilidade: exibir campos, capturar eventos,       │
│  mostrar resultados e alertas                             │
├──────────────────────────────────────────────────────────┤
│                    CONTROLLER (MainController)             │
│  Responsabilidade: intermediar View e Model, detectar     │
│  campo faltante, orquestrar cálculo e validação           │
├──────────────────────────────────────────────────────────┤
│                        MODEL (Domínio)                     │
│  LinkBudget | Equipamento | Validador | Alerta            │
│  Responsabilidade: regras de negócio, fórmula de          │
│  propagação, validações ITU-T                             │
└──────────────────────────────────────────────────────────┘
```

**Fluxo de execução:**

1. View captura os valores dos campos e o evento "Calcular"
2. Controller detecta qual campo está vazio (variável faltante)
3. Controller instancia `LinkBudget` com os valores fornecidos
4. `LinkBudget.calcular()` isola algebricamente a variável faltante e resolve
5. Controller passa o resultado para `Validador.validar()`
6. Controller devolve resultado + lista de alertas para a View exibir

### 3.2 Diagrama de Classes (Conceitual)

```
┌─────────────────┐       ┌─────────────────────┐
│   LinkBudget    │       │    Equipamento      │
├─────────────────┤       ├─────────────────────┤
│ - p_tx: Double  │       │ - nome: String      │
│ - p_rx: Double  │       │ - tipo: TipoEquip   │
│ - distancia     │       │ - potenciaTxMin     │
│ - splitRatio    │       │ - potenciaTxMax     │
│ - conectores    │       │ - sensibilidade     │
│ - fusoes        │       │ - perdaNominal      │
│ - margem: Double│       └─────────────────────┘
│ - comprOnda     │
│ - variavelFalt  │       ┌─────────────────────┐
├─────────────────┤       │     Validador       │
│ + calcular()    │       ├─────────────────────┤
│ + getResultado()│       │ + validar(          │
│ + getVariavel() │       │   LinkBudget):      │
└─────────────────┘       │   List<Alerta>      │
                          └─────────────────────┘
         │
         │ usa
         ▼
┌─────────────────┐       ┌─────────────────────┐
│     Alerta      │       │ <<enum>>            │
├─────────────────┤       │  TipoAlerta         │
│ - mensagem      │       ├─────────────────────┤
│ - tipo          │       │ INFO                │
│ - campoAfetado  │       │ AVISO               │
└─────────────────┘       │ ERRO                │
                          └─────────────────────┘

┌─────────────────────────────────────────────┐
│ <<enum>> ComprimentoOnda                    │
├─────────────────────────────────────────────┤
│ DOWNSTREAM_1490 (α = 0.35 dB/km)            │
│ UPSTREAM_1310   (α = 0.40 dB/km)            │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ <<enum>> TipoEquipamento                    │
├─────────────────────────────────────────────┤
│ OLT, ONU, FIBRA, SPLITTER, CONECTOR, FUSAO  │
└─────────────────────────────────────────────┘
```

### 3.3 Diagrama de Caso de Uso

```
┌────────────────────────────────────────────────┐
│            Calculadora GPON Link Budget         │
│                                                 │
│  ┌──────────────────────────────┐               │
│  │     Inserir Parâmetros       │               │
│  │  (P_tx, sensibilidade,       │               │
│  │   distância, splitters,      │               │
│  │   conectores, fusões, margem)│               │
│  └──────────────┬───────────────┘               │
│                 │                                │
│  ┌──────────────▼───────────────┐               │
│  │  Calcular Variável Faltante  │               │
│  │  (isola e resolve a equação  │               │
│  │   de Link Budget)             │               │
│  └──────────────┬───────────────┘               │
│                 │                                │
│  ┌──────────────▼───────────────┐               │
│  │    Visualizar Alertas        │◄────┐         │
│  │  (validação ITU-T G.984,     │     │         │
│  │   limites de engenharia)     │     │         │
│  └──────────────────────────────┘     │         │
│                                       │         │
└───────────────────────────────────────┼─────────┘
                                        │
                            ┌───────────┴───────────┐
                            │  Engenheiro de Rede   │
                            └───────────────────────┘
```

---

## 4. Engenharia de Propagação (Domínio Físico)

### 4.1 A Equação do Link Budget

A potência recebida (P_rx) é igual à potência transmitida (P_tx) subtraída de todas as perdas acumuladas no enlace:

```
P_rx = P_tx - A_fibra(d) - A_splitter(N) - A_conectores - A_fusoes - M_seguranca
```

Cada componente de perda:

| Componente | Fórmula | Valor típico |
|------------|---------|--------------|
| Fibra óptica (G.652) | A = α × d | α₁₄₉₀ = 0.35 dB/km, α₁₃₁₀ = 0.40 dB/km |
| Splitter óptico (1:N) | A = 10 × log₁₀(N) | 1:2→3dB, 1:4→6dB, 1:8→9dB, 1:16→12dB, 1:32→15dB, 1:64→18dB |
| Conectores | A = N_con × 0.5 | 0.5 dB por conector |
| Fusões | A = N_fus × 0.1 | 0.1 dB por fusão |
| Margem de segurança | M | 3 dB (recomendado) |

### 4.2 Comprimentos de Onda GPON

| Direção | Comprimento de Onda | Atenuação (G.652) | Tx → Rx |
|---------|---------------------|-------------------|---------|
| Downstream | 1490 nm | 0.35 dB/km | OLT → ONU |
| Upstream | 1310 nm | 0.40 dB/km | ONU → OLT |

A diferença de atenuação é significativa: em um enlace de 20 km, o upstream perde 1 dB a mais que o downstream apenas por efeito da fibra.

### 4.3 Isolamento da Variável Faltante

O motor de cálculo (`LinkBudget`) opera por rearranjo algébrico. Dado que a equação é linear em todas as variáveis, o isolamento é direto:

| Variável faltante | Fórmula isolada |
|-------------------|-----------------|
| P_tx | P_tx = P_rx + (α×d) + A_splitter + (N_con×0.5) + (N_fus×0.1) + M |
| P_rx | P_rx = P_tx − (α×d) − A_splitter − (N_con×0.5) − (N_fus×0.1) − M |
| d | d = (P_tx − P_rx − A_splitter − (N_con×0.5) − (N_fus×0.1) − M) / α |
| A_splitter | A_splitter = P_tx − P_rx − (α×d) − (N_con×0.5) − (N_fus×0.1) − M |
| N_con | N_con = (P_tx − P_rx − (α×d) − A_splitter − (N_fus×0.1) − M) / 0.5 |
| N_fus | N_fus = (P_tx − P_rx − (α×d) − A_splitter − (N_con×0.5) − M) / 0.1 |
| M | M = P_tx − P_rx − (α×d) − A_splitter − (N_con×0.5) − (N_fus×0.1) |

---

## 5. Estratégia de Validação (Alertas)

### 5.1 Referência Normativa: ITU-T G.984

A norma ITU-T G.984 define classes de desempenho para redes GPON:

| Parâmetro | Classe B+ | Classe C+ | Classe C++ |
|-----------|-----------|-----------|------------|
| Atenuação máxima do enlace | 28 dB | 32 dB | 35 dB |
| P_tx OLT (downstream) | +1.5 a +5 dBm | +3 a +7 dBm | +6 a +10 dBm |
| P_tx ONU (upstream) | +0.5 a +5 dBm | +0.5 a +5 dBm | +0.5 a +7 dBm |
| Sensibilidade ONU (down) | −28 dBm | −31 dBm | −34 dBm |
| Sensibilidade OLT (up) | −28 dBm | −31 dBm | −34 dBm |
| Distância máxima (típica) | 20 km | 20 km | 20 km |

### 5.2 Matriz de Validação

| Validação | Condição | Severidade |
|-----------|----------|------------|
| P_tx dentro do range da classe | valor < min OU valor > max | ERRO |
| P_rx ≥ sensibilidade | resultado < sensibilidade | ERRO |
| Atenuação total ≤ limite da classe | perda_total > limite_classe | ERRO |
| Distância excede limite lógico | d > 60 km | ERRO |
| Distância excede recomendação | 20 km < d ≤ 60 km | AVISO |
| Splitter dentro dos padrões | splitRatio ∉ {2,4,8,16,32,64} | ERRO |
| Margem < 1 dB | margem < 1.0 | AVISO |
| Margem > 5 dB (superdimensionamento) | margem > 5.0 | INFO |
| Todos os parâmetros dentro do esperado | — | INFO (sucesso) |

### 5.3 Tratamento de Exceções

| Situação | Comportamento esperado |
|----------|------------------------|
| Campo com texto não numérico | `TextFormatter` impede a digitação; se bypass, `NumberFormatException` → diálogo "Valor inválido" |
| Nenhum campo vazio (não há o que calcular) | Mensagem: "Preencha todos os campos exceto a variável que deseja calcular" |
| Dois ou mais campos vazios | Mensagem: "Deixe exatamente 1 campo vazio para calcular a variável faltante" |
| Distância negativa | Mensagem: "A distância não pode ser negativa" |
| Splitter resulta em atenuação negativa (bug) | `IllegalArgumentException` com mensagem descritiva |
| Divisão por zero no isolamento (ex: α=0) | Tratado na fórmula; nunca deve ocorrer pois α é constante fixa |

---

## 6. Decisões de Design

### 6.1 Por que Java?

- **Tipagem forte e estática**: reduz erros em cálculos numéricos críticos de engenharia
- **Orientação a objetos madura**: facilita a modelagem do domínio (Equipamento, LinkBudget, Validador)
- **JavaFX**: framework nativo para interfaces desktop ricas, com suporte a FXML (separação layout/lógica) e CSS
- **Maven**: gerenciamento de dependências e build padronizado
- **Ecossistema de testes**: JUnit 5 consolidado, TestFX para testes de interface

### 6.2 Por que MVC?

O enunciado exige explicitamente que "a lógica de cálculo de propagação esteja separada da lógica de exibição de dados" (§Dicas, item 3). O MVC é o padrão que atende esse requisito de forma natural:

- **Model**: classes `LinkBudget`, `Equipamento`, `Validador` — zero dependência de JavaFX
- **View**: arquivo `main.fxml` — declaração pura de layout, sem lógica de negócio
- **Controller**: `MainController` — única classe que conhece tanto Model quanto View

Essa separação permite, por exemplo, testar toda a lógica de propagação com testes unitários simples (JUnit), sem levantar a interface gráfica.

### 6.3 Por que Enum para Constantes do Domínio?

`ComprimentoOnda`, `TipoEquipamento` e `TipoAlerta` são modelados como enums porque:

- São conjuntos fechados e conhecidos em tempo de compilação
- Evitam "magic strings" e erros de digitação
- Permitem associar dados (ex: atenuação em dB/km) diretamente a cada constante
- Facilitam switches exaustivos e type-safety

### 6.4 Padrão Método Fábrica em Equipamento

A classe `Equipamento` oferece métodos estáticos (`Equipamento.olt()`, `Equipamento.onu()`, etc.) que retornam instâncias pré-configuradas com valores padrão da ITU-T. Isso:

- Centraliza o conhecimento dos valores de referência
- Evita que o controller ou a view precisem conhecer constantes de engenharia
- Facilita a manutenção: alterar um valor de referência muda em um único lugar

---

## 7. Estrutura de Pacotes e Responsabilidades

| Pacote | Propósito | Depende de |
|--------|-----------|------------|
| `model` | Classes de domínio: fórmula, equipamentos, validação, alertas | Nenhum (puro Java) |
| `controller` | Orquestração entre View e Model | `model`, JavaFX |
| `view` | Layout FXML, folhas de estilo CSS | JavaFX |
| `util` | Constantes de engenharia (ITU-T G.984, G.652) | Nenhum |

**Regra de dependência:** `model` não conhece `controller` nem `view`. `controller` conhece `model` e `view`. `view` não conhece `model` diretamente (apenas via controller).

---

## 8. Plano de Testes

### 8.1 Testes Unitários (JUnit 5)

- **LinkBudgetTest**: 7 cenários, um para cada variável faltante. Verificação com valores calculados manualmente (precisão 0.01 dB). Casos de borda: distância zero, splitter 1:1, conectores zero.
- **ValidadorTest**: Cenários para cada classe GPON (B+, C+, C++). Entradas válidas → 0 erros. Entradas com P_tx acima do máximo, distância > 60 km, P_rx abaixo da sensibilidade → validação de severidade correta.
- **EquipamentoTest**: Verificar se métodos fábrica retornam instâncias com atributos corretos.

### 8.2 Testes de Integração

- Cenário completo: downstream, splitter 1:32, 8 km, 4 conectores, 6 fusões, margem 3 dB → verificar P_rx calculado.
- Cenário upstream análogo com atenuação diferente.
- Verificar se a UI responde corretamente a entradas inválidas (sem levantar exceções não tratadas).

---

## 9. Referências Técnicas

| Norma | Descrição |
|-------|-----------|
| ITU-T G.984.1 | GPON — Características gerais |
| ITU-T G.984.2 | GPON — Especificação da camada dependente do meio físico (PMD) — define classes B+, C+, C++ |
| ITU-T G.652 | Características da fibra monomodo padrão (atenuação 0.35-0.40 dB/km) |

---

## 10. Cronograma Resumido

| Sprint | Dias | Entregável principal |
|--------|------|----------------------|
| 1 — Fundação | 1–3 | Motor de cálculo funcional (`LinkBudget`) |
| 2 — Validação | 4–5 | Sistema de alertas ITU-T (`Validador`) |
| 3 — Interface | 6–9 | GUI JavaFX completa e integrada |
| 4 — Finalização | 10–11 | Testes, UML, documentação |
