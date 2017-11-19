package com.example.mathijs.mathijsparmentierpset3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class cart extends AppCompatActivity {

    JSONObject orderCounts;
    JSONArray priceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Set up App Bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.cart_toolbar);
        setSupportActionBar(myToolbar);

        // Set up pricelist
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://resto.mprog.nl/menu";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            priceList = new JSONArray();
                            JSONArray items = response.getJSONArray("items");
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject j = new JSONObject();
                                j.put(items.getJSONObject(i).optString("name"),
                                        items.getJSONObject(i).optString("price"));
                                priceList.put(j);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        loadFromSharedPrefs();
                        int count = getDishCount();
                        float price = getTotalPrice();
                        Toolbar myToolbar = (Toolbar) findViewById(R.id.cart_toolbar);
                        myToolbar.setTitle(count + " dishes for €" + price + "0");


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error);
                    }
                }
        );
        queue.add(jsonObjectRequest);
        }

    private void loadFromSharedPrefs() {

        SharedPreferences prefs = this.getSharedPreferences("orderList", this.MODE_PRIVATE);
        String receivedOrders = prefs.getString("order", null);

        if (receivedOrders != null) {

            List<String> orderList = new ArrayList<String>();

            try {
                orderCounts = new JSONObject(receivedOrders);
                for (int i = 0; i < orderCounts.names().length(); i++) {
                    orderList.add(orderCounts.names().getString(i));
                }
                populateList(orderList);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private int getDishCount() {
        int count = 0;
        for (int i = 0; i < orderCounts.names().length(); i++) {
            try {
                count = count + Integer.parseInt(orderCounts.get(orderCounts.names().getString(i)).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    private float getTotalPrice() {
        float price = 0;
        for (int i = 0; i < orderCounts.names().length(); i++) {
            try {
                price = price + (orderCounts.getInt(orderCounts.names().get(i).toString()) *
                        getDishPrice(orderCounts.names().get(i).toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return price;
    }

    private float getDishPrice(String dishName) {
        for (int i = 0; i < priceList.length(); i++) {
            try {
                if (priceList.getJSONObject(i).keys().next().equals(dishName)) {
                    return Float.parseFloat(priceList.getJSONObject(i).getString(dishName));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void populateList(final List<String> receivedOrders) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                receivedOrders
                );

        ListView orderList = (ListView) findViewById(R.id.orderList);
        orderList.setAdapter(adapter);
        orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                // https://stackoverflow.com/a/2115770
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        cart.this,
                        android.R.style.Theme_Material_Dialog_Alert);
                builder.setTitle("Delete dish");
                builder.setMessage("Are you sure you want to delete this dish from your order?");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = getSharedPreferences(
                                "orderList",
                                getApplicationContext().MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        String received = prefs.getString("order", null);

                        JSONObject order = null;
                        try {
                            order = new JSONObject(received);
                            order.remove(receivedOrders.get(position));
                            editor.putString("order", order.toString());
                            editor.commit();
                            finish();
                            startActivity(getIntent());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        });
    }

    public void sendOrder(View view) {
        Toast toast = Toast.makeText(this, "Order sent!", Toast.LENGTH_LONG);
        toast.show();

        // Instantiate Requestqueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://resto.mprog.nl/order";

        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject res = new JSONObject(response);
                            String responseText =
                                    res.getString("preparation_time") + " minutes remaining";
                            Toast waitingTime = Toast.makeText(
                                    getApplicationContext(),
                                    responseText,
                                    Toast.LENGTH_LONG);
                            waitingTime.show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(postRequest);
    }
}