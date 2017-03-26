package com.blackbox.direction.meter.camera;

/**
 * Created by Umair_Adil on 20/05/2016.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.blackbox.direction.meter.R;
import com.blackbox.direction.meter.models.TargetLocation;
import com.blackbox.direction.meter.utils.Constants;
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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        View.OnClickListener, SensorEventListener, GoogleApiClient.OnConnectionFailedListener {


    private Context context;
    private String TAG = CameraActivity.class.getSimpleName();

    final Handler handler = new Handler();
    Runnable mRunnable;

    private boolean isPlace = false;
    LockView lockView;
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    FloatingActionButton btn_add_places;
    AppCompatImageView meterView;
    InterstitialAd mInterstitialAd;

    Boolean dialogShown = false;

    TextView error_msg;
    RelativeLayout mainLayout, errorLayout;
    ImageView img_help;


    double latitude, longitude;
    int year;
    int month;
    int day;
    int hour;
    int min;
    int sec;
    float deviceAzimuth, devicePitch, deviceRoll;
    float bearing;
    TargetLocation targetLocation;

    GoogleApiClient mGoogleApiClient;
    PlacePicker.IntentBuilder builder;
    Intent placeIntent;
    int PLACE_PICKER_REQUEST = 2;

    // device sensor manager
    private SensorManager mSensorManager;

    private Sensor orientatonSensor;

    boolean haveOrientationSensor = false;

    //Move Curser
    public float xmax, ymax;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        this.context = this;
        new LockView(this);
        GeoLocationService.start(CameraActivity.this);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        builder = new PlacePicker.IntentBuilder();
        try {
            placeIntent = new Intent(builder.build(this));
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

        //Calculate Boundry
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        xmax = (float) metrics.widthPixels - 120;
        ymax = (float) metrics.heightPixels - 120;

        surfaceView = (SurfaceView) findViewById(R.id.texture);
        lockView = (LockView) findViewById(R.id.lockView);
        surfaceHolder = surfaceView.getHolder();

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

        surfaceHolder.addCallback(this);

        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
        error_msg = (TextView) findViewById(R.id.txtMsg);

        img_help = (ImageView) findViewById(R.id.img_help);

        img_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        Calendar today = Calendar.getInstance();
        year = today.get(Calendar.YEAR);
        month = today.get(Calendar.MONTH) + 1;
        day = today.get(Calendar.DAY_OF_MONTH);
        hour = today.get(Calendar.HOUR_OF_DAY);
        min = today.get(Calendar.MINUTE);
        sec = today.get(Calendar.SECOND);


        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

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


    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
        }
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {

        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        refreshCamera();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // open the camera
            camera = Camera.open();
        } catch (RuntimeException e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
        Camera.Parameters param;
        param = camera.getParameters();

        try {
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            Camera.Size cs = sizes.get(0);
            parameters.setPreviewSize(cs.width, cs.height);
            camera.setParameters(parameters);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return;
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            setCameraDisplayOrientation(this, 0, camera);
        } catch (Exception e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // stop preview and release camera
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {

        Camera.CameraInfo info =
                new Camera.CameraInfo();

        Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
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
            }
        }
    }

    private void getLocation(double mLat, double mLong) {
        try {
            try {
                try {


                    targetLocation.setBearing((int) Utils.bearing(latitude, longitude, mLat, mLong));
                    targetLocation.setDistance(Utils.distance(latitude, longitude, mLat, mLong, "K"));

                    bearing = targetLocation.getBearing();
                    Log.i(TAG, "Compass: " + bearing);

                    lockView.setTarget(this, targetLocation);

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

            ra.setDuration(250);
            ra.setFillAfter(true);
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
    public void onClick(View v) {

    }

    @Override
    protected void onDestroy() {
        GeoLocationService.stop(this);
        super.onDestroy();
    }


    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationUpdateMessageReceiver,
                new IntentFilter(GeoLocationService.LOCATION_UPDATE));

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
        this.orientatonSensor = this.mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        this.haveOrientationSensor = this.mSensorManager.registerListener(this, this.orientatonSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        try {
            handler.removeCallbacks(mRunnable);
            mRunnable = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSensorManager.unregisterListener(this);
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationUpdateMessageReceiver);

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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
