package com.tf.routerrecorder;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tf.routerrecorder.Adapters.ListAdapter;
import com.tf.routerrecorder.Services.ForegroundService;
import com.tf.routerrecorder.Utils.JsonHelper;
import com.tf.routerrecorder.Utils.RouteHelper;

import static com.tf.routerrecorder.Utils.Constants.*;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private MapView map;
    private IMapController mapController;
    private ArrayList<OverlayItem> items = new ArrayList<>();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private RouteHelper routeHelper;
    private ArrayList<String> datos = new ArrayList<>();
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
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE
        };
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
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mapController.setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));

        loadStops();
        createRecyclerView();
        setupMapLines();
        startService();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_add:
                loadJSON();
                return true;
            case R.id.action_clean:
                routeHelper.resetRoute();
                resetRoutes();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetRoutes(){
        items.clear();
        pathPoints.clear();
        datos.clear();
        adapter.notifyDataSetChanged();
        polyline = new Polyline();
        if (map.getOverlays().size() > 2) {
            map.getOverlays().remove(0);
            map.getOverlays().remove(1);
        }
    }

    ActivityResultLauncher<Intent> startActivityForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Uri data = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(data);
                        JsonHelper jsonHelper = new JsonHelper();
                        JSONObject routeData = jsonHelper.getJsonObject(inputStream);
                        JSONArray stops = routeData.getJSONArray(ROUTES);
                        routeHelper = new RouteHelper(stops);
                        resetRoutes();
                        displayStops();
                    }catch (IOException | JSONException e){
                        e.printStackTrace();
                    }
                }
            }
    );

    private void loadJSON(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult.launch(intent);
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
        //Toast.makeText(this, location.getProvider(), Toast.LENGTH_SHORT).show();
        if (map.getOverlays().size() > 2)
            map.getOverlays().remove(2);
        OverlayItem newItem = new OverlayItem("Parada #"+items.size()+1, "You are here", new GeoPoint(location));
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
        text += "- Time: " + dtf.format(now) + " ("+location.getProvider()+")";
        if (routeHelper.isStopClose(location.getLatitude(), location.getLongitude())) {
            Toast.makeText(this, "Estoy cerca de una parada", Toast.LENGTH_SHORT).show();
            text += "\n Parada cerca";
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
        JsonHelper jsonHelper = new JsonHelper(this);
        try {
            JSONObject routeData = jsonHelper.getJsonObjectFromPath(R.raw.test_coords);
            JSONArray stops = routeData.getJSONArray(ROUTES);
            routeHelper = new RouteHelper(stops);
            displayStops();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a layer to the mapview with the stops
     */
    private void displayStops() {
        if (map.getOverlays().size() > 0) {
            map.getOverlays().remove(0);
        }
        ArrayList<OverlayItem> stops = new ArrayList<>();
        ArrayList<List<Double>> stops_coords = routeHelper.getStopsCoords();
        for (List<Double> coords : stops_coords) {
            OverlayItem newItem = new OverlayItem(
                    "Here", "You are here",
                    new GeoPoint(coords.get(0), coords.get(1))
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
        map.getOverlays().add(0,myLocOverlay);
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
     * Setup polylines used to draw the route
     */
    private void setupMapLines() {
        polyline = new Polyline();
        pathPoints = new ArrayList<>();
        polyline.setPoints(pathPoints);
        map.getOverlays().add(polyline);
    }

    /**
     * Start a service so the app can update the location in the background
     */
    private void startService(){
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra(INTENT_NAME, "Foreground Service");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
}