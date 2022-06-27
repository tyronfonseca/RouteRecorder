package com.tf.routerrecorder.Utils;

import android.location.Location;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RouteHelper {

    private ArrayList<Pair<Double, Double>> stopsCoords;
    private int nextStopIndex;
    private final double earthRadius = 6371e3;
    private final double piRadian = Math.PI / 180;
    private final double radius = 50.0;

    public RouteHelper(JSONArray stops) {
        stopsCoords = new ArrayList<>();
        nextStopIndex = -1;
        try {
            for (int i = 0; i < stops.length(); i++) {
                JSONObject stop = stops.getJSONObject(i);
                stopsCoords.add(
                        new Pair<>(stop.getDouble("lat"), stop.getDouble("lon"))
                );
            }
        } catch (Exception e) {
            Log.e("TF", "Error when contructing stops object");
        }
    }

    public ArrayList<Pair<Double, Double>> getStopsCoords() {
        return stopsCoords;
    }

    /**
     * Get next stop in the list of stops
     */
    public void getNextStop() {
        stopsCoords.get(nextStopIndex++);
    }

    /**
     * Verify if the user is close or passed the stop.
     * Formula taken from:
     * https://www.movable-type.co.uk/scripts/latlong.html
     *
     * @param currLat Current latitude of the user
     * @param currLon Current longitude of the user
     * @return True is in the radius False otherwise
     */
    public boolean isStopClose(double currLat, double currLon) {
        boolean isClose = false;
        final double lat1 = currLat;
        final double lon1 = currLon;
        final double lat2 = stopsCoords.get(nextStopIndex).first;
        final double lon2 = stopsCoords.get(nextStopIndex).second;

        final double phi1 = lat1 * piRadian;
        final double phi2 = lat2 * piRadian;
        final double deltaPhi = (lat2 - lat1) * piRadian;
        final double deltaLambda = (lon2 - lon1) * piRadian;
        final double a = Math.pow(Math.sin(deltaPhi / 2), 2.0)
                + Math.cos(phi1) * Math.cos(phi2) * Math.pow(Math.sin(deltaLambda / 2), 2.0);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        final double distance = earthRadius * c;

        isClose = distance <= radius;

        return isClose;
    }
}
