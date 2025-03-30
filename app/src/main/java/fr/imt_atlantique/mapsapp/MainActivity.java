package fr.imt_atlantique.mapsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_ADD_MARKER = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 101;

    private GoogleMap mMap;
    private boolean locationPermissionGranted = false;

    private final List<MarkerData> markerList = new ArrayList<>();
    private final Map<Marker, MarkerData> markerMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadMarkersFromPrefs();
        getLocationPermission();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // UI interaction
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        updateLocationUI();

        // Move to IMT Brest
        LatLng imtBrest = new LatLng(48.359285, -4.569933);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(imtBrest, 15f));
        mMap.addMarker(new MarkerOptions().position(imtBrest).title("IMT Atlantique Brest"));

        // Restore existing markers
        for (MarkerData md : markerList) {
            addMarkerToMap(md);
        }

        mMap.setOnMarkerClickListener(this);

        // Show coordinates on map click
        mMap.setOnMapClickListener(latLng -> {
            String msg = "Lat: " + latLng.latitude + ", Lng: " + latLng.longitude;
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Long click to add marker
        mMap.setOnMapLongClickListener(latLng -> {
            Intent intent = new Intent(MainActivity.this, AddMarkerActivity.class);
            intent.putExtra("latitude", latLng.latitude);
            intent.putExtra("longitude", latLng.longitude);
            startActivityForResult(intent, REQUEST_ADD_MARKER);
        });
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) return;
        try {
            mMap.setMyLocationEnabled(locationPermissionGranted);
        } catch (SecurityException e) {
            Log.e("Map", "Location permission error: ", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            locationPermissionGranted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            updateLocationUI();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        if (requestCode == REQUEST_ADD_MARKER && resultCode == RESULT_OK && dataIntent != null) {
            double lat = dataIntent.getDoubleExtra("latitude", 0);
            double lng = dataIntent.getDoubleExtra("longitude", 0);
            String title = dataIntent.getStringExtra("title");
            String desc = dataIntent.getStringExtra("description");
            String imagePath = dataIntent.getStringExtra("imagePath");

            MarkerData md = new MarkerData(lat, lng, title, desc, imagePath);
            markerList.add(md);
            addMarkerToMap(md);
            saveMarkersToPrefs();
        }
    }

    private void addMarkerToMap(MarkerData data) {
        MarkerOptions opts = new MarkerOptions()
                .position(new LatLng(data.latitude, data.longitude))
                .title(data.title);

        // Use resized icon instead of full-size image
        if (data.imagePath != null && !data.imagePath.isEmpty()) {
            Bitmap resizedIcon = getResizedMarkerBitmap(data.imagePath);
            if (resizedIcon != null) {
                opts.icon(BitmapDescriptorFactory.fromBitmap(resizedIcon));
            }
        }

        Marker marker = mMap.addMarker(opts);
        if (marker != null) {
            markerMap.put(marker, data);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        MarkerData data = markerMap.get(marker);
        if (data != null) {
            Intent intent = new Intent(MainActivity.this, MarkerDetailActivity.class);
            intent.putExtra("title", data.title);
            intent.putExtra("description", data.description);
            intent.putExtra("imagePath", data.imagePath);
            startActivity(intent);
            return true;
        }
        return false;
    }

    // ===== Menu for map type =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mMap == null) return false;

        int id = item.getItemId();

        if (id == R.id.normal_map) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            return true;
        } else if (id == R.id.hybrid_map) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            return true;
        } else if (id == R.id.satellite_map) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    // ===== SharedPreferences: Save & Load =====

    private void saveMarkersToPrefs() {
        SharedPreferences prefs = getSharedPreferences("markers", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(markerList);
        editor.putString("markerList", json);
        editor.apply();
    }

    private void loadMarkersFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("markers", MODE_PRIVATE);
        String json = prefs.getString("markerList", null);
        if (json != null) {
            Type type = new TypeToken<List<MarkerData>>() {}.getType();
            List<MarkerData> list = new Gson().fromJson(json, type);
            if (list != null) {
                markerList.clear();
                markerList.addAll(list);
            }
        }
    }

    /**
     * Load a bitmap from given file path and resize it to fit Marker icon.
     */
    private Bitmap getResizedMarkerBitmap(String imagePath) {
        File imgFile = new File(imagePath);
        if (!imgFile.exists()) return null;

        Bitmap original = BitmapFactory.decodeFile(imagePath);
        if (original == null) return null;

        // Resize to a more reasonable marker size (e.g., 100x100 pixels)
        return Bitmap.createScaledBitmap(original, 100, 100, false);
    }

}
