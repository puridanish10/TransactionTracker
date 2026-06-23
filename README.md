# TransactionTracker

An enterprise-grade Android application for transaction management and real-time anomaly detection. This project demonstrates high-quality data governance patterns, rule-based decisioning, and local data persistence using SQLite.

## 🚀 Key Features

- **Real-time Anomaly Detection**: Built-in rule engine that flags high-value transactions, duplicates within a short window, and category-specific spending spikes (3x above average).
- **Data Governance**: Enforces strict data validation at the storage layer to ensure zero integrity errors.
- **SQLite Pipeline**: Efficient local persistence with indexed queries for performance and offline capability.
- **Dynamic Categorization**: Log and filter transactions across multiple categories (Food, Travel, Shopping, etc.).
- **Insights Dashboard**: Instant summary of total spend, transaction counts, and flagged anomalies.

## 🛠 Tech Stack

- **Language**: Java
- **Database**: SQLite
- **UI Architecture**: RecyclerView + ViewHolder Pattern
- **Design**: Material Design Components

## 🧠 Fraud Detection Logic

The app implements a modular `FraudDetectionEngine` that evaluates every transaction before it is committed to the database:
1. **Velocity Check**: Prevents double-billing by flagging identical transactions occurring within 60 seconds.
2. **Threshold Monitoring**: Flags any transaction exceeding ₹5,000.
3. **Behavioral Analysis**: Calculates historical averages per category and flags transactions that deviate by more than 300%.

## 📂 Project Structure

- `com.example.transactiontracker.DatabaseHelper`: Manages SQLite lifecycle and data validation.
- `com.example.transactiontracker.FraudDetectionEngine`: Contains the rule-based decisioning logic.
- `com.example.transactiontracker.TransactionAdapter`: Bridges the data model to the Material UI.

---
*Developed as a demonstration of robust Android architecture and data-driven application design.*
