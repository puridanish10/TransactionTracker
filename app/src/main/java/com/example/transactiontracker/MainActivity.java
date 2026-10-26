package com.example.transactiontracker;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper       db;
    private SettingsManager      settings;
    private FraudDetectionEngine engine;
    private TransactionAdapter   adapter;
    private List<Transaction>    transactions;

    private TextView     tvTotalCount, tvTotalSpend, tvFlagCount;
    private EditText     etName, etAmount, etSearch;
    private Spinner      spinnerCategory;
    private ChipGroup    chipGroupFilter;
    private RecyclerView recycler;
    private LinearLayout layoutEmpty;

    // Category Analytics Views
    private ProgressBar progressFood, progressTravel, progressShopping, progressBills, progressOther;
    private TextView    tvFoodCount, tvFoodPct, tvFoodTotal;
    private TextView    tvTravelCount, tvTravelPct, tvTravelTotal;
    private TextView    tvShoppingCount, tvShoppingPct, tvShoppingTotal;
    private TextView    tvBillsCount, tvBillsPct, tvBillsTotal;
    private TextView    tvOtherCount, tvOtherPct, tvOtherTotal;

    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private static final String[] CATEGORIES = {"Food", "Travel", "Shopping", "Bills", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db       = new DatabaseHelper(this);
        settings = new SettingsManager(this);
        engine   = new FraudDetectionEngine(db, settings);

        bindViews();
        setupSpinner();
        setupSearchAndChips();
        setupCategoryGraphClickListeners();
        setupRecycler();
        refreshAll();
    }

    private void bindViews() {
        tvTotalCount    = findViewById(R.id.tv_total_count);
        tvTotalSpend    = findViewById(R.id.tv_total_spend);
        tvFlagCount     = findViewById(R.id.tv_flag_count);
        etName          = findViewById(R.id.et_name);
        etAmount        = findViewById(R.id.et_amount);
        etSearch        = findViewById(R.id.et_search);
        spinnerCategory = findViewById(R.id.spinner_category);
        chipGroupFilter = findViewById(R.id.chip_group_filter);
        recycler        = findViewById(R.id.recycler);
        layoutEmpty     = findViewById(R.id.layout_empty);

        // Bind Category Analytics Graph Views
        progressFood     = findViewById(R.id.progress_food);
        tvFoodCount      = findViewById(R.id.tv_food_count);
        tvFoodPct        = findViewById(R.id.tv_food_pct);
        tvFoodTotal      = findViewById(R.id.tv_food_total);

        progressTravel   = findViewById(R.id.progress_travel);
        tvTravelCount    = findViewById(R.id.tv_travel_count);
        tvTravelPct      = findViewById(R.id.tv_travel_pct);
        tvTravelTotal    = findViewById(R.id.tv_travel_total);

        progressShopping = findViewById(R.id.progress_shopping);
        tvShoppingCount  = findViewById(R.id.tv_shopping_count);
        tvShoppingPct    = findViewById(R.id.tv_shopping_pct);
        tvShoppingTotal  = findViewById(R.id.tv_shopping_total);

        progressBills    = findViewById(R.id.progress_bills);
        tvBillsCount     = findViewById(R.id.tv_bills_count);
        tvBillsPct       = findViewById(R.id.tv_bills_pct);
        tvBillsTotal     = findViewById(R.id.tv_bills_total);

        progressOther    = findViewById(R.id.progress_other);
        tvOtherCount     = findViewById(R.id.tv_other_count);
        tvOtherPct       = findViewById(R.id.tv_other_pct);
        tvOtherTotal     = findViewById(R.id.tv_other_total);

        findViewById(R.id.btn_add).setOnClickListener(v -> addTransaction());
        findViewById(R.id.btn_settings).setOnClickListener(v -> showSettingsDialog());
    }

    private void setupSpinner() {
        spinnerCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, CATEGORIES));
    }

    private void setupSearchAndChips() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_all)) {
                if (etSearch.getText() != null && etSearch.getText().length() > 0) {
                    etSearch.setText("");
                    etSearch.clearFocus();
                }
            }
            applyFilterAndSearch();
        });
    }

    private void setupCategoryGraphClickListeners() {
        findViewById(R.id.row_graph_food).setOnClickListener(v -> selectCategoryChip(R.id.chip_food));
        findViewById(R.id.row_graph_travel).setOnClickListener(v -> selectCategoryChip(R.id.chip_travel));
        findViewById(R.id.row_graph_shopping).setOnClickListener(v -> selectCategoryChip(R.id.chip_shopping));
        findViewById(R.id.row_graph_bills).setOnClickListener(v -> selectCategoryChip(R.id.chip_bills));
        findViewById(R.id.row_graph_other).setOnClickListener(v -> selectCategoryChip(R.id.chip_other));
    }

    private void selectCategoryChip(int chipId) {
        chipGroupFilter.check(chipId);
        applyFilterAndSearch();
    }

    private void setupRecycler() {
        transactions = new ArrayList<>();
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
        String cat    = CATEGORIES[spinnerCategory.getSelectedItemPosition()].toLowerCase(Locale.ROOT);

        if (name.isEmpty()) { etName.setError("Required"); return; }
        if (amtStr.isEmpty()) { etAmount.setError("Required"); return; }

        double amount;
        try {
            amount = Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            return;
        }
        if (amount <= 0) {
            etAmount.setError("Must be > 0");
            return;
        }

        Transaction tx = new Transaction(name, amount, cat);
        engine.analyze(tx);          // run rule engine before persist
        long id = db.insertTransaction(tx);

        if (id == -1) {
            Snackbar.make(recycler, "Data validation failed — entry rejected", Snackbar.LENGTH_LONG).show();
            return;
        }

        etName.setText("");
        etAmount.setText("");
        etName.clearFocus();
        etAmount.clearFocus();

        if (tx.isFlagged()) {
            Snackbar.make(recycler, "Anomaly flagged: " + tx.getFlagReason(), Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(R.color.flag_text))
                    .show();
        } else {
            Snackbar.make(recycler, "Transaction added — passed all checks", Snackbar.LENGTH_SHORT).show();
        }

        refreshAll();
    }

    private void showSettingsDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null);

        EditText etGlobal   = view.findViewById(R.id.et_setting_global);
        EditText etTravel   = view.findViewById(R.id.et_setting_travel);
        EditText etFood     = view.findViewById(R.id.et_setting_food);
        EditText etShopping = view.findViewById(R.id.et_setting_shopping);
        EditText etBills    = view.findViewById(R.id.et_setting_bills);
        EditText etGeneral  = view.findViewById(R.id.et_setting_general);

        etGlobal.setText(String.valueOf((int) settings.getGlobalHighValueThreshold()));
        etTravel.setText(String.valueOf((int) settings.getTravelThreshold()));
        etFood.setText(String.valueOf((int) settings.getFoodThreshold()));
        etShopping.setText(String.valueOf((int) settings.getShoppingThreshold()));
        etBills.setText(String.valueOf((int) settings.getBillsThreshold()));
        etGeneral.setText(String.valueOf((int) settings.getGeneralThreshold()));

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_settings_title)
                .setView(view)
                .setPositiveButton(R.string.btn_save, (dialog, which) -> {
                    saveThresholdIfValid(etGlobal,   val -> settings.setGlobalHighValueThreshold(val));
                    saveThresholdIfValid(etTravel,   val -> settings.setTravelThreshold(val));
                    saveThresholdIfValid(etFood,     val -> settings.setFoodThreshold(val));
                    saveThresholdIfValid(etShopping, val -> settings.setShoppingThreshold(val));
                    saveThresholdIfValid(etBills,    val -> settings.setBillsThreshold(val));
                    saveThresholdIfValid(etGeneral,  val -> settings.setGeneralThreshold(val));

                    Snackbar.make(recycler, R.string.msg_settings_saved, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private interface ThresholdConsumer {
        void accept(double value);
    }

    private void saveThresholdIfValid(EditText editText, ThresholdConsumer consumer) {
        if (editText == null) return;
        String valStr = editText.getText().toString().trim();
        if (valStr.isEmpty()) return;
        try {
            double val = Double.parseDouble(valStr);
            if (val > 0) {
                consumer.accept(val);
            }
        } catch (NumberFormatException ignored) {}
    }

    private void updateCategoryAnalyticsGraph(List<Transaction> all) {
        double grandTotal = 0;
        double foodSpend = 0, travelSpend = 0, shoppingSpend = 0, billsSpend = 0, otherSpend = 0;
        int foodCount = 0, travelCount = 0, shoppingCount = 0, billsCount = 0, otherCount = 0;

        for (Transaction tx : all) {
            double amt = tx.getAmount();
            grandTotal += amt;
            String cat = (tx.getCategory() != null) ? tx.getCategory().toLowerCase(Locale.ROOT) : "other";

            switch (cat) {
                case "food":
                    foodSpend += amt; foodCount++; break;
                case "travel":
                    travelSpend += amt; travelCount++; break;
                case "shopping":
                    shoppingSpend += amt; shoppingCount++; break;
                case "bills":
                    billsSpend += amt; billsCount++; break;
                default:
                    otherSpend += amt; otherCount++; break;
            }
        }

        updateCategoryRow(progressFood, tvFoodCount, tvFoodPct, tvFoodTotal, foodSpend, foodCount, grandTotal);
        updateCategoryRow(progressTravel, tvTravelCount, tvTravelPct, tvTravelTotal, travelSpend, travelCount, grandTotal);
        updateCategoryRow(progressShopping, tvShoppingCount, tvShoppingPct, tvShoppingTotal, shoppingSpend, shoppingCount, grandTotal);
        updateCategoryRow(progressBills, tvBillsCount, tvBillsPct, tvBillsTotal, billsSpend, billsCount, grandTotal);
        updateCategoryRow(progressOther, tvOtherCount, tvOtherPct, tvOtherTotal, otherSpend, otherCount, grandTotal);
    }

    private void updateCategoryRow(ProgressBar bar, TextView tvCount, TextView tvPct, TextView tvTotal, double catSpend, int catCount, double totalSpend) {
        int pct = totalSpend > 0 ? (int) Math.round((catSpend / totalSpend) * 100.0) : 0;
        bar.setProgress(pct);
        String countText = String.format(Locale.getDefault(), "%d %s", catCount, catCount == 1 ? "entry" : "entries");
        tvCount.setText(countText);
        String pctText = String.format(Locale.getDefault(), "%d%%", pct);
        tvPct.setText(pctText);
        tvTotal.setText(fmt.format(catSpend));
    }

    private void applyFilterAndSearch() {
        int checkedChipId = chipGroupFilter.getCheckedChipId();
        List<Transaction> source;

        if (checkedChipId == R.id.chip_flagged) {
            source = db.getFlagged();
        } else if (checkedChipId == R.id.chip_high_risk) {
            source = new ArrayList<>();
            for (Transaction tx : db.getAllTransactions()) {
                if ("HIGH".equalsIgnoreCase(tx.getRiskLevel())) {
                    source.add(tx);
                }
            }
        } else if (checkedChipId == R.id.chip_food) {
            source = db.getByCategory("food");
        } else if (checkedChipId == R.id.chip_travel) {
            source = db.getByCategory("travel");
        } else if (checkedChipId == R.id.chip_shopping) {
            source = db.getByCategory("shopping");
        } else if (checkedChipId == R.id.chip_bills) {
            source = db.getByCategory("bills");
        } else if (checkedChipId == R.id.chip_other) {
            source = db.getByCategory("other");
        } else {
            source = db.getAllTransactions();
        }

        String query = etSearch.getText() != null ? etSearch.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
        List<Transaction> filtered = new ArrayList<>();

        for (Transaction tx : source) {
            if (query.isEmpty() || (tx.getName() != null && tx.getName().toLowerCase(Locale.ROOT).contains(query))) {
                filtered.add(tx);
            }
        }

        transactions.clear();
        transactions.addAll(filtered);
        adapter.notifyDataSetChanged();

        if (transactions.isEmpty()) {
            recycler.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void refreshAll() {
        List<Transaction> all = db.getAllTransactions();
        updateCategoryAnalyticsGraph(all);
        applyFilterAndSearch();

        tvTotalCount.setText(String.valueOf(db.getTotalCount()));
        tvTotalSpend.setText(fmt.format(db.getTotalSpend()));
        String flagText = db.getFlagged().size() + " flagged";
        tvFlagCount.setText(flagText);
    }
}
