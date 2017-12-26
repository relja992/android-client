package com.example.ljuba.trucks_client.activity;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.ljuba.trucks_client.R;
import com.example.ljuba.trucks_client.app.AppConfig;
import com.example.ljuba.trucks_client.app.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by BobanMihailov on 5.12.2017..
 */

public class Fragment_Podaci extends Fragment {
    private static final String TAG = Fragment_Podaci.class.getSimpleName();
    ProgressDialog pDialog;
    TextView tv_broj_pn,tv_vozac,tv_vozilo,tv_autobaza,tv_miv_poc,tv_miv_zav,tv_status_pn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_podaci,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Fragment Podaci");
        tv_broj_pn =  (TextView)getView().findViewById(R.id.tv_broj_putnog_naloga);
        tv_vozac = (TextView) getView().findViewById(R.id.tv_vozac);
        tv_vozilo = (TextView) getView().findViewById(R.id.tv_vozilo);
        tv_autobaza = (TextView) getView().findViewById(R.id.tv_autobaza);
        tv_miv_poc = (TextView) getView().findViewById(R.id.tv_miv_pocetka);
        tv_miv_zav = (TextView) getView().findViewById(R.id.tv_miv_zavrsetka);
        tv_status_pn = (TextView) getView().findViewById(R.id.tv_status_pn);
        Intent intent = getActivity().getIntent();
        String id_pn = intent.getStringExtra("id_pn");
        String id_vozaca = intent.getStringExtra("id_vozaca");
        // Toast.makeText(getActivity().getApplicationContext(), id_vozaca , Toast.LENGTH_SHORT).show();
        get_pn_details(id_pn,id_vozaca);

    }



    private void get_pn_details(final String id_pn, final String id_vozaca) {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
        String tag_string_req = "req_pn_details";

        pDialog.setMessage("Ucitavanje podataka ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_PN_DETAILS+"?id_pn="+id_pn+"&id_vozaca="+id_vozaca, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "PN Details Response: " + response.toString());
                hideDialog();

                JSONObject jObj = null;
                JSONArray pn_array = null;

                try {
                    jObj = new JSONObject(response);
                    pn_array = jObj.getJSONArray("pn_details");
                    JSONObject pn = pn_array.getJSONObject(0);
                    tv_broj_pn.setText("Broj PN: "+ pn.getString("broj_pn"));
                    tv_vozac.setText("Vozac: "+ pn.getString("broj_pn"));
                    String vozilo = pn.getString("vozilo");
                    JSONObject obj_vozilo = new JSONObject(vozilo);

                    tv_vozilo.setText("Vozilo: "+
                            obj_vozilo.getString("vrsta_vozila")
                            +" ("+obj_vozilo.getString("reg_oznala")+")"
                            +" Nosivost:"+ obj_vozilo.getString("nosivost_vozila"));
                    tv_autobaza.setText("Autobaza: "+pn.getString("autobaza"));
                    tv_miv_poc.setText("Mesto i vreme pocetka: "+pn.getString("lokacija_pocetka")+"-"+pn.getString("vreme_pocetka"));
                    tv_miv_zav.setText("Mesto i vreme zavrsetka: "+pn.getString("lokacija_zavrsetka")+"-"+pn.getString("vreme_zavrsetka"));
                    tv_status_pn.setText("Status: "+pn.getString("status_text"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "PN details Error: " + error.getMessage());
                Toast.makeText(getActivity().getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        });
        strReq.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }





}
