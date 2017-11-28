

package com.example.ljuba.trucks_client.activity;

import com.example.ljuba.trucks_client.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


public class MapsActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMyLocationButtonClickListener(this);


        //dodavanje linija
        PolylineOptions rectOptions = new PolylineOptions()
                .add(new LatLng(44.74, 20.48))
                .add(new LatLng(44.75, 20.49))
                .add(new LatLng(44.76, 20.50))
                .add(new LatLng(44.77, 20.51))
                .add(new LatLng(44.77, 20.52));

        mMap.addPolyline(rectOptions);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Kliknuto dugme za lokaciju", Toast.LENGTH_SHORT).show();
        return false;
    }








}