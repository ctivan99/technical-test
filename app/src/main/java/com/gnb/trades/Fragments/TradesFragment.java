package com.gnb.trades.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gnb.trades.Adapters.TradesAdapter;
import com.gnb.trades.R;
import com.gnb.trades.Utils.Rate;
import com.gnb.trades.Utils.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TradesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TradesFragment extends Fragment {

    private static final String ARG_TRANSACTIONS = "transactions";
    private static final String ARG_RATES = "rates";

    private JSONArray data;
    private ArrayList<Rate> rates;
    private View rootView;

    public TradesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param transactions Parameter 1.
     * @param rates Parameter 2.
     * @return A new instance of fragment TradesFragment.
     */
    public static TradesFragment newInstance(String transactions, ArrayList<Rate> rates) {
        TradesFragment fragment = new TradesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TRANSACTIONS, transactions);
        args.putSerializable(ARG_RATES, rates);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                data = new JSONArray(getArguments().getString(ARG_TRANSACTIONS));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            rates = (ArrayList<Rate>) getArguments().getSerializable(ARG_RATES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_trades, container, false);
        classifyProducts();
        return rootView;
    }

    private void classifyProducts() {
        // Classify and order all the products
        if (data != null) {
            ArrayList<HashMap<String, ArrayList<Transaction>>> classifiedTransactions = new ArrayList<>();
            for (int i = 0; i < data.length() ; i++) {
                Transaction transaction = null;
                try {
                    JSONObject jsonObj = data.getJSONObject(i);
                    // Init transaction object
                    transaction = new Transaction(jsonObj.getString("sku"),
                            jsonObj.getDouble("amount"), jsonObj.getString("currency"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (transaction != null) {
                    // Check if this product is already classified
                    boolean classified = false;
                    String sku = transaction.getSku();
                    for (HashMap<String, ArrayList<Transaction>> hmTransactions :
                            classifiedTransactions) {
                        if (hmTransactions.containsKey(sku)) {
                            // Add a transaction from the same product
                            Objects.requireNonNull(hmTransactions.get(sku)).add(transaction);
                            classified = true;
                            break;
                        }
                    }

                    if (!classified) {
                        // If all products are checked, create a new product in the list
                        ArrayList<Transaction> transactionArrayList = new ArrayList<>();
                        transactionArrayList.add(transaction);
                        // Init HashMap
                        HashMap<String, ArrayList<Transaction>> hmTransactions = new HashMap<>();
                        hmTransactions.put(sku, transactionArrayList);
                        // Add new product in array
                        classifiedTransactions.add(hmTransactions);
                    }
                }
            }

            // Init view
            initRecyclerView(classifiedTransactions);
        }
    }

    private void initRecyclerView(ArrayList<HashMap<String, ArrayList<Transaction>>>
                                          classifiedTransactions) {
        RecyclerView recyclerView = rootView.findViewById(R.id.tradesView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new TradesAdapter(classifiedTransactions, rates, getActivity()));
    }
}