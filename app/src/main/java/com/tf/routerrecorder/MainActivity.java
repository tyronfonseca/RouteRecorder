package com.tf.routerrecorder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.tf.routerrecorder.Adapters.ListAdapter;
import com.tf.routerrecorder.Database.Entities.Agency;
import com.tf.routerrecorder.Database.Entities.RRData;
import com.tf.routerrecorder.Database.Entities.Route;
import com.tf.routerrecorder.Database.Entities.Trips;
import com.tf.routerrecorder.Database.Infrastructure.AppDatabase;
import com.tf.routerrecorder.Services.ForegroundService;
import com.tf.routerrecorder.Utils.DateTimePicker;
import com.tf.routerrecorder.Utils.GTFSExporter;
import com.tf.routerrecorder.Utils.GTFSLoader;
import com.tf.routerrecorder.Utils.JsonHelper;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private MapView map;
    private IMapController mapController;
    private final ArrayList<OverlayItem> items = new ArrayList<>();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private RouteHelper routeHelper;
    private final ArrayList<String> datos = new ArrayList<>();
    private ListAdapter adapter;

    private Polyline polyline;
    private ArrayList<GeoPoint> pathPoints;

    private AppDatabase db;
    private GTFSLoader gtfsLoader;

    // Use by the AlertDialog
    private AlertDialog dialog;
    private String route_id;
    private String agency_id;
    private List<String>  routes;
    private List<String>  agencies;
    private ArrayAdapter<String> routeAdapter;
    private ArrayAdapter<String> agencyAdapter;

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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mapController.setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));

        loadStops();
        createRecyclerView();
        setupMapLines();
        startService();

        createOrAccessDatabase();

        gtfsLoader = new GTFSLoader(getApplicationContext());
        setupDialog();
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
                loadGTFS();
                return true;
            case R.id.action_clean:
                routeHelper.resetRoute();
                resetRoutes();
                return true;
            case R.id.action_select:
                selectRoute();
                return true;
            case R.id.action_export:
                exportData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadGTFS(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        startActivityGTFS.launch(intent);
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

    ActivityResultLauncher<Intent> startActivityGTFS = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Uri data = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(data);
                        gtfsLoader.loadZip(inputStream);
                        //Update database
                        updateDatabase();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
    );

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
        GeoPoint currentLoc = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapController.setCenter(currentLoc);

        //Save in database
        RRData newRRData = new RRData();
        newRRData.unix_time = System.currentTimeMillis()/1000;
        newRRData.lat = location.getLatitude();
        newRRData.lon = location.getLongitude();
        newRRData.route_id = route_id;

        String text = newRRData.getSummary(location.getProvider());
        if (routeHelper.isStopClose(location.getLatitude(), location.getLongitude())) {
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
            Toast.makeText(this, "Estoy cerca de una parada", Toast.LENGTH_SHORT).show();
            text += "\n Parada cerca";
            //Is a stop
            newRRData.stop_id = "test";
        }
        db.rrDataDao().insertRRData(newRRData);

        //Update UI
        datos.add(text);
        adapter.notifyDataSetChanged();
        pathPoints.add(currentLoc);
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

    /**
     * Create a database using Room
     */
    private void createOrAccessDatabase(){
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "gtfsRouteRecorder")
                .allowMainThreadQueries().build();

        //TODO Remove
        String[] item = {"TFA","Transportes-Tyron","S","S","S","S","S","S"};
        db.agencyDao().insertAgency(new Agency(item));
    }

    /**
     * Load the data from the .txt extracted from the gtfs file (.zip)
     * and load it to the database.
     */
    private void updateDatabase(){
        ArrayList<Agency> agencies = gtfsLoader.getAgencies();
        ArrayList<Route> routes = gtfsLoader.getRoutes();
        ArrayList<Trips> trips = gtfsLoader.getTrips();

        //Save in the database
        db.agencyDao().insertAllAgencies(agencies);
        db.routeDao().insertAllRoutes(routes);
        db.tripsDao().insertAllTrips(trips);

    }

    /**
     * Create the AlertDialog to change the Stops
     */
    private void setupDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Seleccione la Agencia, luego la Ruta y presione Cargar paradas")
                .setTitle("Cambiar paradas");

        View layout = getLayoutInflater().inflate(R.layout.dialog_select_route, null);

        //Add custom view to dialog
        Spinner agencySpinner = layout.findViewById(R.id.spinner_agency);
        Spinner routeSpinner = layout.findViewById(R.id.spinner_route);

        // Select agency
        agencies = db.agencyDao().getIdAll();
        agencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, agencies);
        agencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        agencySpinner.setAdapter(agencyAdapter);
        agencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                agency_id = adapterView.getItemAtPosition(i).toString();
                //Repopulate the route list
                routes = db.routeDao().getRoutesIdByAgency(agency_id);
                routeAdapter.clear();
                for(String s : routes){
                    routeAdapter.add(s);
                }
                routeAdapter.notifyDataSetChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        // Select route
        agency_id = agencies.size() > 0 ? agencies.get(0) : "x";
        routes = db.routeDao().getRoutesIdByAgency(agency_id);
        routeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, routes);
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routeSpinner.setAdapter(routeAdapter);
        routeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                route_id = adapterView.getItemAtPosition(i).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        builder.setView(layout);

        //buttons
        builder.setPositiveButton("Cargar paradas", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO Cargar paradas segun el route_id
                Toast.makeText(getApplicationContext(), "Nuevas paradas cargadas",
                                Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.hide();
            }
        });

        dialog = builder.create();
    }

    /**
     * Display the Alertdialog to select new stops
     */
    private void selectRoute(){
        dialog.show();
    }

    /**
     * Export the data to a .txt saved in the Download directory
     */
    private void exportData(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Seleccione la fecha de inicio y la fecha de final")
                .setTitle("Exportar datos de GPS");
        View layout = getLayoutInflater().inflate(R.layout.dialog_export_data, null);

        Button start_btn = layout.findViewById(R.id.start_btn);
        Button end_btn = layout.findViewById(R.id.end_btn);

        long[] start_dt = {0};
        long[] end_dt = {0};
        DateTimePicker start_dt_picker = new DateTimePicker(MainActivity.this);
        DateTimePicker end_dt_picker = new DateTimePicker(MainActivity.this);

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start_dt_picker.getDatetime(start_btn);
            }
        });

        end_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                end_dt_picker.getDatetime(end_btn);
            }
        });
        builder.setView(layout);

        //buttons
        builder.setPositiveButton("Exportar datos", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                end_dt[0] = end_dt_picker.getEpochTime();
                start_dt[0] = start_dt_picker.getEpochTime();
                List<RRData> data = db.rrDataDao().getByRange(start_dt[0], end_dt[0]);
                GTFSExporter exporter = new GTFSExporter();
                exporter.exportRRData(data);
                Toast.makeText(getApplicationContext(),
                        "Datos exportados en la carpeta de Descargas",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
}