package com.example.mathijs.mathijsparmentierpset3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class dishActivity extends AppCompatActivity {

    String receivedDish = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish);

        // Get Dish
        Intent intent = getIntent();
        receivedDish = intent.getStringExtra("dish");

        // Set up App Bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle(receivedDish);


        // Instantiate Requestqueue
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://resto.mprog.nl/menu";

        // Request
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject myObject) {
                try {
                    JSONArray items = myObject.getJSONArray("items");
                    System.out.println(myObject.getJSONArray("items").toString());
                    for (int i = 0; i < items.length(); i++) {
                        if (items.getJSONObject(i).optString("name").equals(receivedDish)) {

                            TextView name = (TextView) findViewById(R.id.dishPrice);
                            name.setText(items.getJSONObject(i).optString("price"));

                            TextView description = (TextView) findViewById(R.id.dishDescription);
                            description.setText(items.getJSONObject(i).optString("description"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("ERROR 😡 " + error.toString());
            }
        });
        queue.add(jsObjRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbarmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.cart_button) {
            Intent intent = new Intent(this, cart.class);
            startActivity(intent);
            return true;
        }
        return true;
    }

    public void addToCart(View view) {

        SharedPreferences prefs = this.getSharedPreferences("orderList", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String received = prefs.getString("order", null);

        if (received == null) {
            // a. Order object doesn't exist yet
            JSONObject order = new JSONObject();
            try {
                order.put(receivedDish, 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            editor.putString("order", order.toString());
            editor.commit();


        } else {
            // b. Order object exists already
            try {
                JSONObject order = new JSONObject(received);

                if (order.has(receivedDish)) {
                    // Dish already exists in the order (add 1)
                    int originalAmount = order.getInt(receivedDish);
                    order.put(receivedDish, originalAmount + 1);

                } else {
                    // The dish didn't exist yet in the order (set 1)
                    order.put(receivedDish, 1);
                }

                editor.putString("order", order.toString());
                editor.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        Toast toast = Toast.makeText(this, "Dish added!", Toast.LENGTH_LONG);
        toast.show();
    }
}