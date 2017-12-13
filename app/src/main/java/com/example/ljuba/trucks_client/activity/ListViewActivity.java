package com.example.ljuba.trucks_client.activity;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.ljuba.trucks_client.R;
import com.example.ljuba.trucks_client.app.AppConfig;
import com.example.ljuba.trucks_client.app.AppController;
import com.example.ljuba.trucks_client.model.PNListAdapter;
import com.example.ljuba.trucks_client.model.PutniNalog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListViewActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    // Log tag
    private static final String TAG = ListViewActivity.class.getSimpleName();


    private ProgressDialog pDialog;
    private List<PutniNalog> pn_list = new ArrayList<PutniNalog>();
    private ListView listView;
    private PNListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        listView = (ListView) findViewById(R.id.lista_pn);
        adapter = new PNListAdapter(this, pn_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        pDialog = new ProgressDialog(this);
        // Showing progress dialog before making http request
        pDialog.setMessage("Ucitavanje putnih naloga...");
        pDialog.show();


        // Creating volley request obj
        JsonArrayRequest movieReq = new JsonArrayRequest(AppConfig.URL_TEST_LISTVIEW,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        hidePDialog();

                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject obj = response.getJSONObject(i);
                                PutniNalog putniNalog = new PutniNalog();
                                putniNalog.setBrojPN(obj.getString("title"));
                                putniNalog.setVozac(obj.getString("title"));
                                putniNalog.setVozilo(obj.getString("title"));
                                putniNalog.setStatus(obj.getString("title"));

                                pn_list.add(putniNalog);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hidePDialog();

            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(movieReq);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

       // String text = String.valueOf(adapterView.getItemAtPosition(i));
        Toast.makeText(getApplicationContext(), "Pozicija " +i, Toast.LENGTH_LONG)
                .show();
    }
}
