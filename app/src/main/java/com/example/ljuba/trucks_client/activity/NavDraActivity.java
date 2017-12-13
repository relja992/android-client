package com.example.ljuba.trucks_client.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.ljuba.trucks_client.R;
import com.example.ljuba.trucks_client.app.AppConfig;
import com.example.ljuba.trucks_client.app.AppController;
import com.example.ljuba.trucks_client.helper.SQLiteHandler;
import com.example.ljuba.trucks_client.helper.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavDraActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = NavDraActivity.class.getSimpleName();
    String my_user_id;
    public SQLiteHandler db;
    final Context context = this;
    FloatingActionButton start;
    FloatingActionButton stop;
    FloatingActionButton pause;
    private FusedLocationProviderClient mFusedLocationClient;
    boolean mRequestingLocationUpdates = false;
    private LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;

    public NavDraActivity() {
        start = null;
        stop = null;
        pause = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_dra);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        start = (FloatingActionButton) findViewById(R.id.start);
        stop = (FloatingActionButton) findViewById(R.id.stop);
        pause = (FloatingActionButton) findViewById(R.id.pause);

        db = new SQLiteHandler(getApplicationContext());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        displaySelectedScreen(R.id.nav_mapa);
        createLocationRequest();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {

                    Double dSirina = location.getLatitude();
                    String sirina = dSirina.toString();
                    Double dDuzina = location.getLongitude();
                    String duzina = dDuzina.toString();

                    sendLocation(duzina, sirina);

//                   Toast.makeText(getApplicationContext(), sirina + ' ' + duzina, Toast.LENGTH_LONG).show();
                }
            }
        };

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Izvrsavanje putnog naloga", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                start.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.VISIBLE);
                pause.setVisibility(View.VISIBLE);

                if (!mRequestingLocationUpdates)
                    startLocationUpdates();
                mRequestingLocationUpdates = true;

            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Prekini putni nalog", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                prikaziDialog();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Pauziraj putni nalog", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                prikaziDialog();
            }
        });


    }
    public void prikaziDialog(){
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Posalji",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                if (mRequestingLocationUpdates){
                                    stopLocationUpdates();
                                    mRequestingLocationUpdates = false;
                                }

                                Toast.makeText(getApplicationContext(), "Periodicno slanje prekinuto.", Toast.LENGTH_LONG).show();

                                stop.setVisibility(View.INVISIBLE);
                                pause.setVisibility(View.INVISIBLE);
                                start.setVisibility(View.VISIBLE);
                            }
                        })
                .setNegativeButton("Odustani",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {

                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();




    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_dra, menu);
        return true;
    }
    private void displaySelectedScreen(int id){
        Fragment fragment = null;
        switch (id){
            case R.id.nav_mapa:
                fragment = new Fragment_Mapa();
                break;
            case R.id.nav_detalji:
                fragment = new Fragment_Podaci();
                break;
            case R.id.nav_opterecenje:
                fragment = new Fragment_Opterecenje();
                break;
            case R.id.nav_tran_tacke:
                fragment = new Fragment_TransportneTacke();
                break;
            case R.id.nav_itinirer:
                fragment = new Fragment_Itinirer();
                break;
        }

        if (fragment!=null){
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_main,fragment);
            ft.commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        displaySelectedScreen(id);
        return true;
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        Toast.makeText(getApplicationContext(), "Periodicno uzimanje lokacije", Toast.LENGTH_LONG).show();
    }

    /**
     * Metoda koja implementira slanje sopstvene lokacije na server
     * */
    private void sendLocation(final String myLatitude, final String myLongitude) {

        HashMap<String, String> user = db.getUserDetails();
        my_user_id = user.get("uid");
        //////////////////////////////////////////////////////////////////////////////
        ///////////////////Koriscenje volley biblioteke///////////////////////////////
        //////////////////////////////////////////////////////////////////////////////

        // Tag used to cancel the request
        String tag_string_req = "req_location";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_SEND_LOCATION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Sending Location Response: " + response.toString());
               // hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        //Logovanje uspesno poslate lokacije u SQLite bazu podataka
                        db.logLocation(myLatitude, myLongitude, 1, 1, 1);

                        Toast.makeText(getApplicationContext(), "Uspesno poslata lokacija na server.", Toast.LENGTH_LONG).show();
                    } else {

                        // Error occurred in sending location. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Sending Location Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                     error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();

                db.logLocation(myLatitude, myLongitude, 1, 1, 0);

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", my_user_id);
                params.put("latitude", myLatitude);
                params.put("longitude", myLongitude);
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
