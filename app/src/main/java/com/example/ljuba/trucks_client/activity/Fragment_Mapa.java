package com.example.ljuba.trucks_client.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ljuba.trucks_client.R;
import com.example.ljuba.trucks_client.helper.MapHelper.DirectionFinder;
import com.example.ljuba.trucks_client.helper.MapHelper.DirectionFinderListener;
import com.example.ljuba.trucks_client.helper.MapHelper.Route;
import com.example.ljuba.trucks_client.helper.SQLiteHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Fragment_Mapa extends Fragment implements DirectionFinderListener {

    private ProgressDialog progressDialog;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();

    private FusedLocationProviderClient mFusedLocationClient;

    int duration;
    double distance;

    private SQLiteHandler db;

    MapView mMapView;
    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle  savedInstanceState){

        View rootView = inflater.inflate(R.layout.fragment_mapa, container, false);
        getActivity().setTitle("Fragment Mapa");
        mMapView= (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // SQLite database handler
        db = new SQLiteHandler(getActivity());

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                if (checkLocationPermission()) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                    }
                }

                sendInitialRequestForRoute();

                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setRotateGesturesEnabled(true);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("")
                        .setMessage("")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(getActivity(),new String[]
                                        {Manifest.permission.ACCESS_FINE_LOCATION},1);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
            {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                    }
                } else {
                }
                return;
            }
        }
    }

    //Inicijalno slanje zahteva za pronalazenje najkrace rute ka google api-ju. Ruta kojom vozilo treba da se krece.
    public void sendInitialRequestForRoute() {

        //Umesto ovog ce ici POST request za dobijanje tacaka
        String origin = "44.801264, 20.521455";
        String[] waypoints = {"44.806255, 20.522842", "44.792045, 20.532508", "44.789483, 20.528277"};
        String destination = "44.796566, 20.522016";

        // Fetching user details from sqlite
//        HashMap<String, String> location = db.getLastLocation();
//        String origin = location.get("latitude") + ", " + location.get("longitude");

        try {
            new DirectionFinder(this, origin, waypoints, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    //Slanje zahteva za pronalazenje najkrace rute ka google api-ju
    public void sendRequestForRealRoute(String sirina, String duzina) {

        ArrayList<String[]> locations = db.getLocationsForRealRoute();
        String[] waypoints = new String[locations.size()-1];

        String[] originStringArray = locations.get(0);
        String origin = originStringArray[1] + ", " + originStringArray[2];
        db.setLocationUsed(originStringArray[0]);

        String destination = sirina + ", " + duzina;

        if(locations.size() >= 2){
            for(int i = 1; i < locations.size(); i++){

                String[] waypoint = locations.get(i);
                waypoints[i-1] = waypoint[1] + ", " + waypoint[2];

                db.setLocationUsed(waypoint[0]);

            }
        }

        try {
            new DirectionFinder(this, origin, waypoints, destination).executeRealRouteRequest();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(getActivity(), "Molim sacekajte.",
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
    public void onDirectionFinderStartWithoutCleaning() {
        progressDialog = ProgressDialog.show(getActivity(), "Molim sacekajte.",
                "Pronalazenje rute..!", true);

//        if (originMarkers != null) {
//            for (Marker marker : originMarkers) {
//                marker.remove();
//            }
//        }
//
//        if (destinationMarkers != null) {
//            for (Marker marker : destinationMarkers) {
//                marker.remove();
//            }
//        }
//
//        if (polylinePaths != null) {
//            for (Polyline polyline:polylinePaths ) {
//                polyline.remove();
//            }
//        }
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

                originMarkers.add(googleMap.addMarker(new MarkerOptions()
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

                polylinePaths.add(googleMap.addPolyline(polylineOptions));
            }else if(route.id == 1){

                duration += route.duration.value;
                distance += route.distance.value;

                originMarkers.add(googleMap.addMarker(new MarkerOptions()
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

                polylinePaths.add(googleMap.addPolyline(polylineOptions));
            }else if(route.id != routes.size()-1){

                duration += route.duration.value;
                distance += route.distance.value;

                originMarkers.add(googleMap.addMarker(new MarkerOptions()
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

                polylinePaths.add(googleMap.addPolyline(polylineOptions));
            } else {

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 14));

                duration += route.duration.value;
                distance += route.distance.value;

                originMarkers.add(googleMap.addMarker(new MarkerOptions()
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
//                        .title(route.startAddress)
                        .title("Stanica za istovar robe " + (route.id - 1))
                        .position(route.startLocation)));

                destinationMarkers.add(googleMap.addMarker(new MarkerOptions()
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

                polylinePaths.add(googleMap.addPolyline(polylineOptions));
            }
        }

        DecimalFormat df = new DecimalFormat("#.00");
        String udaljenost = df.format(distance/1000) + " km";

        int minutes = duration/60;
        int seconds = duration%60;
        String trajanje = minutes + " m " + seconds + " s";

        //((TextView) getActivity().findViewById(R.id.udaljenost_fragment)).append(udaljenost);
        //((TextView) getActivity().findViewById(R.id.trajanje_fragment)).append(trajanje);
    }

    @Override
    public void onDirectionFinderSuccessWithoutCleaning(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            if(route.id == 0){

                duration += route.duration.value;
                distance += route.distance.value;

//                originMarkers.add(googleMap.addMarker(new MarkerOptions()
//                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
////                        .title(route.startAddress)
//                        .title("Trenutna lokacija")
//                        .position(route.startLocation)));

                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.RED).
                        width(10);

                for (int i = 0; i < route.points.size(); i++)
                    polylineOptions.add(route.points.get(i));

                polylinePaths.add(googleMap.addPolyline(polylineOptions));
            }else if(route.id == 1){

                duration += route.duration.value;
                distance += route.distance.value;

//                originMarkers.add(googleMap.addMarker(new MarkerOptions()
//                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
////                        .title(route.startAddress)
//                        .title("Mesto za utovar robe")
//                        .position(route.startLocation)));

                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.RED).
                        width(10);

                for (int i = 0; i < route.points.size(); i++)
                    polylineOptions.add(route.points.get(i));

                polylinePaths.add(googleMap.addPolyline(polylineOptions));
            }else if(route.id != routes.size()-1){

                duration += route.duration.value;
                distance += route.distance.value;

//                originMarkers.add(googleMap.addMarker(new MarkerOptions()
//                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
////                        .title(route.startAddress)
//                        .title("Stanica za istovar robe " + (route.id - 1))
//                        .position(route.startLocation)));

                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.RED).
                        width(10);

                for (int i = 0; i < route.points.size(); i++)
                    polylineOptions.add(route.points.get(i));

                polylinePaths.add(googleMap.addPolyline(polylineOptions));
            } else {

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 14));

                duration += route.duration.value;
                distance += route.distance.value;

//                originMarkers.add(googleMap.addMarker(new MarkerOptions()
//                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
////                        .title(route.startAddress)
//                        .title("Stanica za istovar robe " + (route.id - 1))
//                        .position(route.startLocation)));

//                destinationMarkers.add(googleMap.addMarker(new MarkerOptions()
//                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
////                        .title(route.endAddress)
//                        .title("Krajnja destinacija")
//                        .position(route.endLocation)));

                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.RED).
                        width(10);

                for (int i = 0; i < route.points.size(); i++)
                    polylineOptions.add(route.points.get(i));

                polylinePaths.add(googleMap.addPolyline(polylineOptions));
            }
        }

        DecimalFormat df = new DecimalFormat("#.00");
        String udaljenost = df.format(distance/1000) + " km";

        int minutes = duration/60;
        int seconds = duration%60;
        String trajanje = minutes + " m " + seconds + " s";

        //((TextView) getActivity().findViewById(R.id.udaljenost_fragment)).append(udaljenost);
        //((TextView) getActivity().findViewById(R.id.trajanje_fragment)).append(trajanje);
    }

}