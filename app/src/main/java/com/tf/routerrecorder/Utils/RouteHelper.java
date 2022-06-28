package com.tf.routerrecorder.Utils;


import static com.tf.routerrecorder.Utils.Constants.*;

import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RouteHelper {

    private final ArrayList<Pair<Double, Double>> stopsCoords;
    private int nextStopIndex;
    private double prevLat = 0.0;
    private double prevLon = 0.0;

    public RouteHelper(JSONArray stops) {
        stopsCoords = new ArrayList<>();
        nextStopIndex = -1;
        try {
            for (int i = 0; i < stops.length(); i++) {
                JSONObject stop = stops.getJSONObject(i);
                stopsCoords.add(
                        new Pair<>(stop.getDouble(LATITUDE), stop.getDouble(LONGITUDE))
                );
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
        return stopsCoords.get(nextStopIndex).first;
    }

    /**
     * Get stop longitude
     *
     * @return Longitude of the next stop
     */
    public double getStopLon() {
        return stopsCoords.get(nextStopIndex).second;
    }

    /**
     * Get the list of stops
     *
     * @return List of stops
     */
    public ArrayList<Pair<Double, Double>> getStopsCoords() {
        return stopsCoords;
    }

    /**
     * Get next stop in the list of stops
     */
    public void getNextStop() {
        if (nextStopIndex < stopsCoords.size()) {
            if (nextStopIndex >= 0) {
                prevLat = getStopLat();
                prevLon = getStopLon();
            }
            nextStopIndex++;
        }
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
        if (nextStopIndex < stopsCoords.size()) {
            final double lat2 = getStopLat();
            final double lon2 = getStopLon();

            final double piRadian = Math.PI / 180;
            final double earthRadius = 6371e3;
            final double radius = 50.0;

            final double phi1 = currLat * piRadian;
            final double phi2 = lat2 * piRadian;
            final double deltaPhi = (lat2 - currLat) * piRadian;
            final double deltaLambda = (lon2 - currLon) * piRadian;
            final double a = Math.pow(Math.sin(deltaPhi / 2), 2.0)
                    + Math.cos(phi1) * Math.cos(phi2) * Math.pow(Math.sin(deltaLambda / 2), 2.0);
            final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            final double distance = earthRadius * c;

            isClose = distance <= radius;

            if (!isClose) {
                //Verify if we passed a stop
                isClose = hasPassedAStop(currLat, currLon);
            }
        }

        return isClose;
    }

    /**
     * Verify if the current location is inside a rectangle given by
     * the next stop and the next next stop.
     *
     * @param currLat Current latitude of the user
     * @param currLon Current longitude of the user
     * @return True is in the rectangle, False otherwise
     */
    public boolean hasPassedAStop(double currLat, double currLon) {
        boolean stopPassed;
        double nextLat;
        double nextLon;
        double midLat = currLat;
        double midLon = currLon;
        double stopLat = getStopLat();
        double stopLon = getStopLon();
        if (nextStopIndex + 1 <= stopsCoords.size() - 1) {
            nextLat = stopsCoords.get(nextStopIndex + 1).first;
            nextLon = stopsCoords.get(nextStopIndex + 1).second;
        } else {
            //There is only one stop left in the list
            nextLat = currLat;
            nextLon = currLon;
            midLat = stopLat;
            midLon = stopLon;
            stopLat = prevLat;
            stopLon = prevLon;
        }
        final double minLat = Math.min(stopLat, nextLat);
        final double maxLat = Math.max(stopLat, nextLat);
        final double minLon = Math.min(stopLon, nextLon);
        final double maxLon = Math.max(stopLon, nextLon);

        stopPassed = (minLat < midLat && midLat < maxLat) &&
                (minLon < midLon && midLon < maxLon);
        return stopPassed;
    }
}
