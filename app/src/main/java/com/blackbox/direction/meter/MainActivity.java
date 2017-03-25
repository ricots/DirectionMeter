package com.blackbox.direction.meter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.blackbox.direction.meter.camera.Camera2Activity;
import com.blackbox.direction.meter.camera.CameraActivity;
import com.blackbox.direction.meter.utils.Constants;
import com.blackbox.direction.meter.utils.GeoLocationService;
import com.blackbox.direction.meter.utils.Preference;
import com.blackbox.direction.meter.utils.Utils;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isLocationEnabled()) {
            Intent i;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                i = new Intent(this, Camera2Activity.class);
                startActivity(i);
                finish();
            } else {
                i = new Intent(this, CameraActivity.class);
                startActivity(i);
                finish();
            }
        } else {
            promptForLocation();
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
}
