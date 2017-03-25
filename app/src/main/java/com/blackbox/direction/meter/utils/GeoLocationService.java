package com.blackbox.direction.meter.utils;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class GeoLocationService extends Service {

    public static final String LOCATION_UPDATE = GeoLocationService.class.getSimpleName();
    public static final String LOCATION_DATA = "location_data";

    private static final String TAG = GeoLocationService.class.getSimpleName();
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000; // 1 Second
    private static final float LOCATION_DISTANCE = 0f; // meters = 100 m

    public static void start(Context context) {
        context.startService(new Intent(context, GeoLocationService.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, GeoLocationService.class));
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation.set(location);
            //if (DEBUG)
            //Log.i(TAG, "Location Changed! " + location.getLatitude() + " " + location.getLongitude());
            Intent intent = new Intent(LOCATION_UPDATE);
            intent.putExtra(LOCATION_DATA, location);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        @Override
        public void onProviderDisabled(String provider) {

            Log.e(TAG, "Location Provider Disabled!");
        }

        @Override
        public void onProviderEnabled(String provider) {

            Log.i(TAG, "Location Provider Enabled!");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (SecurityException ex) {

            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {

            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (SecurityException ex) {

            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {

            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (checkLocationPermission()) {
                        mLocationManager.removeUpdates(mLocationListeners[i]);
                    }
                } catch (Exception ex) {

                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT < 23)
            return true;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

}