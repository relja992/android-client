package com.example.ljuba.trucks_client.activity;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
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

    String user_id;
    private ProgressDialog pDialog;
    private List<PutniNalog> pn_list = new ArrayList<PutniNalog>();
    private ListView listView;
    private PNListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        //ucitavanje podataka iz intenta  neophodnih za ucitavanje  putnih naloga vozaca
        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        //Toast.makeText(getApplicationContext(),user_id,Toast.LENGTH_LONG).show();

        listView = (ListView) findViewById(R.id.lista_pn);
        adapter = new PNListAdapter(this, pn_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        pDialog = new ProgressDialog(this);
        // Showing progress dialog before making http request
        pDialog.setMessage("Ucitavanje putnih naloga...");
        pDialog.show();


        get_pn(user_id);


    }
    public void get_pn(final String driver_id){
        StringRequest movieReq = new StringRequest(AppConfig.URL_PN_LISTVIEW+"?id_vozaca="+driver_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response.toString());

                        JSONObject jObj = null;
                        JSONArray pn_array = null;

                        try {
                            jObj = new JSONObject(response);
                            pn_array = jObj.getJSONArray("putni_nalozi");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Parsing json
                        for (int i = 0; i < pn_array.length(); i++) {
                            try {
                                JSONObject pn = pn_array.getJSONObject(i);
                                JSONObject pn_detail = pn.getJSONObject("pn");

                                PutniNalog putniNalog = new PutniNalog();
                                putniNalog.setId_pn(pn_detail.getString("id_pn"));
                                putniNalog.setBroj_pn("Broj PN: "+pn_detail.getString("broj_pn")+"   Datum: "+pn_detail.getString("vreme_unosa"));
                                putniNalog.setVozac("Vozac: "+pn_detail.getString("vozac")+"   Vozilo: "+pn_detail.getString("vrsta_vozila")+" "+pn_detail.getString("reg_oznaka_vozila"));
                                String status_pn = null;
                                if (pn_detail.getString("status_pn").equals("2"))
                                    status_pn = "KREIRAN";
                                else if (pn_detail.getString("status_pn").equals("6"))
                                    status_pn = "PREKINUT";
                                else if (pn_detail.getString("status_pn").equals("5"))
                                    status_pn = "U IZVRSENJU";
                                else if (pn_detail.getString("status_pn").equals("8"))
                                    status_pn = "IZVRSEN";

                                putniNalog.setStatus_pn("Status putnog naloga: "+status_pn);
                                pn_list.add(putniNalog);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        hidePDialog();
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hidePDialog();

            }
        });
        movieReq.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(movieReq);
    }

//    public void get_pn(final String driver_id){
//        JsonArrayRequest movieReq = new JsonArrayRequest(AppConfig.URL_PN_LISTVIEW+"?id_vozaca="+driver_id,
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        Log.d(TAG, response.toString());
//                        hidePDialog();
//
//                        // Parsing json
//                        for (int i = 0; i < response.length(); i++) {
//                            try {
//
//                                JSONObject obj = response.getJSONObject(i);
//                                PutniNalog putniNalog = new PutniNalog();
//                                putniNalog.setId_pn(obj.getString("id_pn"));
//                                putniNalog.setBroj_pn(obj.getString("broj_pn"));
//                                putniNalog.setVozac(obj.getString("vozac"));
//                                putniNalog.setStatus_pn(obj.getString("status_pn"));
//
//                                pn_list.add(putniNalog);
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        adapter.notifyDataSetChanged();
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                VolleyLog.d(TAG, "Error: " + error.getMessage());
//                hidePDialog();
//
//            }
//        });
//        movieReq.setRetryPolicy(new DefaultRetryPolicy(5000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//        // Adding request to request queue
//        AppController.getInstance().addToRequestQueue(movieReq);
//    }


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
        //uzimanje vrednosti broj_pn jer ce trebati na sledecoj za ucitavanje detalja naloga
        String broj_putnog_naloga = ((TextView) view.findViewById(R.id.id_pn)).getText().toString();
       // Toast.makeText(getApplicationContext(), broj_putnog_naloga +i, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ListViewActivity.this,NavDraActivity.class);
        intent.putExtra("id_pn",broj_putnog_naloga);
        intent.putExtra("id_vozaca",user_id);
        startActivity(intent);
    }
}
