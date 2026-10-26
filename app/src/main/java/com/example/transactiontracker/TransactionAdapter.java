package com.example.transactiontracker;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public interface OnDeleteListener { void onDelete(Transaction tx); }

    private final List<Transaction> items;
    private final OnDeleteListener  listener;
    private final NumberFormat      fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat  sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());

    public TransactionAdapter(List<Transaction> items, OnDeleteListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Transaction tx = items.get(position);
        h.tvName.setText(tx.getName());
        h.tvAmount.setText(fmt.format(tx.getAmount()));
        String catStr = (tx.getCategory() != null) ? tx.getCategory() : "other";
        h.tvCategory.setText(catStr.toUpperCase(Locale.ROOT));
        h.tvDate.setText(sdf.format(new Date(tx.getTimestamp())));

        // Set Category Icon
        int iconRes;
        switch (catStr.toLowerCase(Locale.ROOT)) {
            case "food":     iconRes = R.drawable.ic_food; break;
            case "travel":   iconRes = R.drawable.ic_travel; break;
            case "shopping": iconRes = R.drawable.ic_shopping; break;
            case "bills":    iconRes = R.drawable.ic_bills; break;
            default:         iconRes = R.drawable.ic_other; break;
        }
        h.ivCategoryIcon.setImageResource(iconRes);

        // Bind Risk Badge & Score
        int score = tx.getRiskScore();
        String level = tx.getRiskLevel();
        String riskText = level + " (" + score + ")";
        h.tvRiskBadge.setText(riskText);

        int badgeColor;
        switch (level) {
            case "HIGH":
                badgeColor = Color.parseColor("#DC2626");
                break;
            case "MEDIUM":
                badgeColor = Color.parseColor("#D97706");
                break;
            default:
                badgeColor = Color.parseColor("#16A34A");
                break;
        }
        h.tvRiskBadge.setBackgroundTintList(ColorStateList.valueOf(badgeColor));

        if (tx.isFlagged()) {
            h.cardFlagBanner.setVisibility(View.VISIBLE);
            h.tvFlag.setText(tx.getFlagReason());
            h.tvAmount.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.flag_text));
        } else {
            h.cardFlagBanner.setVisibility(View.GONE);
            h.tvAmount.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.ok_text));
        }

        h.btnDelete.setOnClickListener(v -> listener.onDelete(tx));
    }

    @Override public int getItemCount() { return items.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView         tvName, tvAmount, tvCategory, tvRiskBadge, tvDate, tvFlag;
        ImageView        ivCategoryIcon, btnDelete;
        MaterialCardView cardFlagBanner;

        ViewHolder(View v) {
            super(v);
            tvName         = v.findViewById(R.id.tv_name);
            tvAmount       = v.findViewById(R.id.tv_amount);
            tvCategory     = v.findViewById(R.id.tv_category);
            tvRiskBadge    = v.findViewById(R.id.tv_risk_badge);
            tvDate         = v.findViewById(R.id.tv_date);
            tvFlag         = v.findViewById(R.id.tv_flag);
            ivCategoryIcon = v.findViewById(R.id.iv_category_icon);
            btnDelete      = v.findViewById(R.id.btn_delete);
            cardFlagBanner = v.findViewById(R.id.card_flag_banner);
        }
    }
}
