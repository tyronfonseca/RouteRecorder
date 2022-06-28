package com.tf.routerrecorder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tf.routerrecorder.Adapters.ListAdapter;
import com.tf.routerrecorder.Utils.RouteHelper;

import static com.tf.routerrecorder.Utils.Constants.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private MapView map;
    private IMapController mapController;
    private final ArrayList<OverlayItem> items = new ArrayList<>();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private RouteHelper routeHelper;
    private final ArrayList<String> datos = new ArrayList<>();
    private ListAdapter adapter;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Polyline polyline;
    private ArrayList<GeoPoint> pathPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Handle permissions
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        requestPermissionsIfNecessary(permissions);

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //inflate and create the map
        setContentView(R.layout.activity_main);

        //Load mapview
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        //Costa Rica's coords
        mapController = map.getController();
        mapController.setZoom(18.0);
        GeoPoint startPoint = new GeoPoint(9.934739, -84.087502);
        mapController.setCenter(startPoint);

        //Access the GPS
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mapController.setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));

        loadStops();
        createRecyclerView();
        setupMapLines();
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>(Arrays.asList(permissions).subList(0, grantResults.length));
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //Toast.makeText(this, location.getLatitude() +","+location.getLongitude(), Toast.LENGTH_SHORT).show();
        if (map.getOverlays().size() > 2)
            map.getOverlays().remove(2);
        OverlayItem newItem = new OverlayItem("Here", "You are here", new GeoPoint(location));
        items.add(newItem);
        ItemizedIconOverlay<OverlayItem> myLocOverlay = new ItemizedIconOverlay<>(items,
                getResources().getDrawable(org.osmdroid.library.R.drawable.marker_default, null),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                },
                this);
        map.getOverlays().add(myLocOverlay);
        GeoPoint currenLoc = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapController.setCenter(currenLoc);

        String text = location.getLatitude() + "," + location.getLongitude();
        LocalDateTime now = LocalDateTime.now();
        text += "- Time: " + dtf.format(now);
        if (routeHelper.isStopClose(location.getLatitude(), location.getLongitude())) {
            Toast.makeText(this, "Estoy cerca de una parada", Toast.LENGTH_SHORT).show();
            text += "\n Parada cerca";
            routeHelper.getNextStop();
        }
        datos.add(text);
        adapter.notifyDataSetChanged();
        pathPoints.add(currenLoc);
        polyline.setPoints(pathPoints);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    /**
     * Load the stops from a JSON file
     */
    private void loadStops() {
        InputStream inputStream = getResources().openRawResource(R.raw.test_coords);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONObject routeData = new JSONObject(byteArrayOutputStream.toString());
            JSONArray stops = routeData.getJSONArray(ROUTES);
            routeHelper = new RouteHelper(stops);
            displayStops();
            routeHelper.getNextStop();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Add a layer to the mapview with the stops
     */
    private void displayStops() {
        ArrayList<OverlayItem> stops = new ArrayList<>();
        ArrayList<Pair<Double, Double>> stops_coords = routeHelper.getStopsCoords();
        for (Pair<Double, Double> coords : stops_coords) {
            OverlayItem newItem = new OverlayItem(
                    "Here", "You are here",
                    new GeoPoint(coords.first, coords.second)
            );
            stops.add(newItem);
        }
        ItemizedIconOverlay<OverlayItem> myLocOverlay = new ItemizedIconOverlay<>(stops,
                getResources().getDrawable(org.osmdroid.library.R.drawable.marker_default_focused_base, null),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                },
                this);
        map.getOverlays().add(myLocOverlay);
    }

    /**
     * Create the recyclerView where the list of coords will be stored
     */
    private void createRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv);
        adapter = new ListAdapter(datos);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Setup polylines use to draw the route
     */
    private void setupMapLines() {
        polyline = new Polyline();
        pathPoints = new ArrayList<>();
        polyline.setPoints(pathPoints);
        map.getOverlays().add(polyline);
    }
}