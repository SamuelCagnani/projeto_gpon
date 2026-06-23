# Calculadora GPON — Link Budget

**Projeto Interdisciplinar:** Propagação de Ondas Eletromagnéticas × Engenharia de Software

Calculadora de balanço de enlace (Link Budget) para redes GPON (Gigabit Passive Optical Network). O sistema isola e resolve qualquer variável faltante da equação de propagação óptica, validando os resultados contra os limites da norma ITU-T G.984.

---

## Stack

| Tecnologia | Versão | Uso |
|-----------|--------|-----|
| Java | 21 (LTS) | Linguagem base |
| JavaFX | 21.0.2 | Interface gráfica desktop |
| Maven | 3.9+ | Gerenciamento de build e dependências |
| JUnit 5 | 5.10.2 | Testes unitários e de integração |

---

## Como Executar

### Pré-requisitos

- JDK 21+
- Maven 3.9+ (ou use a wrapper `mvnw`)

### Build e Testes

```bash
mvn clean compile      # Compilar
mvn test               # Executar 54 testes
mvn javafx:run         # Abrir interface gráfica
```

### Estrutura do Projeto

```
src/main/java/br/edu/gpon/
├── model/                          # Domínio puro — zero dependência de JavaFX
│   ├── LinkBudget.java             # Motor de cálculo — isola variável faltante
│   ├── Equipamento.java            # Classes fábrica (OLT, ONU, fibra, splitter…)
│   ├── Validador.java              # 6 regras de validação ITU-T G.984
│   ├── ClasseGPON.java             # Enum B+ / C+ / C++ com limites por direção
│   ├── ComprimentoOnda.java        # Downstream (1490nm) / Upstream (1310nm)
│   ├── Alerta.java                 # Mensagem com severidade (INFO/AVISO/ERRO)
│   ├── TipoAlerta.java             # Enum de severidade
│   └── TipoEquipamento.java        # Enum OLT, ONU, FIBRA, SPLITTER…
├── controller/
│   └── MainController.java         # Controller JavaFX — orquestra View ↔ Model
├── view/ (resources)
│   ├── main.fxml                   # Layout declarativo da interface
│   └── styles.css                  # Tema escuro Catppuccin-inspired
├── util/
│   └── Constantes.java             # Valores ITU-T G.984, G.652, tabela splitters
└── MainApp.java                    # Entry point (carrega FXML, 720×680)

src/test/java/br/edu/gpon/model/
├── LinkBudgetTest.java             # 21 testes — motor de cálculo
├── ValidadorTest.java              # 23 testes — regras de validação
└── IntegracaoTest.java             # 10 testes — cenários reais de engenharia
```

---

## Fórmula do Link Budget

```
P_rx = P_tx − (α × d) − A_splitter − (N_con × 0.5) − (N_fus × 0.1) − M
```

| Variável | Unidade | Descrição |
|----------|---------|-----------|
| **P_tx** | dBm | Potência de transmissão |
| **P_rx** | dBm | Potência recebida |
| **α** | dB/km | Atenuação da fibra G.652 (1490nm: 0.35, 1310nm: 0.40) |
| **d** | km | Distância do enlace |
| **A_splitter** | dB | Perda do divisor óptico: `10 × log₁₀(N)` |
| **N_con** | un | Número de conectores (0.5 dB cada) |
| **N_fus** | un | Número de fusões (0.1 dB cada) |
| **M** | dB | Margem de segurança (recomendado ≥ 3 dB) |

### Splitters Padrão

| Razão | Atenuação (dB) |
|-------|----------------|
| 1:2   | 3.01           |
| 1:4   | 6.02           |
| 1:8   | 9.03           |
| 1:16  | 12.04          |
| 1:32  | 15.05          |
| 1:64  | 18.06          |

### Classes GPON (ITU-T G.984)

| Classe | Atenuação Máx. | P_tx OLT (dBm) | P_tx ONU (dBm) | Sensibilidade (dBm) |
|--------|----------------|----------------|----------------|----------------------|
| **B+** | 28 dB | 1.5 a 5.0 | 0.5 a 5.0 | −28 |
| **C+** | 32 dB | 3.0 a 7.0 | 0.5 a 5.0 | −31 |
| **C++** | 35 dB | 6.0 a 10.0 | 0.5 a 7.0 | −34 |

---

## Funcionalidades

### 1. Cálculo da Variável Faltante
O usuário preenche 6 dos 7 campos da equação e deixa exatamente 1 vazio. O sistema detecta qual campo está vazio, isola a variável algebricamente e resolve.

Variáveis que podem ser calculadas:
- **P_tx** — Potência de transmissão necessária
- **P_rx** — Potência recebida no destino
- **Distância** — Alcance máximo do enlace
- **Splitter** — Atenuação máxima do divisor
- **Conectores** — Quantidade máxima de conectores
- **Fusões** — Quantidade máxima de fusões
- **Margem** — Margem de segurança disponível

### 2. Validação ITU-T G.984
Após o cálculo, o sistema valida todos os parâmetros contra os limites da classe GPON selecionada (B+, C+, C++):

| Regra | ERRO | AVISO |
|-------|------|-------|
| P_tx fora do range da classe | ✓ | — |
| P_rx < sensibilidade | ✓ | — |
| P_rx a menos de 2 dB da sensibilidade | — | ✓ |
| Distância > 60 km | ✓ | — |
| Distância > 20 km | — | ✓ |
| Atenuação total > limite da classe | ✓ | — |
| Atenuação total > 80% do limite | — | ✓ |
| Splitter fora do padrão (1:2 a 1:64) | ✓ | — |
| Margem negativa (enlace inviável) | ✓ | — |
| Margem < 1 dB | — | ✓ |
| Margem > 5 dB (superdimensionamento) | — | INFO ℹ |

### 3. Interface Gráfica
- **Tema escuro** com código de cores consistente
- **Validação em tempo real** nos campos numéricos (regex, impede caracteres inválidos)
- **Botão Calcular** com atalho Enter
- **ListView de alertas** coloridos:
  <span style="color:#f38ba8">■ ERRO</span> &nbsp;
  <span style="color:#f9e2af">■ AVISO</span> &nbsp;
  <span style="color:#a6e3a1">■ INFO</span>
- **Toggle Downstream/Upstream** com grupo exclusivo
- **Suporte a 3 classes GPON** (B+, C+, C++)
- **Botão Limpar** para resetar todos os campos

---

## Arquitetura (MVC)

```
┌──────────────────────────────────────────────┐
│                   VIEW                        │
│  main.fxml + styles.css                      │
│  Layout declarativo, eventos, exibição        │
├──────────────────────────────────────────────┤
│                CONTROLLER                     │
│  MainController.java                         │
│  Detecta campo vazio, orquestra cálculo,     │
│  valida e exibe resultado + alertas           │
├──────────────────────────────────────────────┤
│                  MODEL                        │
│  LinkBudget | Equipamento | Validador        │
│  Alerta | ClasseGPON | ComprimentoOnda       │
│  Domínio puro — zero dependência JavaFX      │
└──────────────────────────────────────────────┘
```

O pacote `model/` não tem nenhuma dependência de JavaFX, permitindo testes unitários isolados.

---

## Diagramas UML

### Diagrama de Caso de Uso

```
                    ┌─────────────────────────────────┐
                    │   Calculadora GPON Link Budget   │
                    │                                   │
   ┌────────────────┤  ⬤ Inserir Parâmetros            │
   │                │     (P_tx, sensibilidade,         │
   │                │      distância, splitters,        │
   │                │      conectores, fusões, margem)  │
   │                │                                   │
   │                │  ⬤ Calcular Variável Faltante     │
   │                │     (isola e resolve a            │
   │                │      equação de Link Budget)      │
   │                │                                   │
   │                │  ⬤ Visualizar Alertas             │
   │                │     (validação ITU-T G.984)       │
   │                └─────────────────────────────────┘
   │
   └── Engenheiro de Rede
```

### Diagrama de Classes

```
┌──────────────────────┐        ┌──────────────────────┐
│     LinkBudget       │        │    Equipamento       │
├──────────────────────┤        ├──────────────────────┤
│ - pTx: Double        │        │ - nome: String       │
│ - pRx: Double        │        │ - tipo: TipoEquip    │
│ - distancia: Double  │        │ - potenciaTxMin      │
│ - splitRatio: Integer│        │ - potenciaTxMax      │
│ - conectores: Integer│        │ - sensibilidade      │
│ - fusoes: Integer    │        │ - perdaNominal       │
│ - margem: Double     │        ├──────────────────────┤
│ - comprimentoOnda    │        │ + olt()              │
│ - variavelFaltante   │        │ + onu()              │
├──────────────────────┤        │ + fibra()            │
│ + calcular()         │        │ + splitter(ratio)    │
│ + getResultado()     │        │ + conector()         │
│ + calcularAtenuacao()│        │ + fusao()            │
└──────────┬───────────┘        └──────────────────────┘
           │
           │ usa
           ▼
┌──────────────────────┐        ┌──────────────────────┐
│     Validador        │        │       Alerta         │
├──────────────────────┤        ├──────────────────────┤
│ + validar(           │        │ - mensagem: String   │
│   LinkBudget,        │───────▶│ - tipo: TipoAlerta   │
│   ClasseGPON):       │        │ - campoAfetado       │
│   List<Alerta>       │        └──────────────────────┘
└──────────────────────┘
                                       │
         ┌─────────────────────────────┼──────────────────────┐
         │                             │                      │
         ▼                             ▼                      ▼
┌──────────────────┐    ┌──────────────────────┐   ┌──────────────────────┐
│  ClasseGPON      │    │  ComprimentoOnda     │   │   TipoAlerta         │
├──────────────────┤    ├──────────────────────┤   ├──────────────────────┤
│ B_PLUS           │    │ DOWNSTREAM_1490      │   │ INFO                 │
│ C_PLUS           │    │ UPSTREAM_1310        │   │ AVISO                │
│ C_PLUS_PLUS      │    │                      │   │ ERRO                 │
│                  │    │ + getAtenuacaoDbPorKm │   └──────────────────────┘
│ + getPTxMin(dir) │    └──────────────────────┘
│ + getPTxMax(dir) │
└──────────────────┘
```

---

## Testes

**54 testes automatizados** cobrindo todas as camadas:

| Suite | Testes | Cobertura |
|-------|--------|-----------|
| `LinkBudgetTest` | 21 | Cálculo de cada variável, downstream/upstream, casos de borda (distância zero, splitter 1:1), exceções (0 ou 2+ faltantes, valores negativos) |
| `ValidadorTest` | 23 | Todas as 6 regras, 3 classes GPON, múltiplos alertas simultâneos, validação pré e pós-cálculo |
| `IntegracaoTest` | 10 | Cenários reais de engenharia: enlace de provedor regional, enlace longo, enlace inviável, C++, margem mínima, fluxo completo |

```bash
$ mvn test
Tests run: 54, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## Decisões de Design

### Por que MVC?
O enunciado exige separação entre lógica de cálculo e exibição (§Dicas, item 3). O MVC garante que o pacote `model/` seja testável sem levantar a interface gráfica.

### Por que Enum para constantes do domínio?
`ComprimentoOnda`, `ClasseGPON` e `TipoAlerta` são conjuntos fechados. Enums garantem type-safety, evitam "magic strings" e permitem associar dados (ex: atenuação em dB/km) a cada constante.

### Por que Factory Methods em Equipamento?
`Equipamento.olt()`, `Equipamento.splitter(32)` centralizam valores de referência ITU-T em um único ponto, facilitando manutenção.

### Por que TextFormatter (regex) em vez de Spinner?
Spinners JavaFX não permitem campo vazio (necessário para a variável faltante). O `TextFormatter` com regex aceita campo vazio e valida em tempo real.

---

## Referências Normativas

| Norma | Descrição |
|-------|-----------|
| ITU-T G.984.1 | GPON — Características gerais |
| ITU-T G.984.2 | GPON — PMD layer, classes B+, C+, C++ |
| ITU-T G.652 | Fibra monomodo padrão (atenuação 0.35–0.40 dB/km) |

---

## Licença

Projeto acadêmico interdisciplinar — Engenharia de Software + Propagação de Ondas Eletromagnéticas.
