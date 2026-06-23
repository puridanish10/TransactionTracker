package com.example.transactiontracker;


import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper       db;
    private FraudDetectionEngine engine;
    private TransactionAdapter   adapter;
    private List<Transaction>    transactions;

    private TextView  tvTotalCount, tvTotalSpend, tvFlagCount;
    private EditText  etName, etAmount;
    private Spinner   spinnerCategory, spinnerFilter;
    private RecyclerView recycler;

    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private static final String[] CATEGORIES = {"Food", "Travel", "Shopping", "Bills", "Other"};
    private static final String[] FILTERS    = {"All", "Flagged", "Food", "Travel", "Shopping", "Bills", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db     = new DatabaseHelper(this);
        engine = new FraudDetectionEngine(db);

        bindViews();
        setupSpinners();
        setupRecycler();
        refreshAll();
    }

    private void bindViews() {
        tvTotalCount  = findViewById(R.id.tv_total_count);
        tvTotalSpend  = findViewById(R.id.tv_total_spend);
        tvFlagCount   = findViewById(R.id.tv_flag_count);
        etName        = findViewById(R.id.et_name);
        etAmount      = findViewById(R.id.et_amount);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerFilter   = findViewById(R.id.spinner_filter);
        recycler        = findViewById(R.id.recycler);
        findViewById(R.id.btn_add).setOnClickListener(v -> addTransaction());
    }

    private void setupSpinners() {
        spinnerCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, CATEGORIES));

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, FILTERS);
        spinnerFilter.setAdapter(filterAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { applyFilter(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void setupRecycler() {
        transactions = db.getAllTransactions();
        adapter = new TransactionAdapter(transactions, tx -> {
            db.deleteTransaction(tx.getId());
            refreshAll();
            Snackbar.make(recycler, "Transaction deleted", Snackbar.LENGTH_SHORT).show();
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
    }

    private void addTransaction() {
        String name   = etName.getText().toString().trim();
        String amtStr = etAmount.getText().toString().trim();
        String cat    = CATEGORIES[spinnerCategory.getSelectedItemPosition()].toLowerCase();

        // Input validation — data governance gate
        if (name.isEmpty()) { etName.setError("Required"); return; }
        if (amtStr.isEmpty()) { etAmount.setError("Required"); return; }

        double amount;
        try { amount = Double.parseDouble(amtStr); }
        catch (NumberFormatException e) { etAmount.setError("Invalid amount"); return; }
        if (amount <= 0) { etAmount.setError("Must be > 0"); return; }

        Transaction tx = new Transaction(name, amount, cat);
        engine.analyze(tx);          // run rule engine before persist
        long id = db.insertTransaction(tx);

        if (id == -1) {
            Snackbar.make(recycler, "Data validation failed — entry rejected", Snackbar.LENGTH_LONG).show();
            return;
        }

        etName.setText("");
        etAmount.setText("");

        if (tx.isFlagged()) {
            Snackbar.make(recycler, "Anomaly flagged: " + tx.getFlagReason(), Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(R.color.flag_text))
                    .show();
        } else {
            Snackbar.make(recycler, "Transaction added — passed all checks", Snackbar.LENGTH_SHORT).show();
        }

        refreshAll();
    }

    private void applyFilter() {
        String selected = FILTERS[spinnerFilter.getSelectedItemPosition()];
        List<Transaction> filtered;

        switch (selected) {
            case "Flagged": filtered = db.getFlagged(); break;
            case "All":     filtered = db.getAllTransactions(); break;
            default:        filtered = db.getByCategory(selected.toLowerCase()); break;
        }

        transactions.clear();
        transactions.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    private void refreshAll() {
        applyFilter();
        tvTotalCount.setText(String.valueOf(db.getTotalCount()));
        tvTotalSpend.setText(fmt.format(db.getTotalSpend()));
        tvFlagCount.setText(db.getFlagged().size() + " flagged");
    }
}
