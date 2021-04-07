package com.wacked.smarttourist;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApiRequest;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Route {

    private final LatLng startpoint;
    private final LatLng endpoint;
    String[] places;

    public Route(LatLng startpoint, LatLng endpoint) {
        this.startpoint = startpoint;
        this.endpoint = endpoint;
    }

    public void setPlaces(String[] places) {
        this.places = places;
    }

    public DirectionsApiRequest.Waypoint[] GetWaypointsForRoute() {
        ArrayList<DirectionsApiRequest.Waypoint> result = new ArrayList<>();

        for (String place : places) {
            LatLng point = new LatLng(Double.parseDouble(place.split(",")[0]), Double.parseDouble(place.split(",")[1]));
            if (InArea(point)) {
                result.add(new DirectionsApiRequest.Waypoint(new com.google.maps.model.LatLng(Double.parseDouble(place.split(",")[0]), Double.parseDouble(place.split(",")[1]))));
            }
        }
        return result.toArray(new DirectionsApiRequest.Waypoint[0]);
    }

    public LatLng[] GetLatLngsForRoute() {
        ArrayList<LatLng> result = new ArrayList<>();

        for (String place : places) {
            LatLng point = new LatLng(Double.parseDouble(place.split(",")[0]), Double.parseDouble(place.split(",")[1]));
            if (InArea(point)) {
                result.add(point);
            }
        }

        return result.toArray(new LatLng[0]);
    }

    protected boolean InArea(@NotNull LatLng point) {
        LatLng SqA = startpoint;
        LatLng SqC = endpoint;
        return point.latitude <= SqC.latitude &
                point.latitude >= SqA.latitude &
                point.longitude <= SqC.longitude &
                point.longitude >= SqA.longitude;
    }

}

