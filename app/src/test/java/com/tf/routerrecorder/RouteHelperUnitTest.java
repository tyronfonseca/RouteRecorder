package com.tf.routerrecorder;

import static com.tf.routerrecorder.Utils.Constants.ROUTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tf.routerrecorder.Utils.JsonHelper;
import com.tf.routerrecorder.Utils.RouteHelper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Testing of methods of the class RouteHelper.
 */
public class RouteHelperUnitTest {

    private static JSONObject test_json_object = new JSONObject();
    private static JSONArray test_json_array = new JSONArray();

    @BeforeClass
    public static void setUp() {
        try {
            final JsonHelper jsonHelper = new JsonHelper();
            InputStream file = Objects.requireNonNull(RouteHelperUnitTest.class.getClassLoader())
                    .getResourceAsStream("test_coords.json");
            test_json_object = jsonHelper.getJsonObject(file);
            test_json_array = test_json_object.getJSONArray(ROUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void constructor_Works() {
        //arrange
        RouteHelper helper = new RouteHelper(test_json_array);

        //act
        ArrayList<List<Double>> coords = helper.getStopsCoords();

        //assert
        assertEquals(11, coords.size());
    }

    @Test
    public void getStopLat_Works() {
        //arr
        RouteHelper helper = new RouteHelper(test_json_array);
        ArrayList<List<Double>> coords = helper.getStopsCoords();

        //act
        double lat_valid = coords.get(0).get(0);
        double lat = helper.getStopLat();

        //assert
        assertEquals(lat_valid, lat, 0);
    }

    @Test
    public void getStopLon_Works() {
        //arr
        RouteHelper helper = new RouteHelper(test_json_array);
        ArrayList<List<Double>> coords = helper.getStopsCoords();

        //act
        double lon_valid = coords.get(0).get(1);
        double lon = helper.getStopLon();

        //assert
        assertEquals(lon_valid, lon, 0);
    }

    @Test
    public void getNextStop_Works() {
        //arr
        RouteHelper helper = new RouteHelper(test_json_array);
        ArrayList<List<Double>> coords = helper.getStopsCoords();

        //act
        helper.getNextStop();
        double lon_valid = coords.get(1).get(1);
        double lon = helper.getStopLon();
        double lat_valid = coords.get(1).get(0);
        double lat = helper.getStopLat();

        //assert
        assertEquals(lon_valid, lon, 0);
        assertEquals(lat_valid, lat, 0);
    }

    @Test
    public void getDistance_Works() {
        //arr
        RouteHelper helper = new RouteHelper(test_json_array);
        double expected = 34.975270023671094;
        double lat = 9.951903530297937;
        double lon = -84.04502645128804;

        //act
        double distance = helper.getDistance(lat, lon, helper.getStopLat(), helper.getStopLon());

        //assert
        assertEquals(expected, distance, 1.0);
    }

    @Test
    public void isStopClose_Works() {
        //arr
        RouteHelper helper = new RouteHelper(test_json_array);
        double lat = 9.951903530297937;
        double lon = -84.04502645128804;

        //act
        boolean isStopClose = helper.isStopClose(lat, lon);

        //assert
        assertTrue(isStopClose);
    }

    @Test
    public void isStopClose_False_Works() {
        //arr
        RouteHelper helper = new RouteHelper(test_json_array);
        double lat = 9.95151782034637;
        double lon = -84.04578551656628;

        //act
        boolean isStopClose = helper.isStopClose(lat, lon);

        //assert
        assertFalse(isStopClose);
    }

    //@TODO Test last coord
    public void isStopClose_LastCoord_Works() {
        //arr
        JSONArray json_array = null;
        try {
            final JsonHelper jsonHelper = new JsonHelper();
            InputStream file = Objects.requireNonNull(RouteHelperUnitTest.class.getClassLoader())
                    .getResourceAsStream("test_coord_small.json");
            final JSONObject json_object = jsonHelper.getJsonObject(file);
            json_array = json_object.getJSONArray(ROUTES);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RouteHelper helper = new RouteHelper(json_array);
        double lat_1 = 9.952187268722609;
        double lon_1 = -84.0451642749949;
        double lat_2 = 9.950050897975803;
        double lon_2 = -84.04390327366455;

        //act
        helper.isStopClose(lat_1, lon_1);
        boolean isStopClose = helper.isStopClose(lat_2, lon_2);

        //assert
        assertTrue(isStopClose);
    }
}