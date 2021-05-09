package com.wacked.smarttourist.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.wacked.smarttourist.BuildConfig;
import com.wacked.smarttourist.R;
import com.wacked.smarttourist.Route;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainMapsActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final int CHECK_SETTINGS_CODE = 111;
    private static final int REQUEST_LOCATION_PERMISSION = 222;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    static int id = 1;
    FloatingActionButton fab;
    FloatingActionButton fabClose;
    Route route;
    boolean onRoute = false;
    Map<String, Integer> res = new HashMap<String, Integer>();
    MediaPlayer mPlayer;
    int id1 = 0;
    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private boolean isLocationUpdatesActive;
    private GoogleMap map;
    private UiSettings uiSettings;
    private boolean OnPoint = false;
    private boolean ReadyToStart = false;

    public static String DownloadFile(String fname) throws IOException {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://smart-tourist-0-1.appspot.com/");
        StorageReference islandRef = storageRef.child(fname);

        File rootPath = new File(String.valueOf(Environment.getExternalStorageDirectory()), fname);
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        File localFile = new File(String.format("/data/data/com.wacked.smarttourist/files/%d.mp3", id));
        id += 1;

        islandRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> Log.d("Successful download", localFile.getPath())).addOnFailureListener(exception -> Log.d("Unsucsesful download", exception.getMessage()));
        if (localFile.exists()) {
            return "ERROR";
        } else {
            return localFile.getAbsolutePath();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);
        for (int i = 1; i < 28; i++) {
            try {
                DownloadFile(String.format("%d.mp3", i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ParseIDFromTxtLines();

        buildLocationRequest();
        buildLocationCallBack();
        buildLocationSettingsRequest();
        startLocationUpdates();
        stopLocationUpdates();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            if (ReadyToStart) {
                onRoute = true;
                fabClose.setVisibility(View.VISIBLE);
                LatLng userLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                map.animateCamera(CameraUpdateFactory.zoomTo(16));
                startLocationUpdates();
            } else {
                Toast toast = Toast.makeText(MainMapsActivity.this, "Сначала введите конечный пункт.", Toast.LENGTH_SHORT);
                toast.show();
            }

        });
        fabClose = findViewById(R.id.fab2);
        fabClose.setVisibility(View.GONE);
        fabClose.setOnClickListener(this::StopRoute);
        Button Search = findViewById(R.id.search_button);
        Search.setOnClickListener(this::MapSearch);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        uiSettings = map.getUiSettings();


        if (currentLocation != null) {
            LatLng userLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
            map.animateCamera(CameraUpdateFactory.zoomTo(16));
        }
        enableMyLocation();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHECK_SETTINGS_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        isLocationUpdatesActive = false;
                        updateLocationUi();
                        break;
                }
                break;
        }
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
            buildLocationCallBack();
            startLocationUpdates();
        } else if (!checkLocationPermission()) {
            requestLocationPermission();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            stopPlay();

        }
    }

    /**
     * Технический метод request-а к местоположению.
     */
    private void buildLocationSettingsRequest() {

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();

    }

    /**
     * Технический метод request-а к местоположению.
     */
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

    /**
     * Технический метод request-а к местоположению.
     */
    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(8000);
        locationRequest.setFastestInterval(6000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    /**
     * Метод включения иконки местоположения пользователя и кнопки центрирования.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (map != null) {
                map.setMyLocationEnabled(true);
                if (!uiSettings.isMyLocationButtonEnabled()) {
                    uiSettings.setMyLocationButtonEnabled(true);
                }
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    /**
     * Метод завершения обновления местоположения.
     */
    private void stopLocationUpdates() {

        if (!isLocationUpdatesActive) {
            return;
        }

        fusedLocationClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(this, task -> isLocationUpdatesActive = false);

    }

    /**
     * Метод начала обновлений местоположения пользователя.
     */
    private void startLocationUpdates() {
        isLocationUpdatesActive = true;
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this,
                        locationSettingsResponse -> {
                            if (ActivityCompat.checkSelfPermission(
                                    MainMapsActivity.this,
                                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(MainMapsActivity.this,
                                            Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                            PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                            updateLocationUi();
                        })
                .addOnFailureListener(this, e -> {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes
                                .RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainMapsActivity.this, CHECK_SETTINGS_CODE);
                            } catch (IntentSender.SendIntentException sie) {
                                sie.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            String message = "Adjust location settings on your device";
                            Toast.makeText(MainMapsActivity.this, message, Toast.LENGTH_LONG).show();
                            isLocationUpdatesActive = false;
                    }
                    updateLocationUi();
                });
    }

    /**
     * Технический метод запроса разрешений.
     */
    private boolean checkLocationPermission() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Технический метод запроса разрешений.
     */
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

    /**
     * Технический метод для запроса разрешений.
     */

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

    /**
     * Показывает снэкбар.
     */
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

    /**
     * Создает список координат мест.
     */
    public String[] ParseFromTxtLines() {
        ArrayList<String> outputArray = new ArrayList<String>();
        InputStream is = getResources().openRawResource(R.raw.places);
        InputStreamReader isp = new InputStreamReader(is);
        try (BufferedReader reader = new BufferedReader(isp)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] adder = line.split(" ");
                outputArray.add(adder[0]);
            }
        } catch (IOException e) {
            Log.d("Error: ", e.getMessage());
        }
        String[] output = new String[outputArray.size()];
        output = outputArray.toArray(output);
        return output;
    }


    /**
     * Создает список id из файла с геоточками.
     */
    public void ParseIDFromTxtLines() {

        InputStream is = getResources().openRawResource(R.raw.places);
        InputStreamReader isp = new InputStreamReader(is);
        try (BufferedReader reader = new BufferedReader(isp)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] adder = line.split(" ");
                res.put(adder[0], Integer.parseInt(adder[1]));
            }
        } catch (IOException e) {
            Log.d("Error: ", e.getMessage());
        }

    }


    /**
     * Метод поиска точки на карте и построения маршрута по нажатию.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void MapSearch(View view) {
        try {
            map.clear();
            TextView locSearch = findViewById(R.id.LocSearch);
            String location = locSearch.getText().toString() + ", Ростов-на-Дону";
            List<Address> addressList;

            Geocoder geocoder = new Geocoder(MainMapsActivity.this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(MainMapsActivity.this, "Введите корректный адрес!", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            if (addressList != null) {
                ReadyToStart = true;
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                map.addMarker(new MarkerOptions().position(latLng).title("Endpoint"));

                LatLng startpoint = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                route = new Route(startpoint, latLng);
                route.setPlaces(ParseFromTxtLines());
                CreateRoute(startpoint, latLng);
                stopLocationUpdates();
                locSearch.setText("");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(locSearch.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

            } else {
                Toast toast = Toast.makeText(MainMapsActivity.this, "Введите адрес!", Toast.LENGTH_SHORT);
                toast.show();
            }
        } catch (Exception e) {
            Toast toast = Toast.makeText(MainMapsActivity.this, "Простите, произошла ошибка.", Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }
    }


    /**
     * Основной метод обновления UI пользователя.
     */
    private void updateLocationUi() {

        if (currentLocation != null) {
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            map.animateCamera(CameraUpdateFactory.zoomTo(16));
            if (onRoute) {
                map.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                map.animateCamera(CameraUpdateFactory.zoomTo(16));
                if (NearPoint(route.GetLatLngsForRoute()) && !OnPoint) {
                    mPlayer = MediaPlayer.create(this,
                            Uri.fromFile(new File(String.format("/data/data/com.wacked.smarttourist/files/%d.mp3", id1))));
                    mPlayer.start();
                    OnPoint = true;
                    mPlayer.setOnCompletionListener(mp -> {
                        mPlayer.stop();
                        OnPoint = false;
                    });
                }
            }
        }
    }


    /**
     * Остановка проигрывания аудио.
     */
    private void stopPlay() {
        mPlayer.stop();
        try {
            mPlayer.prepare();
            mPlayer.seekTo(0);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    /**
     * Метод определения местоположения относительно точки маршрута, на которую нужно запускать аудио и текст.
     */
    public boolean NearPoint(LatLng[] points) {
        boolean checker = false;
        for (LatLng point : points) {
            if (DistanceInMeters(currentLocation.getLatitude(), currentLocation.getLongitude(), point.latitude, point.longitude) < 80.0) {
                checker = true;
                id1 = res.get(String.format("%f,%f", point.latitude, point.longitude));
            }
        }
        return checker;
    }


    /**
     * Дистанция между точками.
     */
    public double DistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        final double r = 6371e3;
        double dLat = Math.toRadians(lat2 - lat1) * 0.5;
        double dLon = Math.toRadians(lon2 - lon1) * 0.5;
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat) * Math.sin(dLat) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon) * Math.sin(dLon);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }

    /**
     * Метод построения маршрута.
     */

    public void CreateRoute(LatLng startpoint, LatLng endpoint) {
        String api_key = BuildConfig.API_KEY;
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(api_key)
                .build();
        DirectionsApiRequest.Waypoint[] waypoints = route.GetWaypointsForRoute();
        DirectionsApiRequest apiRequest = DirectionsApi.newRequest(geoApiContext);
        apiRequest.origin(new com.google.maps.model.LatLng(startpoint.latitude, startpoint.longitude));
        apiRequest.destination(new com.google.maps.model.LatLng(endpoint.latitude, endpoint.longitude));
        apiRequest.mode(TravelMode.WALKING);
        apiRequest.waypoints(waypoints);
        apiRequest.optimizeWaypoints(true);
        apiRequest.language("Russian");
        apiRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                DirectionsRoute[] routes = result.routes;
                List<com.google.maps.model.LatLng> path = routes[0].overviewPolyline.decodePath();
                String time = "";
                long time2 = 0;
                for (DirectionsLeg leg : routes[0].legs) {
                    time2 += leg.duration.inSeconds;
                }
                time2 = time2 / 60;
                time = String.format("%dч. %d мин. ", time2 / 60, time2 % 60);
                LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
                PolylineOptions line = new PolylineOptions();
                for (int i = 0; i < path.size(); i++) {
                    line.add(new LatLng(path.get(i).lat, path.get(i).lng));
                    latLngBuilder.include(new LatLng(path.get(i).lat, path.get(i).lng));
                }
                LatLngBounds latLngBounds = latLngBuilder.build();
                String finalTime = time;
                runOnUiThread(() -> {
                    line.width(16f).color(R.color.purple_500);
                    CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, 1080, 1920, 25);
                    map.moveCamera(track);
                    map.addPolyline(line);
                    TextView bottom = findViewById(R.id.Bottom_time);
                    bottom.setText(finalTime);

                });
            }

            @Override
            public void onFailure(Throwable e) {
                Log.i("OnFailureInfo", e.getMessage());
            }
        });
    }


    /**
     * Метод выхода с маршрута.
     */
    private void StopRoute(View view) {
        map.clear();
        fabClose.setVisibility(View.GONE);
        onRoute = false;
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(16));
        stopLocationUpdates();
    }


    //Временно отключено за ненадобностью деавторизации, которая производится по нажатию кнопки "назад", и настроек.
    /*
    public void goToProfile(View view) {
        startActivity(new Intent(MainMapsActivity.this, ProfileActivity.class));
    }
    public void goToSettings(View view) {
        startActivity(new Intent(MainMapsActivity.this, SettingsActivity.class));
    }*/

}