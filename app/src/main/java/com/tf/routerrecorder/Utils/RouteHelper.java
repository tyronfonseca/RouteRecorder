package com.tf.routerrecorder.Utils;


import static com.tf.routerrecorder.Utils.Constants.*;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RouteHelper {

    private ArrayList<List<Double>> stopsCoords;
    private int nextStopIndex;
    private double oldDistance = -1.0;

    public RouteHelper(JSONArray stops) {
        stopsCoords = new ArrayList<>();
        nextStopIndex = 0;
        try {
            for (int i = 0; i < stops.length(); i++) {
                JSONObject stop = stops.getJSONObject(i);
                double lat = stop.getDouble(LATITUDE);
                double lon = stop.getDouble(LONGITUDE);
                List<Double> pair = new ArrayList<>();
                pair.add(lat);
                pair.add(lon);
                stopsCoords.add(pair);
            }
        } catch (Exception e) {
            Log.e(DEBUG_TAG, ERROR_JSON_CONS);
        }
    }

    /**
     * Get stop latitude
     *
     * @return Latitude of the next stop
     */
    public double getStopLat() {
        return stopsCoords.get(nextStopIndex).get(0);
    }

    /**
     * Get stop longitude
     *
     * @return Longitude of the next stop
     */
    public double getStopLon() {
        return stopsCoords.get(nextStopIndex).get(1);
    }

    /**
     * Get the list of stops
     *
     * @return List of stops
     */
    public ArrayList<List<Double>> getStopsCoords() {
        return stopsCoords;
    }

    /**
     * Get next stop in the list of stops
     */
    public void getNextStop() {
        if (nextStopIndex < stopsCoords.size()) {
            nextStopIndex++;
        }
    }

    /**
     * Get the distance between a given pair of coordinates
     * Formula taken from:
     * https://www.movable-type.co.uk/scripts/latlong.html
     *
     * @param currLat Current latitude of the user
     * @param currLon Current longitude of the user
     * @return Distance in meters
     */
    public double getDistance(double currLat, double currLon, double lat2, double lon2) {
        double distance;
        final double piRadian = Math.PI / 180;
        final double earthRadius = 6371e3;

        final double phi1 = currLat * piRadian;
        final double phi2 = lat2 * piRadian;
        final double deltaPhi = (lat2 - currLat) * piRadian;
        final double deltaLambda = (lon2 - currLon) * piRadian;
        final double a = Math.pow(Math.sin(deltaPhi / 2), 2.0)
                + Math.cos(phi1) * Math.cos(phi2) * Math.pow(Math.sin(deltaLambda / 2), 2.0);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        distance = earthRadius * c;

        return distance;
    }

    /**
     * Verify if the user is close or passed the stop.
     *
     * @param currLat Current latitude of the user
     * @param currLon Current longitude of the user
     * @return True is in the radius False otherwise
     */
    public boolean isStopClose(double currLat, double currLon) {
        boolean isClose = false;
        double distance = 0;
        if (nextStopIndex < stopsCoords.size()) {
            distance = getDistance(currLat, currLon, getStopLat(), getStopLon());
            final double radius = 50.0;

            isClose = distance <= radius;
            if (!isClose && distance > oldDistance) {
                //Verify if we passed a stop
                if (oldDistance >= 0) {
                    oldDistance = distance;
                    isClose = true;
                }
            }
        }

        if(isClose){
            getNextStop();
        }

        return isClose;
    }
}
