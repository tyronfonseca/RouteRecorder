package com.tf.routerrecorder;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.provider.Settings;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tf.routerrecorder.adapters.ListAdapter;
import com.tf.routerrecorder.database.entities.Agency;
import com.tf.routerrecorder.database.entities.RRData;
import com.tf.routerrecorder.database.entities.Route;
import com.tf.routerrecorder.database.entities.Stops;
import com.tf.routerrecorder.database.entities.StopsRoutes;
import com.tf.routerrecorder.database.entities.Trips;
import com.tf.routerrecorder.database.infrastructure.AppDatabase;
import com.tf.routerrecorder.services.ForegroundService;
import com.tf.routerrecorder.utils.DateTimePicker;
import com.tf.routerrecorder.utils.GTFSExporter;
import com.tf.routerrecorder.utils.GTFSLoader;
import com.tf.routerrecorder.utils.RouteHelper;
import static com.tf.routerrecorder.utils.Constants.*;

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
    FloatingActionButton stopRecord;
    private boolean isRecording = false;
    private boolean hasAllPermissions = false;

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
        if(!hasPermissions()){
            openSettingsScreen();
        }else{
            setupApp();
        }

        stopRecord = findViewById(R.id.stop_fab);
        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecord.hide();
                isRecording = false;
            }
        });

    }

    /**
     * Display a message to open the app's settings.
     */
    private void openSettingsScreen(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("La app no tiene todos los permisos")
                .setMessage("Dale todos los permisos a la app\n"+
                        "- Ubicación: Permitir todo el tiempo\n"+
                        "- Archivos y multimedia: Solo permitir acceso a contenido multimedia")
                .setNegativeButton("Cerrar App", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.finish();
                    }
                })
                .setPositiveButton("Abrir Configuración", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });
        builder.show();
    }

    @SuppressLint("MissingPermission")
    private void setupApp(){
        //Access the GPS
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 8000, 10, this);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 8000, 10, this);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mapController.setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));

        createOrAccessDatabase();

        createRecyclerView();
        startService();

        gtfsLoader = new GTFSLoader(getApplicationContext());
        setupDialog();

        hasAllPermissions = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
        if(hasPermissions()){
            setupApp();
        }else{
            hasAllPermissions = false;
        }
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
        if(hasAllPermissions) {
            switch (item.getItemId()) {
                case R.id.action_add:
                    loadGTFS();
                    break;
                case R.id.action_clean:
                    if (routeHelper != null)
                        routeHelper.resetRoute();
                    resetRoutes();
                    break;
                case R.id.action_select:
                    selectRoute();
                    break;
                case R.id.action_export:
                    exportData();
                    break;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }else{
            openSettingsScreen();
        }
        return true;
    }

    /**
     * Open the GTFS.zip file
     */
    private void loadGTFS(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        startActivityGTFS.launch(intent);
    }

    /**
     * Reset all the routes and the map
     */
    private void resetRoutes(){
        items.clear();
        pathPoints.clear();
        datos.clear();
        adapter.notifyDataSetChanged();
        polyline = new Polyline();
        if (map.getOverlays().size() > 2) {
            map.getOverlays().remove(0);//Stops
            map.getOverlays().remove(1);//Lines
        }
        map.invalidate();
        Toast.makeText(this, "Reset", Toast.LENGTH_SHORT).show();
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
        super.onRequestPermissionsResult(requestCode, permissions,grantResults);
    }

    /**
     * Verify if the app has all the permissions
     * @return False at least one permission is missing True all granted
     */
    private boolean hasPermissions() {
        //Handle permissions
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(MainActivity.this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(isRecording) {
            GeoPoint currentLoc = new GeoPoint(location.getLatitude(), location.getLongitude());
            mapController.setCenter(currentLoc);

            //Save in database
            RRData newRRData = new RRData();
            newRRData.unix_time = System.currentTimeMillis() / 1000;
            newRRData.lat = location.getLatitude();
            newRRData.lon = location.getLongitude();
            newRRData.route_id = route_id;

            String text = newRRData.getSummary(location.getProvider());
            if (routeHelper.isStopClose(location.getLatitude(), location.getLongitude())) {
                if (map.getOverlays().size() > 2)
                    map.getOverlays().remove(2);
                OverlayItem newItem = new OverlayItem("Parada #" + items.size() + 1, "You are here", new GeoPoint(location));
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
                newRRData.stop_id = routeHelper.getLastStopId();
            }

            //Update UI
            datos.add(text);
            adapter.notifyDataSetChanged();
            pathPoints.add(currentLoc);
            polyline.setPoints(pathPoints);

            db.rrDataDao().insertRRData(newRRData);
        }
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
     * Load the stops from the database
     */
    private void loadStops() {
        List<Stops> stops = db.stopsDao().getAllByRouteId(route_id);
        routeHelper = new RouteHelper(stops);
        displayStops();
        setupMapLines();
    }

    /**
     * Add a layer to the mapview with the stops
     */
    private void displayStops() {
        if (map.getOverlays().size() > 0) {
            map.getOverlays().remove(0);
        }
        ArrayList<OverlayItem> stops = new ArrayList<>();
        ArrayList<List<Double>> stops_coords = routeHelper.getStopsList();
        for (List<Double> coords : stops_coords) {
            OverlayItem newItem = new OverlayItem(
                    "Here", "You are here",
                    new GeoPoint(coords.get(0), coords.get(1))
            );
            stops.add(newItem);
        }
        ItemizedIconOverlay<OverlayItem> myLocOverlay = new ItemizedIconOverlay<>(stops,
                getResources().getDrawable(org.osmdroid.library.R.drawable.marker_default, null),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        Toast.makeText(MainActivity.this, "Parada #"+index, Toast.LENGTH_SHORT).show();
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
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries().build();
    }

    /**
     * Load the data from the .txt extracted from the gtfs file (.zip)
     * and load it to the database.
     */
    private void updateDatabase(){
        ArrayList<Agency> agencies = gtfsLoader.getAgencies();
        ArrayList<Route> routes = gtfsLoader.getRoutes();
        ArrayList<Trips> trips = gtfsLoader.getTrips();
        ArrayList<Stops> stops = gtfsLoader.getStops();
        ArrayList<StopsRoutes> stopsRoutes = gtfsLoader.getStopsRoutes();

        //Save in the database
        db.agencyDao().insertAllAgencies(agencies);
        db.routeDao().insertAllRoutes(routes);
        db.tripsDao().insertAllTrips(trips);
        db.stopsDao().insertAllStops(stops);
        db.stopsRoutesDao().insertAllStopsRoutes(stopsRoutes);

        //Recreate dialog;
        setupDialog();
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
                loadStops();
                Toast.makeText(getApplicationContext(), "Nuevas paradas cargadas",
                                Toast.LENGTH_SHORT).show();
                isRecording = true;
                stopRecord.show();
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