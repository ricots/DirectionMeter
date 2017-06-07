package com.blackbox.direction.meter;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackbox.direction.meter.models.TargetLocation;
import com.blackbox.direction.meter.utils.GeoLocationService;
import com.blackbox.direction.meter.utils.Utils;
import com.blackbox.direction.meter.views.LockView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements SensorEventListener, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleApiClient.ConnectionCallbacks {

    private String TAG = MainActivity.class.getSimpleName();

    //UI
    TextView error_msg;
    LockView lockView;
    RelativeLayout mainLayout, errorLayout;
    ImageView img_help;
    FloatingActionButton btn_add_places;
    AppCompatImageView meterView;
    InterstitialAd mInterstitialAd;
    RxPermissions rxPermissions;
    ProgressDialog progressDialog;

    boolean useFusedLocation = false;
    double latitude, longitude;
    float deviceAzimuth, devicePitch, deviceRoll;
    float bearing;
    TargetLocation targetLocation;

    //Location Updates
    public static long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    public static long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    PlacePicker.IntentBuilder builder;
    Intent placeIntent;
    int PLACE_PICKER_REQUEST = 2;

    // device sensor manager
    private SensorManager mSensorManager;
    private Sensor orientatonSensor;
    boolean haveOrientationSensor = false;

    private BroadcastReceiver mLocationUpdateMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(GeoLocationService.LOCATION_DATA);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.i(TAG, "GPS Lat: " + location.getLatitude() + " Long: " + location.getLongitude());
            if (targetLocation != null) {
                getLocation(targetLocation.getLatitude(), targetLocation.getLongitude());
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new LockView(this);

        progressDialog = new ProgressDialog(this);
        rxPermissions = new RxPermissions(this);

        //Start Location Service
        rxPermissions.request(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION)
                .compose(rxPermissions.<Boolean>ensureEach(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION))
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            GeoLocationService.start(MainActivity.this);
                        }
                    }
                });

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();

        createLocationRequest();

        builder = new PlacePicker.IntentBuilder();
        try {
            placeIntent = new Intent(builder.build(this));
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

        meterView = (AppCompatImageView) findViewById(R.id.meter_view);
        btn_add_places = (FloatingActionButton) findViewById(R.id.fab_add);
        btn_add_places.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
        btn_add_places.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_add_places.clearAnimation();
                try {
                    startActivityForResult(placeIntent, PLACE_PICKER_REQUEST);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lockView = (LockView) findViewById(R.id.lockView);

        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
        error_msg = (TextView) findViewById(R.id.txtMsg);

        img_help = (ImageView) findViewById(R.id.img_help);
        img_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        final AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("9F51D215FB2210FE19FDE0CEE6AC47E9")
                .build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .tagForChildDirectedTreatment(true)
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                targetLocation = new TargetLocation();
                targetLocation.setName(place.getName().toString());
                targetLocation.setLatitude(place.getLatLng().latitude);
                targetLocation.setLongitude(place.getLatLng().longitude);
                getLocation(place.getLatLng().latitude, place.getLatLng().longitude);

                /*Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, place.getId())
                        .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {
                            @Override
                            public void onResult(PlacePhotoMetadataResult photos) {
                                if (photos.getStatus().isSuccess()) {
                                    PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                                    Bitmap image = photo.getPhoto(mGoogleApiClient).await().getBitmap();
                                    CharSequence attribution = photo.getAttributions();
                                    photoMetadataBuffer.release();
                                }
                            }
                        });*/

            }
        }
    }

    private void getLocation(double mLat, double mLong) {
        try {
            try {
                try {
                    progressDialog.setMessage(getString(R.string.txt_progress));
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();

                    new CompositeDisposable().add(getBearing(mLat, mLong)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableObserver<Integer>() {

                                @Override
                                public void onComplete() {
                                    progressDialog.hide();
                                }

                                @Override
                                public void onNext(Integer value) {
                                    targetLocation.setBearing(value);
                                    bearing = value;
                                    Log.i(TAG, "Compass: " + value);
                                    lockView.setTarget(MainActivity.this, targetLocation);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    progressDialog.hide();
                                    e.printStackTrace();

                                }
                            }));

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.txt_no_location), Toast.LENGTH_SHORT).show();
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            }


        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.txt_no_location), Toast.LENGTH_SHORT).show();
        }
    }

    private Observable<Integer> getBearing(double mLat, double mLong) {
        targetLocation.setDistance(Utils.distance(latitude, longitude, mLat, mLong, "K"));
        return Observable.just((int) Utils.bearing(latitude, longitude, mLat, mLong));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        try {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ORIENTATION:
                    deviceAzimuth = event.values[0]; //Azimuth
                    devicePitch = event.values[1];   // pitch
                    deviceRoll = event.values[2];    // roll
                    updateCompass(event.values[0]);
                    break;
                default:
                    return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // get the angle around the z-axis rotated
            float degree = Math.round(deviceAzimuth);

            // create a rotation animation (reverse turn degree degrees)
            RotateAnimation ra = new RotateAnimation(
                    -degree + bearing, -degree + bearing,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(100);
            ra.setInterpolator(new FastOutLinearInInterpolator());
            ra.setFillAfter(true);
            if (targetLocation != null)
                meterView.startAnimation(ra);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void updateCompass(float values) {
        try {
            // get the angle around the z-axis rotated
            float degree = Math.round(values);
            if (degree == 360) {
                degree -= 1;
            }
            if (degree >= 0 && degree <= 359) {
                lockView.setDegrees(degree);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (useFusedLocation)
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.i(TAG, "Lat: " + location.getLatitude() + " Long: " + location.getLongitude());
        if (targetLocation != null) {
            getLocation(targetLocation.getLatitude(), targetLocation.getLongitude());
        }
    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return (gps_enabled || network_enabled);
    }

    public void promptForLocation() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(getString(R.string.txt_location_access));
        alertDialogBuilder.setMessage(getString(R.string.txt_location_info));
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.txt_okay), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Intent callGPSSettingIntent =
                                new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(callGPSSettingIntent);
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onDestroy() {
        GeoLocationService.stop(this);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        if (!isLocationEnabled()) {
            promptForLocation();
        }
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
        this.orientatonSensor = this.mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        this.haveOrientationSensor = this.mSensorManager.registerListener(this, this.orientatonSensor, SensorManager.SENSOR_DELAY_NORMAL);
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationUpdateMessageReceiver,
                new IntentFilter(GeoLocationService.LOCATION_UPDATE));

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(this);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationUpdateMessageReceiver);
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }

    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
        showExitDialog();
    }

    private void showExitDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.txt_exit_app));
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
