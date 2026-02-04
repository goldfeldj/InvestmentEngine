# Investment Engine: AI-Driven Digital Twin

A decoupled, state-based simulation engine designed to treat investment portfolios as evolving datasets. The engine leverages multi-stage AI reasoning to generate, audit, and execute investment strategies across different temporal contexts.

## 🚀 Core Vision & Goals

* **The Digital Twin:** A mirrored representation of wealth where portfolios are persistent "states" (YAML) that evolve through AI-driven actions.
* **The "Time Machine" Logic:** 1. **Real Present:** Live portfolio auditing and recommendation execution.
    2. **Simulated Present:** "What-if" branching (persisted to new states without affecting the live ledger).
    3. **Simulated Historical:** Full backtesting from a specific `start_date` against historical market reality.
* **Historical Amnesia:** A strict protocol ensuring the AI only "knows" what was public at a specific `effective_date`, eliminating look-ahead bias.
* **Market Impact Simulation:** High-fidelity ledger updates including:
    * **25% Tax Drag** on realized gains.
    * **Transaction Slippage** (e.g., 0.1% cost).
    * **Currency Conversion** (ILS/USD/Digital Assets).

---

## 🏗️ Architectural Insights

### The Logic Loop
* **Manager (`InvestmentOrchestrator`):** Handles file I/O, directory management, and time-stepping.
* **Thinker (`ModelChainOrchestrator`):** The "Decision Core" that processes a state through the AI chain.

### Multi-Stage AI Chain (The 3-Phase Debate)
1. **Auditor:** Proposes moves based on current state and macro context.
2. **Anti-Thesis:** Critiques the auditor, identifying risks and contrarian viewpoints.
3. **Chairman:** Reconciles the debate to produce the final executable `List<Recommendation>`.

### Data Strategy & Reliability
* **Hybrid Market Data Layer:** A "Cache-Aside" pattern using **SQLite** for local persistence and high-performance retrieval.
* **Resilient API Integration:** Robust consumption of **FMP** (Real-time/Forex) and **Tiingo** (Historical) with automated error handling for market holidays and empty data sets.
* **YAML-as-Database:** Human-readable persistence. Every simulation "tick" generates a new YAML file, creating a versioned history of the portfolio's evolution.

---

## 🚦 Current Project State

### ✅ Completed
* **Persistence Layer:** Fully implemented YAML parsing and writing for `GlobalPortfolio` states.
* **Market Data Cache:** SQLite integration with JPA repositories and custom dialect support.
* **External Providers:** Wired `RestClient` integrations for FMP and Tiingo with API key security via environment variables.
* **Infrastructure:** Multi-provider AI client setup (Gemini, OpenAI, DeepSeek) with local environment protection (`.env` integration).

### 🚧 In Progress
* **Simulation Engine Logic:** Refining the mathematical application of "Tax Drag" and "Slippage" to the live ledger after a Chairman's decision.
* **Historical Amnesia Tests:** Validating that the `MarketDataService` correctly routes to historical providers when `targetDate < now()`.

### 📅 Next Steps
1. **The "Warm-up" Script:** Implement a `CommandLineRunner` to pre-cache historical prices for core tickers to speed up backtests.
2. **Mock AI Profiles:** Create a "Local/Mock" mode for the `IntelligenceService` to allow full orchestration testing without consuming API credits.
3. **Macro Context Injection:** Build the service that scrapes/fetches "Macro News" for a specific historical date to feed the Auditor's prompt.

---

## 🛠️ Execution Workflow

1. **Input:** A `GlobalPortfolio` state (`asOfDate`) and a temporal configuration.
2. **Context Injection:** The engine injects date-boxed macro data and the "Amnesia Protocol" into the system prompts.
3. **The Debate:** The model chain executes across specified AI models (e.g., Gemini 1.5 Pro, Flash).
4. **Market Data Sync:** Real-time or historical prices are pulled (Cache -> API) to value the portfolio.
5. **Execution:** The `SimulationEngine` applies decisions.
6. **Output:** A new persistent YAML state and a performance delta report relative to benchmarks (SPY/QQQ).

---