package com.example.transactiontracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
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
        h.tvCategory.setText(tx.getCategory().toUpperCase());
        h.tvDate.setText(sdf.format(new Date(tx.getTimestamp())));

        if (tx.isFlagged()) {
            h.tvFlag.setVisibility(View.VISIBLE);
            h.tvFlag.setText(tx.getFlagReason());
            h.tvFlag.setBackgroundColor(ContextCompat.getColor(h.itemView.getContext(), R.color.flag_bg));
            h.tvAmount.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.flag_text));
        } else {
            h.tvFlag.setVisibility(View.GONE);
            h.tvAmount.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.ok_text));
        }

        h.btnDelete.setOnClickListener(v -> listener.onDelete(tx));
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  tvName, tvAmount, tvCategory, tvDate, tvFlag;
        ImageView btnDelete;
        ViewHolder(View v) {
            super(v);
            tvName     = v.findViewById(R.id.tv_name);
            tvAmount   = v.findViewById(R.id.tv_amount);
            tvCategory = v.findViewById(R.id.tv_category);
            tvDate     = v.findViewById(R.id.tv_date);
            tvFlag     = v.findViewById(R.id.tv_flag);
            btnDelete  = v.findViewById(R.id.btn_delete);
        }
    }
}