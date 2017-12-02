package com.example.ljuba.trucks_client.helper.MapHelper;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Ljuba on 2.12.2017..
 */

public class Route {

    public int id;
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;

}
