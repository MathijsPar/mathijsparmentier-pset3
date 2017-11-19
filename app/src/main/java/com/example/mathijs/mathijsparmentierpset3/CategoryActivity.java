package com.example.mathijs.mathijsparmentierpset3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

public class CategoryActivity extends AppCompatActivity {

    List<String> categories = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Category
        Intent intent = getIntent();
        final String receivedCategory = intent.getStringExtra("category");
        System.out.println(receivedCategory);

        // Set up App Bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle(receivedCategory);

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
                    for (int i = 0; i < items.length(); i++) {
                        if(items.getJSONObject(i).optString("category").equals(receivedCategory)) {
                            categories.add(items.getJSONObject(i).optString("name"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Set up adapter
                ArrayAdapter<String> adapter =
                        new ArrayAdapter<String>(
                                CategoryActivity.this,
                                android.R.layout.simple_list_item_1,
                                categories);
                ListView list = (ListView) findViewById(R.id.categorylist);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent dishIntent = new Intent(view.getContext(), dishActivity.class);
                        System.out.println("testtes12  " + String.valueOf(adapterView.getItemAtPosition(i)));
                        dishIntent.putExtra("dish", String.valueOf(adapterView.getItemAtPosition(i)));
                        startActivity(dishIntent);
                    }
                });
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("ERROR 😡 " + error.toString());
            }
        }
        );

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
}