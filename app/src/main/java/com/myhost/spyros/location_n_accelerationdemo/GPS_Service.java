package com.myhost.spyros.location_n_accelerationdemo;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.github.anastr.speedviewlib.SpeedView;

public class GPS_Service extends Service {
    private LocationListener listener;
    private LocationManager locationManager;

    //SharedPreferences preferences;
    public static String service = "";

    //SQLiteDatabase mDatabase;

    private static String LOG_TAG = "BoundService";
    private boolean isSpeedServiceOn;
    private boolean isPoisServiceOn;

    public static double currentLatitudeForMaps,currentLongitudeForMaps = 0;


    private void sendMessageToActivity(Location l, String msg) {
        Intent intent = new Intent("speed_update");
        intent.putExtra("Status", msg);
        Bundle b = new Bundle();
        b.putParcelable("Location", l);
        intent.putExtra("Location", b);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendMessageToActivity2(Location l, String msg) {
        Intent intent = new Intent("pois_update");
        intent.putExtra("Status", msg);
        Bundle b = new Bundle();
        b.putParcelable("Location", l);
        intent.putExtra("Location", b);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


    @Override
    public void onCreate() {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if (isSpeedServiceOn) {
                    sendMessageToActivity(location, "speed_update");
                }
                if(isPoisServiceOn){
                    sendMessageToActivity2(location,"pois_update");
                }
                currentLatitudeForMaps = location.getLatitude();
                currentLongitudeForMaps = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, listener);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    class myServiceBinder extends Binder{
        public GPS_Service getService(){
            return GPS_Service.this;
        }
    }

    private IBinder mBinder = new myServiceBinder();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");
        //String key = intent.getStringExtra("key");

        if(service.equals("speed_request")){
            Toast.makeText(getApplicationContext(),"Activating Speed Service",Toast.LENGTH_SHORT).show();
            isSpeedServiceOn = true;
        }
        else if(service.equals("pois_request")){
            Toast.makeText(getApplicationContext(),"Activating Location Service",Toast.LENGTH_SHORT).show();
            isPoisServiceOn = true;
        }
        return mBinder;
    }

    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        //String key = intent.getStringExtra("key");
        if(service.equals("speed_request")) {
            Toast.makeText(getApplicationContext(), "Activating Speed Service", Toast.LENGTH_SHORT).show();
            isSpeedServiceOn = true;
        }
        else{
            Toast.makeText(getApplicationContext(),"Activating Location Service",Toast.LENGTH_SHORT).show();
            isPoisServiceOn = true;
        }
    }

    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        //String key = intent.getStringExtra("key");
        if(service.equals("speed_request")){
            Toast.makeText(getApplicationContext(),"Stopping Speed Service",Toast.LENGTH_SHORT).show();
            isSpeedServiceOn = false;
        }
        else{
            Toast.makeText(getApplicationContext(),"Stopping Location Service",Toast.LENGTH_SHORT).show();
            isPoisServiceOn = false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }
}
