package com.gnb.trades.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.gnb.trades.Fragments.DetailFragment;
import com.gnb.trades.R;
import com.gnb.trades.Utils.Rate;
import com.gnb.trades.Utils.Transaction;

import java.util.ArrayList;
import java.util.HashMap;

public class TradesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<HashMap<String, ArrayList<Transaction>>> trades;
    private ArrayList<Rate> rates;
    private final Context context;

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        public View view;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }
    }

    public TradesAdapter(ArrayList<HashMap<String, ArrayList<Transaction>>> trades,
                         ArrayList<Rate> rates, Context context) {
        this.trades = trades;
        this.rates = rates;
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
            // Show product sku
            TextView skuText = ((ItemViewHolder) holder).view.findViewById(R.id.itemText);
            HashMap<String, ArrayList<Transaction>> product = trades.get(position);
            String key = product.keySet().toArray()[0].toString();
            String sSku = context.getString(R.string.sku_label) + " " + key;
            skuText.setText(sSku);
            // Set onClick event
            ((ItemViewHolder) holder).view.setOnClickListener(view -> {
                // Open the detail fragment of the product
                AppCompatActivity activity = (AppCompatActivity) context;
                DetailFragment fragment = DetailFragment.newInstance(product, rates);
                FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, fragment);
                ft.addToBackStack(null);
                ft.commit();
            });
        }
    }

    @Override
    public int getItemCount() {
        return trades == null ? 0 : trades.size();
    }
}
