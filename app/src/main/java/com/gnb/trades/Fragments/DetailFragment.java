package com.gnb.trades.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gnb.trades.Adapters.DetailAdapter;
import com.gnb.trades.Adapters.TradesAdapter;
import com.gnb.trades.R;
import com.gnb.trades.Utils.Rate;
import com.gnb.trades.Utils.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {

    private static final String ARG_PRODUCT = "product";
    private static final String ARG_RATES = "rates";
    private static final String MAIN_CURRENCY = "EUR";

    private ArrayList<Transaction> transactions;
    private ArrayList<Rate> rates;
    private View rootView;

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param product Parameter 1.
     * @param rates Parameter 2.
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(HashMap<String, ArrayList<Transaction>> product,
                                             ArrayList<Rate> rates) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        args.putSerializable(ARG_RATES, rates);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rates = (ArrayList<Rate>) getArguments().getSerializable(ARG_RATES);
            HashMap<String, ArrayList<Transaction>> product = (HashMap<String,
                    ArrayList<Transaction>>) getArguments().getSerializable(ARG_PRODUCT);
            // Get transactions
            String sku = product.keySet().toArray()[0].toString();
            transactions = product.get(sku);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // Show details and init recycler view
        showDetails();

        return rootView;
    }

    private void showDetails() {
        // Get transactions
        if (transactions != null) {
            // Show total transactions detail
            ((TextView) rootView.findViewById(R.id.totalText)).setText(
                    String.valueOf(transactions.size()));
            // Calculate and show the total amount of trades
            calculateTotalAmount();
            // Show all transactions
            initRecyclerView();
        }
    }

    private void calculateTotalAmount() {
        double totalAmount = 0;
        for (Transaction transaction : transactions) {
            double amount = transaction.getAmount();
            String currency = transaction.getCurrency();
            // If the currency of transaction is different of the desired, apply the rate
            if (!currency.equals(MAIN_CURRENCY)) {
                for (Rate rate : rates) {
                    if (rate.getFrom().equals(currency)) {
                        amount = amount * rate.getRate();
                        // Round half to even
                        amount = roundHalfToEven(amount);
                        break;
                    }
                }
            }
            totalAmount += amount;
        }
        if (totalAmount > 0) {
            // Round half to even
            totalAmount = roundHalfToEven(totalAmount);
        }
        // Show the detail
        String detail = totalAmount + " " + getString(R.string.EUR);
        ((TextView) rootView.findViewById(R.id.totalAmountText)).setText(detail);
    }

    private double roundHalfToEven(double amount) {
        BigDecimal bd = BigDecimal.valueOf(amount);
        bd = bd.setScale(2, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = rootView.findViewById(R.id.transactionsView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new DetailAdapter(transactions, getActivity()));
    }
}