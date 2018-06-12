package com.example.luca.progettoesame;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;

import static android.hardware.SensorManager.getOrientation;
import static android.hardware.SensorManager.getRotationMatrix;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mGravitySensor;
    private Sensor mGeomagneticSensor;

    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted = false;
    private static final float DEFAULT_ZOOM = 15f;

    private TextView azimuthTv;
    private LocationManager locationManager;
    private TextView nome;
    private TextView stato;
    private TextView popolazione;
    private TextView distance;
    private TextView angle;

    float[] gravity = new float[3];
    float[] geomagnetic = new float[3];

    private ArrayList<Citta> cities;
    private double latitudine;
    private double longitudine;
    private ArrayDeque<Float> codaAzim = new ArrayDeque<>();
    private final static int MAX_NUM_AZIM = 100;
    private static String TAG = MainActivity.class.getSimpleName();
    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    ArrayList<NavItem> mNavItems = new ArrayList<>();
    private Polyline line;
    private DecimalFormat azimuthFormat = new DecimalFormat("#.###");
    private Citta citta_precedente = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        assert mSensorManager != null;
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGeomagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        azimuthTv = findViewById(R.id.azimuth);
        nome = findViewById(R.id.nome);
        stato = findViewById(R.id.stato);
        popolazione = findViewById(R.id.popolazione);

        distance = findViewById(R.id.Distance);
        angle = findViewById(R.id.Angle);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mNavItems.add(new NavItem("Capitali", "Elenco capitali", R.drawable.onu, MySQLiteHelper.COUNTRY_CODE_CAPITALI));
        mNavItems.add(new NavItem("Italia", "Elenco città italiane", R.drawable.italia, "ITA"));
        mNavItems.add(new NavItem("Francia", "Elenco città francesi", R.drawable.francia, "FRA"));
        mNavItems.add(new NavItem("Germania", "Elenco città tedesche", R.drawable.germany, "GER"));
        mNavItems.add(new NavItem("Giappone", "Elenco città giapponesi", R.drawable.japan, "JAP"));
        mNavItems.add(new NavItem("Regno Unito", "Elenco città inglesi", R.drawable.uk, "GBR"));
        mNavItems.add(new NavItem("Stati Uniti d'America", "Elenco città americane", R.drawable.usa, "USA"));


        changeCities(MySQLiteHelper.COUNTRY_CODE_CAPITALI, this);
        mDrawerLayout = findViewById(R.id.drawerLayout);

        mDrawerPane = findViewById(R.id.drawerPane);
        mDrawerList = findViewById(R.id.navList);
        DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems, this);
        mDrawerList.setAdapter(adapter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Drawer Item click listeners
        mDrawerList.setOnItemClickListener((parent, view, position, id) -> selectItemFromDrawer(position));

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                latitudine = location.getLatitude();
                longitudine = location.getLongitude();
                location.getBearing();
                updatePolyline();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        getLocationPermission();
        checkLocationPermission();
        checkLocationPermission1();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_launcher_background, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d(TAG, "onDrawerClosed: ");

                invalidateOptionsMenu();
            }

        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


    public void checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
    }

    public void checkLocationPermission1() {
        String permission = "android.permission.ACCESS_COARSE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mGeomagneticSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private void selectItemFromDrawer(int position) {
        Fragment fragment = new PreferencesFragment();

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.mainContent, fragment)
                .commit();

        mDrawerList.setItemChecked(position, true);

        // Close the drawer
        mDrawerLayout.closeDrawer(mDrawerPane);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(mGravitySensor)) {
            gravity = event.values;
            updateScreen();
        } else if (event.sensor.equals(mGeomagneticSensor)) {
            geomagnetic = event.values;
            updateScreen();
        }
    }

    public void updateScreen() {
        float[] R = new float[16];
        getRotationMatrix(R, null, gravity, geomagnetic);
        float[] angles = new float[3];
        getOrientation(R, angles);
        codaAzim.addFirst(angles[0]);
        if (codaAzim.size() > MAX_NUM_AZIM) {
            codaAzim.removeLast();
        }

        azimuthTv.setText(MessageFormat.format("{0} {1}", getString(com.example.luca.progettoesame.R.string.azimuth_value), azimuthFormat.format(Math.toDegrees(getAzimuth()))));
        updatePolyline();

    }

    public float getAzimuth() {
        // Media aritmentica per valori circolari
        // https://en.wikipedia.org/wiki/Mean_of_circular_quantities
        float sina = 0;
        float cosa = 0;
        for (Float x : codaAzim) {
            sina += Math.sin(x);
            cosa += Math.cos(x);
        }
        return (float) Math.atan2(sina, cosa);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public Vector3D gps_to_xyz(double lat, double longg, double radius) {
        lat = Math.toRadians(90 - lat);
        longg = Math.toRadians(longg);
        double x = radius * Math.sin(lat) * Math.cos(longg);
        double y = radius * Math.sin(lat) * Math.sin(longg);
        double z = radius * Math.cos(lat);
        return new Vector3D(x, y, z);
    }

    public Pair<Citta, Float> findCity() {
        double azimuth = getAzimuth();
        Vector3D user = gps_to_xyz(latitudine, longitudine, 1);
        double minAngle = 1000;
        Citta city = null;
        for (Citta x : cities) {
            Vector3D cityPos = gps_to_xyz(x.getLatitudine(), x.getLongitudine(), 1);
            Vector3D locPos = new Vector3D(-Math.cos(azimuth), Math.sin(azimuth), 0);
            Rotation quat_rot_lat = new Rotation(new Vector3D(0, 1, 0), latitudine);
            Rotation quat_rot_long = new Rotation(new Vector3D(0, 0, 1), longitudine);
            Vector3D gloDir = quat_rot_long.applyTo(quat_rot_lat.applyTo(locPos)).scalarMultiply(user.getNorm());
            Vector3D normUser = Vector3D.crossProduct(user, cityPos);
            Vector3D normCity = Vector3D.crossProduct(user, gloDir.add(user));
            double angle = Vector3D.angle(normUser, normCity);
            if (angle < minAngle) {
                minAngle = angle;
                city = x;
            }
        }
        return new Pair<>(city, (float) minAngle);
    }


    //-------------------------GOOGLE MAPS CODE---------------------------------------------------------//

    public void changeCities(String countryCode, Context context) {
        MySQLiteHelper helper = MySQLiteHelper.getInstance(context);
        Log.d("DB", countryCode);
        cities = helper.elencoCitta(countryCode);
        Stato stato = MySQLiteHelper.getInstance(context).getStatoByCountryCode(countryCode);

        if(stato != null) {
            setTitle(MessageFormat.format("{0} {1}", getString(R.string.cities_state), stato.getNomeStato()));
        } else {
            setTitle("Capitali");
        }
        // Close the drawer
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mDrawerPane);
        }
    }

    public String getStato(String citta) {
        MySQLiteHelper helper = MySQLiteHelper.getInstance(this);
        return helper.getNomeStatoByCitta(citta);
    }


    private void getLocationPermission() {
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permission, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permission, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void onRequestPermissionResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResult) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResult.length > 0) {
                    for (int i = 0; i < grantResult.length; i++) {
                        if (grantResult[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            break;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Log.d(TAG, "on Complete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG, "on Complete: current location is null");
                            Toast.makeText(MainActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: security Exception: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            Log.d("GPS", Double.valueOf(latitudine).toString());
            Log.d("GPS", Double.valueOf(longitudine).toString());
            updatePolyline();
        }
    }

    private void updatePolyline() {
        Pair<Citta, Float> citta_angolo = findCity();
        Citta citta = citta_angolo.first;
        if (citta != null && mMap != null && (citta_precedente == null || citta.getIdCitta() != citta_precedente.getIdCitta())) {
            citta_precedente = citta;
            if (line != null) {
                line.remove();
            }
            nome.setText(MessageFormat.format("{0} {1}", getString(R.string.city_name), citta.getNomeCitta()));
            popolazione.setText(MessageFormat.format("{0} {1}", getString(R.string.city_population), citta.getPopolazione()));
            stato.setText(MessageFormat.format("{0} {1}", getString(R.string.city_state), getStato(citta.NomeCitta)));
            LatLng startPoint = new LatLng(latitudine, longitudine);
            LatLng endPoint = new LatLng(citta.getLatitudine(), citta.getLongitudine());
            line = mMap.addPolyline(new PolylineOptions()
                    .add(startPoint, endPoint)
                    .width(5)
                    .color(Color.RED));
            line.setGeodesic(true);

            Location start = new Location("");
            start.setLatitude(latitudine);
            start.setLongitude(longitudine);
            Location end = new Location("");
            end.setLatitude(citta.getLatitudine());
            end.setLongitude(citta.getLongitudine());
            distance.setText(MessageFormat.format(
                    "{0} {1} km", getString(R.string.distance_city),
                    start.distanceTo(end)/1000));
        }
        angle.setText(MessageFormat.format("{0} {1}°", getString(R.string.angle_city), Math.toDegrees(citta_angolo.second)));

    }
}


