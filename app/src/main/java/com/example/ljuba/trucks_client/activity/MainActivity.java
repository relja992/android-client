package com.example.ljuba.trucks_client.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.ljuba.trucks_client.R;
import com.example.ljuba.trucks_client.app.AppConfig;
import com.example.ljuba.trucks_client.app.AppController;
import com.example.ljuba.trucks_client.helper.SQLiteHandler;
import com.example.ljuba.trucks_client.helper.SessionManager;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;
    private Button btnLocation;

    private SQLiteHandler db;
    private SessionManager session;

    Double latitude, longitude;
    String user_id;

    private FusedLocationProviderClient mFusedLocationClient;

    private ProgressDialog pDialog;

    boolean mRequestingLocationUpdates = true;

    private LocationCallback mLocationCallback;

    LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLocation = (Button) findViewById(R.id.btnLocation);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        user_id = user.get("uid");
        String name = user.get("name");
        String email = user.get("email");

        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Pokretanje uzimanja lokacije i slanja na server
        btnLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendLocation();
            }
        });

        createLocationRequest();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...

                    Double sirina = location.getLatitude();
                    String ssirina = sirina.toString();
                    Double duzina = location.getLongitude();
                    String sduzina = duzina.toString();

                    //Toast.makeText(getApplicationContext(), "Uspesno poslata lokacija na server.", Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), ssirina + ' ' + sduzina, Toast.LENGTH_LONG).show();
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
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
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    /**
     * Metoda koja implementira slanje sopstvene lokacije na server
     * */
    private void sendLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            final String sirina = latitude.toString();
                            final String duzina = longitude.toString();

                            // Fetching user details from sqlite
                            HashMap<String, String> user = db.getUserDetails();

                            user_id = user.get("uid");

                            //////////////////////////////////////////////////////////////////////////////
                            ///////////////////Koriscenje volley biblioteke///////////////////////////////
                            //////////////////////////////////////////////////////////////////////////////

                            // Tag used to cancel the request
                            String tag_string_req = "req_location";

                            pDialog.setMessage("Sending Location ...");
                            showDialog();

                            StringRequest strReq = new StringRequest(Request.Method.POST,
                                    AppConfig.URL_SEND_LOCATION, new Response.Listener<String>() {

                                @Override
                                public void onResponse(String response) {
                                    Log.d(TAG, "Sending Location Response: " + response.toString());
                                    hideDialog();

                                    try {
                                        JSONObject jObj = new JSONObject(response);
                                        boolean error = jObj.getBoolean("error");
                                        if (!error) {

                                            //OVDE CE DA IDE UPISIVANJE U LOKALNU SQLite BAZU

//                                            // User successfully stored in MySQL
//                                            // Now store the user in sqlite
//                                            String uid = jObj.getString("uid");
//
//                                            JSONObject user = jObj.getJSONObject("user");
//                                            String name = user.getString("name");
//                                            String email = user.getString("email");
//                                            String created_at = user.getString("created_at");
//
//                                            // Inserting row in users table
//                                            db.addUser(name, email, uid, created_at);

                                            Toast.makeText(getApplicationContext(), "Uspesno poslata lokacija na server.", Toast.LENGTH_LONG).show();
                                        } else {

                                            // Error occurred in sending location. Get the error message
                                            String errorMsg = jObj.getString("error_msg");
                                            Toast.makeText(getApplicationContext(),
                                                    errorMsg, Toast.LENGTH_LONG).show();
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
                                    hideDialog();
                                }
                            }) {

                                @Override
                                protected Map<String, String> getParams() {
                                    // Posting params to register url
                                    Map<String, String> params = new HashMap<String, String>();

                                    params.put("user_id", user_id);
                                    params.put("latitude", sirina);
                                    params.put("longitude", duzina);

                                    return params;
                                }

                            };

                            // Adding request to request queue
                            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

                        }
                    }
                });
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
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
