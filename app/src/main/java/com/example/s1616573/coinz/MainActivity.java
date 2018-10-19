package com.example.s1616573.coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.gson.JsonElement;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.time.LocalDate;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, LocationEngineListener, DownloadCompleteListener {

    private String tag = "MainActivity";
    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private final String ACCESS_TOKEN = "pk.eyJ1IjoiY2Q2IiwiYSI6ImNqbXowNzYxMDE2bWcza3FsMXRpNG1xaGkifQ.-rbujzxJSMehxZ-v63eULA";
    private String downloadDate = ""; // Format: YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences
    private String geoJsonCoins;

    private DownloadFileTask downloadFileTask = new DownloadFileTask();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Mapbox.getInstance(this, ACCESS_TOKEN);
        mapView = findViewById(R.id.mapboxMapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

        // use "" as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "");
        geoJsonCoins = settings.getString("coinMap", "");
        Log.d(tag, "[onStart Recalled lastDownloadDate is '" + downloadDate + "'");

        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(tag, "[onStop] Storing lastDownloadDate of " + downloadDate);
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        // We need an Editor object to make preference changes.
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);
        editor.putString("coinMap", geoJsonCoins);
        // Apply the edits
        editor.apply();

        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if (mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapBox is null");
        } else {
            map = mapboxMap;
            // Set user interface options
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            // Make location information available
            enableLocation();
            getCoinMap();
        }
    }

    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Log.d(tag, "Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            Log.d(tag, "Permissions are not granted");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setInterval(5000); // preferably every 5 seconds
        locationEngine.setFastestInterval(1000); // at most every second
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer() {
        if (mapView == null) {
            Log.d(tag, "mapView is null");
        } else {
            if (map == null) {
                Log.d(tag, "map is null");
            } else {
                locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
            }
        }
    }

    private void setCameraPosition(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null");
        } else {
            Log.d(tag, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);
        }
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected()
    {
        Log.d(tag,"[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain)
    {
        Log.d(tag,"Permissions: " + permissionsToExplain.toString());
        // Present toast or dialog.
    }

    @Override
    public void onPermissionResult(boolean granted)
    {
        Log.d(tag,"[onPermissionResult] granted == " + granted);
        if (granted) {
            enableLocation();
        }
        else {
            // Open a dialogue with the user
        }
    }

    private void getCoinMap() {
        // download map if it has not already been downloaded
        LocalDate date = LocalDate.now();
        if (downloadDate.equals("") || !date.isEqual(LocalDate.parse(downloadDate))) {
            downloadDate = date.toString();
            String[] yearMonthDay = downloadDate.split("-");
            String mapURL = "http://homepages.inf.ed.ac.uk/stg/coinz/" + yearMonthDay[0] + "/" + yearMonthDay[1] + "/" + yearMonthDay[2] + "/coinzmap.geojson";

            downloadFileTask.listener = this;
            downloadFileTask.execute(mapURL);
        }
        else {
            // if the map has already been downloaded today, go straight to adding coins to the map
            addCoinsToMap();
        }
    }

    public void downloadComplete(String result) {
        // https://stackoverflow.com/questions/9963691/android-asynctask-sending-callbacks-to-ui
        if (result != null) {
            geoJsonCoins = result;
            addCoinsToMap();
        }
    }

    private void addCoinsToMap() {
        FeatureCollection featureCollection = FeatureCollection.fromJson(geoJsonCoins);
        List<Feature> features = featureCollection.features();

        for (Feature f : features) {
            if (f.geometry() instanceof Point) {
                LatLng coordinates = new LatLng(((Point) f.geometry()).latitude(), ((Point) f.geometry()).longitude());
                Log.d(tag,"[downloadComplete] coordinates == " + coordinates);
                JsonElement symbol = f.properties().get("marker-symbol");
                map.addMarker(new MarkerOptions().position(coordinates));

            }
        }
    }
}
