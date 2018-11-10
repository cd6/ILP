package com.example.s1616573.coinz;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.gson.JsonElement;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;

import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
    private FirebaseAuth mAuth;
    private DownloadFileTask downloadFileTask = new DownloadFileTask();
    private HashMap<Marker, Coin> coinMap;
    private UserFirestore userFirestore;
    private boolean done = false;
    private ArrayList<String> pickedUpCoins;

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

        FloatingActionButton mCoinButton = findViewById(R.id.fab_coin);
        mCoinButton.setOnClickListener(view -> {

        });

        FloatingActionButton mBankButton = findViewById(R.id.fab_bank);
        mBankButton.setOnClickListener(view -> {
            // open bank
            Intent bankIntent = new Intent(this, BankActivity.class);
            startActivity(bankIntent);
        });

        FloatingActionButton mWalletButton = findViewById(R.id.fab_wallet);
        mWalletButton.setOnClickListener(view -> {
            // open wallet
            Intent walletIntent = new Intent(this, WalletActivity.class);
            startActivity(walletIntent);
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (locationEngine != null && !locationEngine.isConnected()) {
            locationEngine.activate();
        }
        if (locationLayerPlugin != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationLayerPlugin.onStart();
        }
        mAuth = FirebaseAuth.getInstance();
        userFirestore = new UserFirestore(mAuth);
        userFirestore.listener = this;
        // check if this is the first time the user has logged in today
        userFirestore.checkFirstLoginToday();

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

        if (locationEngine != null) locationEngine.deactivate(); // stops app crashing when activity is stopped due to location being checked after map has been closed
        mapView.onStop();
        if (locationLayerPlugin != null) locationLayerPlugin.onStop();
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
            // Don't load coins onto map until map is ready
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
            inRangeOfCoin();
        }
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.d(tag,"[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Log.d(tag,"Permissions: " + permissionsToExplain.toString());
        // Present toast or dialog.
        Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), permissionsToExplain.toString(), Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        Log.d(tag,"[onPermissionResult] granted == " + granted);
        if (granted) {
            enableLocation();
        }
        else {
            // Open a dialogue with the user
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sign_out_item) {
            mAuth.signOut();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            this.finish();
        }

        return super.onOptionsItemSelected(item);
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
        // get result when async task completes
        if (result != null) {
            geoJsonCoins = result;
            addCoinsToMap();
        } else {
            Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content), "Could not download map", Snackbar.LENGTH_LONG);
            mySnackbar.show();
        }
    }

    public void downloadComplete(ArrayList<String> result) {
        // gets list of picked up coins when firestore query completes
        pickedUpCoins = result;
        addCoinsToMap();
    }

    private void addCoinsToMap() {
        // don't add coins until map has been downloaded from DownloadFileTask and picked up coins have been gotten from FireStore
        if (done && map.getMarkers().size() == 0) {
            FeatureCollection featureCollection = FeatureCollection.fromJson(geoJsonCoins);
            List<Feature> features = featureCollection.features();
            int[] markers = new int[] {R.drawable.marker0, R.drawable.marker0, R.drawable.marker1,
                    R.drawable.marker2, R.drawable.marker3, R.drawable.marker4, R.drawable.marker5,
                    R.drawable.marker6, R.drawable.marker7, R.drawable.marker8, R.drawable.marker9};
            // Create hashmap to link coins to their marker
            coinMap = new HashMap<>();
            for (Feature f : features) {
                String id = f.properties().get("id").getAsString();
                Double value = f.properties().get("value").getAsDouble();
                String currency = f.properties().get("currency").getAsString();
                // add marker if it is not in the list of coins already picked up today
                if (f.geometry() instanceof Point && (pickedUpCoins == null || !pickedUpCoins.contains(id))) {
                    // Create an Icon object for the marker to use
                    String markerColour = f.properties().get("marker-color").getAsString();
                    int markerSymbol = f.properties().get("marker-symbol").getAsInt();
                    Icon icon = drawableToIcon(markerColour, markers[markerSymbol]);

                    // Get marker details from feature
                    LatLng coordinates = new LatLng(((Point) f.geometry()).latitude(), ((Point) f.geometry()).longitude());
                    JsonElement symbol = f.properties().get("marker-symbol");
                    Marker m = map.addMarker(new MarkerOptions().position(coordinates).icon(icon));
                    // Add marker and coin to hashmap then put marker on map
                    coinMap.put(m, new Coin(id, value, currency));
                }
            }
        }
        else {
            // coins will be added the second time the method has been called
            done = true;
        }
    }

    // https://github.com/mapbox/mapbox-gl-native/issues/7897
    public Icon drawableToIcon(String colorRes, int marker) {
        // dynamically change colour of marker
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), marker, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, Color.parseColor(colorRes));
        //DrawableCompat.setTintMode(vectorDrawable, PorterDuff.Mode.DST_OVER);
        vectorDrawable.draw(canvas);
        return IconFactory.getInstance(this).fromBitmap(bitmap);
    }

    private void inRangeOfCoin() {
        // pick up coin when the user is within 25 metres
        List<Marker> markers = map.getMarkers();
        double latAngleUser = Math.toRadians(originLocation.getLatitude());
        double longAngleUser = Math.toRadians(originLocation.getLongitude());
        double radius = 6378100; // radius of the earth
        for(Marker m:markers) {
            LatLng markerPosition = m.getPosition();
            // Equirectangular approximation is a suitable formula to find small distances between points on earth
            double latAngleMarker = Math.toRadians(markerPosition.getLatitude());
            double longAngleMarker = Math.toRadians(markerPosition.getLongitude());
            double x = (longAngleUser-longAngleMarker) * Math.cos((latAngleUser+latAngleMarker)/2.0);
            double y = latAngleUser - latAngleMarker;
            double dist = Math.sqrt(x*x + y*y) * radius;
            if (dist < 25) {
                map.removeMarker(m);
                userFirestore.pickUp(coinMap.get(m));
            }
        }
    }

}
