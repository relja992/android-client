package com.example.ljuba.trucks_client.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

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

    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;
    private Button btnLocation;

    private SQLiteHandler db;
    private SessionManager session;

    Double latitude, longitude;
    String user_id;

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLocation = (Button) findViewById(R.id.btnLocation);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
    }

    //Metoda koja implementira slanje lokacije na server
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
                            txtName.setText(latitude.toString() + " " + longitude.toString());
                            sendLocatinToServer(user_id,latitude.toString(),longitude.toString());
                            Toast.makeText(getApplicationContext(), "Uspesno poslati podaci o lokaciji", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }


    /**
     * slanje gps podataka na server nakon kilka na dugme
     * */
    private void sendLocatinToServer(final String user_id, final String latitude, final String longitude) {
        String tag_string_req = "req_login";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOCATION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Postovanje parametara na server za poziciju
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", user_id);
                params.put("latitude", latitude);
                params.put("longitude", longitude);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
}
