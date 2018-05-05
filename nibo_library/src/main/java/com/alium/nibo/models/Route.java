package com.alium.nibo.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Abdul-Mujib Aliu on 4/3/2016.
 */
public class Route {
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
}
