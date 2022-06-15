package com.gnb.trades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gnb.trades.Fragments.TradesFragment;
import com.gnb.trades.Utils.Rate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String RATES_URL = "http://quiet-stone-2094.herokuapp.com/rates";
    private static final String TRANSACTIONS_URL = "http://quiet-stone-2094.herokuapp.com/transactions";

    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init views
        loading = findViewById(R.id.loading);
        // Obtain data
        new Thread(this::getData).start();
    }

    private void getData() {
        // Init data
        String rates = null;
        String transactions = null;
        // Show loading
        runOnUiThread(() -> loading.setVisibility(View.VISIBLE));
        // Init client
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        // Init end points array
        String[] endPoints = {RATES_URL, TRANSACTIONS_URL};
        // Add one call for each end point
        for (String sUrl : endPoints) {
            Request request = new Request.Builder()
                    .url(sUrl)
                    .method("GET", null)
                    .addHeader("Accept", "application/json")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    switch (sUrl) {
                        case RATES_URL:
                            rates = Objects.requireNonNull(response.body()).string();
                            break;
                        case TRANSACTIONS_URL:
                            transactions = Objects.requireNonNull(response.body()).string();
                            break;
                    }
                    // Check if both requests have returned data
                    if ((rates != null) && (transactions != null)) {
                        manageData(rates, transactions);
                    }
                } else {
                    dataNotFound();
                }
            } catch (IOException e) {
                dataNotFound();
            }
        }
    }

    private void manageData(String rates, String transactions) {
        // Set conversion rates and get EUR rates
        ArrayList<Rate> eurRates = completeRates(rates);
        // Add TradesFragment to view
        TradesFragment fragment = TradesFragment.newInstance(transactions, eurRates);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.container, fragment);
        ft.commit();
        // Dismiss loading
        runOnUiThread(() -> loading.setVisibility(View.GONE));
    }

    private ArrayList<Rate> completeRates(String rates) {
        ArrayList<ArrayList<Rate>> allRates = new ArrayList<>();
        ArrayList<String> currencies = new ArrayList<>();
        // Order the rates of same currency
        try {
            JSONArray ratesArray = new JSONArray(rates);
            for (int i = 0 ; i < ratesArray.length() ; i++) {
                JSONObject rateObject = ratesArray.getJSONObject(i);
                String to = rateObject.getString("to");
                // Create rate object
                Rate rate = new Rate(rateObject.getString("from"), to,
                        rateObject.getDouble("rate"));
                boolean added = false;
                // Check if this currency has an array for him and add the rates
                for (ArrayList<Rate> currencyRates : allRates) {
                    if (to.equals(currencyRates.get(0).getTo())) {
                        currencyRates.add(rate);
                        added = true;
                    }
                }
                // Create an array for currency and add to array of all rates
                if (!added) {
                    ArrayList<Rate> currencyRates = new ArrayList<>();
                    currencyRates.add(rate);
                    allRates.add(currencyRates);
                    // Add currency to currencies array
                    currencies.add(to);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Complete the rates that are not provided
        if (allRates.size() > 0) {
            for (ArrayList<Rate> currencyRates : allRates) {
                // Check if for this currency, you have all the rates
                while (currencyRates.size() != (allRates.size()-1)) {
                    // Check what rate is missing
                    for (String currency : currencies) {
                        boolean found = false;
                        String currentCurrency = currencyRates.get(0).getTo();
                        // Check that are not the same currency
                        if (!currency.equals(currentCurrency)) {
                            boolean rateExist = false;
                            for (Rate rate : currencyRates) {
                                // Check if this rate exist
                                if (currency.equals(rate.getFrom())) {
                                    rateExist = true;
                                    break;
                                }
                            }
                            // Search a common currency
                            if (!rateExist) {
                                // From = currency
                                // To = currentCurrency
                                for (String common : currencies) {
                                    // Check that the common currency are not the same that the missing
                                    // trade currency
                                    if ((!common.equals(currency)) && (!common.equals(currentCurrency))) {
                                        // Search currency -> common and common -> currentCurrency
                                        double fromCurrencyToCommon = -1;
                                        double fromCommonToCurrentCurrency = -1;
                                        for (ArrayList<Rate> currencyRatesAux : allRates) {
                                            for (Rate rateAux : currencyRatesAux) {
                                                if ((rateAux.getFrom().equals(currency)) &&
                                                        (rateAux.getTo().equals(common))) {
                                                    fromCurrencyToCommon = rateAux.getRate();
                                                } else if ((rateAux.getFrom().equals(common)) &&
                                                        (rateAux.getTo().equals(currentCurrency))) {
                                                    fromCommonToCurrentCurrency = rateAux.getRate();
                                                }
                                                if ((fromCommonToCurrentCurrency != -1) &&
                                                        (fromCurrencyToCommon != -1)) {
                                                    // Missing rate found!
                                                    found = true;
                                                    double missingValue = fromCurrencyToCommon *
                                                            fromCommonToCurrentCurrency;
                                                    Rate missing = new Rate(currency, currentCurrency,
                                                            missingValue);
                                                    // Add the missing rate to array
                                                    currencyRates.add(missing);
                                                    break;
                                                }
                                            }
                                            if (found) {
                                                // Exit
                                                break;
                                            }
                                        }
                                    }
                                    if (found) {
                                        // Exit
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return getEurRates(allRates);
    }

    private ArrayList<Rate> getEurRates(ArrayList<ArrayList<Rate>> allRates) {
        ArrayList<Rate> eurRates = null;
        for (ArrayList<Rate> rates : allRates) {
            if (rates.get(0).getTo().equals("EUR")) {
                eurRates = rates;
                break;
            }
        }
        return eurRates;
    }
    
    private void dataNotFound() {
        runOnUiThread(() -> {
            // Dismiss loading
            loading.setVisibility(View.GONE);
            // Show error message
            Toast.makeText(MainActivity.this,
                    R.string.data_not_found, Toast.LENGTH_SHORT).show();
        });
    }
}