# 🛡️ TransactionTracker — On-Device AI Expense & Anomaly Engine

**TransactionTracker** is a modern Android application engineered with an **on-device statistical anomaly engine**, quantitative risk scoring pipeline, and category pattern analyzers. Designed with privacy-first architecture, all financial data processing, pattern evaluations, and risk scoring execute 100% locally on the device's CPU and SQLite storage without cloud dependencies.

---

## 🌟 Key Features & Highlights

- **🛡️ On-Device Anomaly Detection Engine**: Runs domain-specific pattern analysis against historical SQLite records to flag transaction anomalies in real-time.
- **📊 Quantitative Risk Scoring (0–100)**: Evaluates statistical spend variance, duplicate transactions within 60 seconds, 3× category average spikes, and rapid velocity rules to assign dynamic risk levels (**HIGH**, **MEDIUM**, **LOW**).
- **📈 Category Spend Analytics Breakdown**: Visual progress graph displaying percentage spend distribution, entry count badges, and formatted totals per transaction type (`Food`, `Travel`, `Shopping`, `Bills`, `Other`).
- **🎯 1-Tap Category Entry Drill-Down**: Tapping any category in the analytics graph or filter chips instantly filters the list to view every individual transaction entry for that type.
- **⚙️ Customizable Anomaly Thresholds**: Persistent `SettingsManager` allowing users to configure custom spend limits for global high value and category-specific rules via a Material dialog.
- **🔍 Live Description Search & Filter Chips**: Real-time search filter combined with single-selection Material chips (`All`, `Flagged`, `High Risk`, `Food`, `Travel`, `Shopping`, `Bills`, `Other`).
- **🔒 100% Local Privacy**: Zero server uploads, external API calls, or cloud dependencies.

---

## 🏗️ Architecture & Design Patterns

The project follows clean architecture principles and software design patterns:

### 1. Strategy Pattern (`PatternAnalyzer`)
Each transaction category implements a dedicated pattern analyzer isolating category-specific velocity and spending rules:
- `TravelPatternAnalyzer`: Detects high travel expenditure and rapid transit charge velocity (multiple charges within 30 mins).
- `FoodPatternAnalyzer`: Detects unusually high dining expenses and rapid succession orders (multiple orders within 15 mins).
- `BillsPatternAnalyzer`: Detects unusually high bill amounts and recurring payments within 24 hours.
- `ShoppingPatternAnalyzer`: Detects high retail expenses and shopping spree patterns (3+ purchases within 1 hour).
- `DefaultPatternAnalyzer`: General fallback analyzer for uncategorized transactions.

### 2. Factory Pattern (`PatternAnalyzerFactory`)
Dynamically instantiates the appropriate category analyzer and injects user-configured settings.

### 3. Quantitative Risk Scoring (`RiskScoreCalculator`)
Computes a numeric **0–100 Risk Score** based on weighted signals:
- **Amount Ratio Signal** (Up to 30 pts)
- **Duplicate Charge Signal** (+25 pts)
- **Category 3× Average Spike Signal** (+25 pts)
- **Pattern Anomaly Velocity Signal** (+20 pts)

---

## 💻 Tech Stack & Libraries

- **Language**: Java / Kotlin
- **Architecture**: Clean Architecture / Strategy & Factory Patterns
- **Database**: Offline SQLite (`SQLiteOpenHelper`)
- **UI Components**: Material Design 3 (`MaterialCardView`, `ChipGroup`, `TextInputLayout`, `ProgressBar`, `MaterialAlertDialogBuilder`)
- **Testing**: JUnit 4 (Unit Test Suite)
- **Min SDK**: 24 (Android 7.0+)
- **Target SDK**: 36 (Android 15+)

---

## 🧪 Unit Testing

The repository includes a comprehensive unit test suite covering pattern analyzers, dynamic settings, and the rule engine.

Run unit tests via Gradle:
```bash
./gradlew app:testDebugUnitTest
```

---

## 🚀 Building & Running

1. **Clone Repository**:
   ```bash
   git clone https://github.com/puridanish10/TransactionTracker.git
   cd TransactionTracker
   ```

2. **Build Debug APK**:
   ```bash
   ./gradlew app:assembleDebug
   ```

3. **Install on Connected Device / Emulator**:
   ```bash
   ./gradlew app:installDebug
   ```

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
