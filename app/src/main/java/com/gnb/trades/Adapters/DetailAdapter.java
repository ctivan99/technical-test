package com.gnb.trades.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gnb.trades.R;
import com.gnb.trades.Utils.Transaction;

import java.util.ArrayList;
import java.util.HashMap;

public class DetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Transaction> transactions;
    private final Context context;

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        public View view;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }
    }

    public DetailAdapter(ArrayList<Transaction> transactions, Context context) {
        this.transactions = transactions;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list,
                parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            TextView itemText = ((ItemViewHolder) holder).view.findViewById(R.id.itemText);
            Transaction transaction = transactions.get(position);
            String s = transaction.getSku() + " - " + transaction.getAmount() + " " +
                transaction.getCurrency();
            itemText.setText(s);
        }
    }

    @Override
    public int getItemCount() {
        return transactions == null ? 0 : transactions.size();
    }
}
