package com.tf.routerrecorder.Utils;

import com.tf.routerrecorder.Database.Entities.Stops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RouteHelper {

    private List<Stops> stopsList;
    private int nextStopIndex;

    public RouteHelper(List<Stops> stops) {
        stopsList = stops;
        nextStopIndex = 0;
    }

    /**
     * Get stop latitude
     *
     * @return Latitude of the next stop
     */
    public double getStopLat() {
        return stopsList.get(nextStopIndex).lat;
    }

    /**
     * Get stop longitude
     *
     * @return Longitude of the next stop
     */
    public double getStopLon() {
        return stopsList.get(nextStopIndex).lon;
    }

    /**
     * Get the list of stops
     *
     * @return List of stops
     */
    public ArrayList<List<Double>> getStopsList() {
        ArrayList<List<Double>> coords = new ArrayList<>();
        for(Stops stop : stopsList){
            List<Double> coordPair = Arrays.asList(stop.lat, stop.lon);
            coords.add(coordPair);
        }
        return coords;
    }

    /**
     * Get next stop in the list of stops
     */
    public void getNextStop() {
        if (nextStopIndex < stopsList.size()) {
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
        if (nextStopIndex < stopsList.size()) {
            distance = getDistance(currLat, currLon, getStopLat(), getStopLon());
            final double radius = 50.0;

            isClose = distance <= radius;
            if (!isClose && nextStopIndex != 0 && nextStopIndex - 1 >= 0) {
                //Verify if we passed a stop
                double prvLat  = stopsList.get(nextStopIndex-1).lat;
                double prvLon = stopsList.get(nextStopIndex-1).lon;
                double prvStpDstc = getDistance(currLat, currLon, prvLat, prvLon);
                double distanceStops = getDistance(prvLat, prvLon, getStopLat(), getStopLon());
                if(prvStpDstc > distance && distanceStops < prvStpDstc){
                    isClose = true;
                }
            }
        }

        if(isClose){
            getNextStop();
        }

        return isClose;
    }

    /**
     * Reset the route count
     */
    public void resetRoute(){
        nextStopIndex = 0;
    }
}
