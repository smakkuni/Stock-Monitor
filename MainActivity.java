package com.example.stockmonitorapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String API_KEY = "dce4498a02754ac986642c39de67b1fa";

    private EditText symbolEditText;
    private TextView stockPriceTextView;
    private Map<String, String> companyNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        symbolEditText = findViewById(R.id.symbolEditText);
        stockPriceTextView = findViewById(R.id.stockPriceTextView);
        Button fetchButton = findViewById(R.id.fetchButton);

        initializeCompanyNames();

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String symbol = symbolEditText.getText().toString().trim().toUpperCase();
                if (!symbol.isEmpty()) {
                    fetchStockPrice(symbol);
                } else {
                    stockPriceTextView.setText("Please enter a valid stock symbol.");
                }
            }
        });
    }

    private void initializeCompanyNames() {
        companyNames = new HashMap<>();
        companyNames.put("AAPL", "Apple Inc.");
        companyNames.put("IBM", "IBM Corporation");
        companyNames.put("MSFT", "Microsoft Corporation");
        companyNames.put("TSLA", "Tesla, Inc.");
        companyNames.put("GOOGL", "Alphabet Inc. (Google)");
        companyNames.put("AMZN", "Amazon.com, Inc.");
    }

    private void fetchStockPrice(String symbol) {
        new FetchStockPriceTask(symbol).execute();
    }

    private class FetchStockPriceTask extends AsyncTask<Void, Void, String> {
        private String symbol;

        public FetchStockPriceTask(String symbol) {
            this.symbol = symbol;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String apiUrl = "https://api.twelvedata.com/quote?symbol=" + symbol + "&apikey=" + API_KEY;
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

                if (jsonResponse.has("message")) {
                    return "Error fetching price: " + jsonResponse.get("message").getAsString();
                }
                String price = jsonResponse.get("close").getAsString();
                String companyName = companyNames.getOrDefault(symbol, symbol);
                return companyName + ": $" + price;

            } catch (Exception e) {
                e.printStackTrace();
                return "Error fetching price for " + companyNames.getOrDefault(symbol, symbol);
            }
        }

        @Override
        protected void onPostExecute(String stockInfo) {
            stockPriceTextView.setText(stockInfo);
        }
    }
}
