package com.example.ljuba.trucks_client.activity;

import com.example.ljuba.trucks_client.R;
import com.example.ljuba.trucks_client.helper.MapHelper.DirectionFinder;
import com.example.ljuba.trucks_client.helper.MapHelper.DirectionFinderListener;
import com.example.ljuba.trucks_client.helper.MapHelper.Route;
import com.example.ljuba.trucks_client.helper.SQLiteHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    private ProgressDialog progressDialog;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();

    private FusedLocationProviderClient mFusedLocationClient;

    int duration;
    double distance;

    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

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
        mMap.setMyLocationEnabled(true);

        sendRequest();
    }

    public void sendRequest() {

        //Umesto ovog ce ici POST request za dobijanje tacaka
        String[] waypoints = {"44.806255, 20.522842", "44.792045, 20.532508", "44.789483, 20.528277"};
        String destination = "44.796566, 20.522016";

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

                            Double originLat = location.getLatitude();
                            Double originLon = location.getLongitude();

                            // Inserting row in users table
                            db.logLocation(originLat.toString(), originLon.toString());

                        }
                    }
                });

        // Fetching user details from sqlite
        HashMap<String, String> location = db.getLastLocation();
        String origin = location.get("latitude") + ", " + location.get("longitude");

        try {
            new DirectionFinder(this, origin, waypoints, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Molim sacekajte.",
                "Pronalazenje rute..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            if(route.id == 0){

                duration += route.duration.value;
                distance += route.distance.value;

                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
//                        .title(route.startAddress)
                        .title("Trenutna lokacija")
                        .position(route.startLocation)));

                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLUE).
                        width(10);

                for (int i = 0; i < route.points.size(); i++)
                    polylineOptions.add(route.points.get(i));

                polylinePaths.add(mMap.addPolyline(polylineOptions));
            }else if(route.id == 1){

                duration += route.duration.value;
                distance += route.distance.value;

                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
//                        .title(route.startAddress)
                        .title("Mesto za utovar robe")
                        .position(route.startLocation)));

                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLUE).
                        width(10);

                for (int i = 0; i < route.points.size(); i++)
                    polylineOptions.add(route.points.get(i));

                polylinePaths.add(mMap.addPolyline(polylineOptions));
            }else if(route.id != routes.size()-1){

                duration += route.duration.value;
                distance += route.distance.value;

                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
//                        .title(route.startAddress)
                        .title("Stanica za istovar robe " + (route.id - 1))
                        .position(route.startLocation)));

                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLUE).
                        width(10);

                for (int i = 0; i < route.points.size(); i++)
                    polylineOptions.add(route.points.get(i));

                polylinePaths.add(mMap.addPolyline(polylineOptions));
            } else {

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 14));

                duration += route.duration.value;
                distance += route.distance.value;

                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
//                        .title(route.startAddress)
                        .title("Stanica za istovar robe " + (route.id - 1))
                        .position(route.startLocation)));

                destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
//                        .title(route.endAddress)
                        .title("Krajnja destinacija")
                        .position(route.endLocation)));

                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLUE).
                        width(10);

                for (int i = 0; i < route.points.size(); i++)
                    polylineOptions.add(route.points.get(i));

                polylinePaths.add(mMap.addPolyline(polylineOptions));
            }
        }

        DecimalFormat df = new DecimalFormat("#.00");
        String udaljenost = df.format(distance/1000) + " km";

        int minutes = duration/60;
        int seconds = duration%60;
        String trajanje = minutes + " m " + seconds + " s";

        ((TextView) findViewById(R.id.udaljenost)).append(udaljenost);
        ((TextView) findViewById(R.id.trajanje)).append(trajanje);
    }
}