package com.wacked.smarttourist.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.wacked.smarttourist.BuildConfig;
import com.wacked.smarttourist.R;


public class MainMapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private static final int CHECK_SETTINGS_CODE = 111;
    private static final int REQUEST_LOCATION_PERMISSION = 222;


    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private boolean isLocationUpdatesActive;

    private GoogleMap mMap;
    //private UiSettings uiSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        buildLocationRequest();
        buildLocationCallBack();
        buildLocationSettingsRequest();
        startLocationUpdates();


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //uiSettings = mMap.getUiSettings();

        if (currentLocation != null) {
            LatLng userLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        }

    }

    private void stopLocationUpdates() {

        if (!isLocationUpdatesActive) {
            return;
        }

        fusedLocationClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(this, task -> isLocationUpdatesActive = false);

    }

    private void startLocationUpdates() {

        isLocationUpdatesActive = true;

        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this,
                        locationSettingsResponse -> {

                            if (ActivityCompat.checkSelfPermission(
                                    MainMapsActivity.this,
                                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                                    PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat
                                            .checkSelfPermission(
                                                    MainMapsActivity.this,
                                                    Manifest.permission
                                                            .ACCESS_COARSE_LOCATION) !=
                                            PackageManager.PERMISSION_GRANTED) {

                                return;
                            }
                            fusedLocationClient.requestLocationUpdates(
                                    locationRequest,
                                    locationCallback,
                                    Looper.myLooper()
                            );

                            updateLocationUi();

                        })
                .addOnFailureListener(this, e -> {


                    int statusCode = ((ApiException) e).getStatusCode();

                    switch (statusCode) {

                        case LocationSettingsStatusCodes
                                .RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException =
                                        (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(
                                        MainMapsActivity.this,
                                        CHECK_SETTINGS_CODE
                                );
                            } catch (IntentSender.SendIntentException sie) {
                                sie.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes
                                .SETTINGS_CHANGE_UNAVAILABLE:
                            String message =
                                    "Adjust location settings on your device";
                            Toast.makeText(MainMapsActivity.this, message,
                                    Toast.LENGTH_LONG).show();

                            isLocationUpdatesActive = false;

                    }

                    updateLocationUi();

                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //TODO Разобраться с проблемой, которая не влияет на сборку и запуск приложения
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case CHECK_SETTINGS_CODE:

                switch (resultCode) {

                    case Activity.RESULT_OK:
                        Log.d("MainActivity", "User has agreed to change location" +
                                "settings");
                        startLocationUpdates();
                        break;

                    case Activity.RESULT_CANCELED:
                        Log.d("MainActivity", "User has not agreed to change location" +
                                "settings");
                        isLocationUpdatesActive = false;
                        updateLocationUi();
                        break;
                }

                break;
        }

    }

    private void buildLocationSettingsRequest() {

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();

    }

    private void buildLocationCallBack() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                currentLocation = locationResult.getLastLocation();

                updateLocationUi();


            }
        };

    }

    private void updateLocationUi() {

        if (currentLocation != null) {
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        }

    }

    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isLocationUpdatesActive && checkLocationPermission()) {

            startLocationUpdates();

        } else if (!checkLocationPermission()) {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {

        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        );

        if (shouldProvideRationale) {

            showSnackBar(
                    "Location permission is needed for " +
                            "app functionality",
                    "OK",
                    v -> ActivityCompat.requestPermissions(
                            MainMapsActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            REQUEST_LOCATION_PERMISSION
                    )

            );

        } else {

            ActivityCompat.requestPermissions(
                    MainMapsActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION
            );

        }

    }

    private void showSnackBar(
            final String mainText,
            final String action,
            View.OnClickListener listener) {

        Snackbar.make(
                findViewById(android.R.id.content),
                mainText,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(
                        action,
                        listener
                )
                .show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION_PERMISSION) {

            if (grantResults.length <= 0) {
                Log.d("onRequestPermissions",
                        "Request was cancelled");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationUpdatesActive) {
                    startLocationUpdates();
                }
            } else {
                showSnackBar(
                        "Turn on location on settings",
                        "Settings",
                        v -> {
                            Intent intent = new Intent();
                            intent.setAction(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts(
                                    "package",
                                    BuildConfig.APPLICATION_ID,
                                    null
                            );
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                );
            }

        }

    }

    private boolean checkLocationPermission() {

        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;

    }

    public void goToProfile(View view) {
        startActivity(new Intent(MainMapsActivity.this, ProfileActivity.class));
    }

    public void goToSettings(View view) {
        startActivity(new Intent(MainMapsActivity.this, SettingsActivity.class));
    }
}