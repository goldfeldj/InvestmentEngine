# Investment Engine: AI-Driven Digital Twin

A decoupled, state-based simulation engine designed to treat investment portfolios as evolving datasets. The engine leverages multi-stage AI reasoning to generate, audit, and execute investment strategies across different temporal contexts.

## 🚀 Core Vision & Goals

* **The Digital Twin:** A mirrored representation of your wealth. Portfolios are treated as persistent "states" (YAML) that evolve through time based on AI-driven actions rather than static tracking.
* **The "Time Machine" Logic:** Supporting three distinct operational modes:
1. **Real Present:** Live portfolio auditing and recommendation execution.
2. **Simulated Present:** "What-if" branching where recommendations are persisted to new state files without affecting the live ledger.
3. **Simulated Historical:** Full backtesting from a specific `start_date` to evaluate engine performance against historical market reality.


* **Historical Amnesia:** A strict protocol ensuring the AI only "knows" what was public at a specific `effective_date`, eliminating look-ahead bias and "cheating" during backtests.
* **Market Impact Simulation:** High-fidelity ledger updates including:
* **25% Tax Drag** on realized gains.
* **Transaction Slippage** (e.g., 0.1% cost).
* **Currency Conversion** (ILS/USD/Digital Assets).



---

## 🏗️ Architectural Insights

* **The Logic Loop (Manager vs. Thinker):**
* **Manager (`InvestmentOrchestrator`):** Handles file I/O, directory management, and time-stepping.
* **Thinker (`ModelChainOrchestrator`):** The "Decision Core" that processes a state through the AI chain.


* **Multi-Stage AI Chain (The 3-Phase Debate):**
* **Auditor:** Proposes moves based on current state and macro context.
* **Anti-Thesis:** Critiques the auditor, looking for risks and contrarian viewpoints.
* **Chairman:** Reconciles the debate to produce the final executable `List<Recommendation>`.


* **YAML-as-Database:** Human-readable persistence. Every simulation "tick" generates a new YAML file, creating a versioned history of the portfolio's evolution.
* **Type-Safe Simulation:** Built on Kotlin and Jackson to ensure mathematical integrity between asset types (Private Equity, Digital Assets, and Cash Wallets).

---

## 🛠️ Execution Workflow

1. **Input:** A `GlobalPortfolio` state (`asOfDate`) and a temporal configuration.
2. **Context Injection:** The engine injects date-boxed macro data and the "Amnesia Protocol" into the system prompts.
3. **The Debate:** The model chain (Auditor → Anti-Thesis → Chairman) executes across specified AI models (e.g., Gemini 1.5 Pro, Flash).
4. **Execution:** The `SimulationEngine` applies the final decisions to the ledger.
5. **Output:** A new persistent YAML state and a performance delta report relative to benchmarks like **SPY** or **QQQ**.

---

**Would you like me to begin drafting the `runAuditChain` implementation to bring the "Thinker" logic to life?**
