package com.myhost.spyros.location_n_accelerationdemo;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.anastr.speedviewlib.SpeedView;

public class SpeedometerActivity extends AppCompatActivity /*implements LocationListener*/ {
    //declaring a location manager
    protected LocationManager locationManager;
    SharedPreferences preferences;
    SQLiteDatabase mDatabase;
    SpeedView speedometer;
    private Button start_speed_service;
    private Button stop_speed_service;

    //variables for speed limit notification
    final int REQUEST_CODE = 0;
    final String NOTIFICATION_CHANNEL_ID = "my_chanel_id_0";

    //metavliti pou tha elegxei kathe stigmi an einai mesa sta oria i taxutita
    boolean isStillAboveLimit = false;

    private GPS_Service speedService;
    private boolean isServiceBound = false;
    private ServiceConnection serviceConnection;
    private Intent speedServiceIntent;




    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getBundleExtra("Location");
            Location loc = (Location) b.getParcelable("Location");
            if(loc.hasSpeed()){
                speedometer.setSpeedAt(loc.getSpeed());
                checkSpeedRestrictions(loc);
            }
            else
                speedometer.setSpeedAt(0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                mMessageReceiver, new IntentFilter("speed_update"));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speedometer);

        //creating a database
        mDatabase = openOrCreateDatabase(POIsActivity.DATABASE_NAME, MODE_PRIVATE, null);
        //createSpeedLimitTable();

        String pref_key = SettingsActivity.pref_key_speed_limit;
        preferences = getSharedPreferences(pref_key,Context.MODE_PRIVATE);

        speedometer = (SpeedView) findViewById(R.id.speedometer);
        speedServiceIntent = new Intent(SpeedometerActivity.this,GPS_Service.class);
        speedServiceIntent.putExtra("key","speed_request");

        start_speed_service = (Button) findViewById(R.id.start_speed_service);
        stop_speed_service = (Button) findViewById(R.id.stop_speed_service);
        start_speed_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GPS_Service.service = "speed_request";
                bindService();

            }
        });

        stop_speed_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unbindService();
                //GPS_Service.service = null;
                speedometer.setSpeedAt(0);
            }
        });
    }

    protected void onResume() {
        //bindService(speedServiceIntent,serviceConnection,Context.BIND_AUTO_CREATE);
        isServiceBound = true;
        GPS_Service.service = "speed_request";
        registerReceiver(mMessageReceiver, new IntentFilter("speed_update"));
        super.onResume();
    }


    @Override
    protected void onPause() {
        unbindService();
        isServiceBound = false;
        //GPS_Service.service = "";
        LocalBroadcastManager.getInstance(SpeedometerActivity.this).unregisterReceiver(
                mMessageReceiver);
        //unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unbindService();
        isServiceBound = false;
        //GPS_Service.service = "";
        LocalBroadcastManager.getInstance(SpeedometerActivity.this).unregisterReceiver(
                mMessageReceiver);
        //unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    private void bindService(){
        if(serviceConnection == null){
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    GPS_Service.myServiceBinder myServiceBinder = (GPS_Service.myServiceBinder)iBinder;
                    speedService = myServiceBinder.getService();
                    isServiceBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    isServiceBound = false;
                }
            };
        }
        GPS_Service.service = "speed_request";
        bindService(speedServiceIntent,serviceConnection,Context.BIND_AUTO_CREATE);
    }

    private void unbindService(){
        if(isServiceBound && serviceConnection != null){
            unbindService(serviceConnection);
            isServiceBound = false;
            //GPS_Service.service = null;
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 2://2 is code for request permissions
                if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(),"Service Starting..",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Permission not given",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }
                //NA VALW SE SHAREDPREFERENCES TO AN DEXTIKE O XRISTIS I OXI
        }
    }


    public void checkSpeedRestrictions(Location location){
        int limit = preferences.getInt(SettingsActivity.speed_limit_value,40);
        if(location.hasSpeed()){
            double current_speed = location.getSpeed();
            double current_latitude = location.getLatitude();
            double current_longitude = location.getLongitude();
            if(location.getSpeed() > limit && isStillAboveLimit == false){
                isStillAboveLimit = true;
                addNotification();
                addRecordWhenAboveSpeedLimit(current_speed,current_latitude,current_longitude);
            }
            else if(location.getSpeed() > limit){
                isStillAboveLimit = true;
            }
            else{
                isStillAboveLimit = false;
            }
        }
        else{
            speedometer.setSpeedAt(0);
            isStillAboveLimit = false;
        }
    }


    //adds notification when current speed is above the limit
    private void addNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Speed Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Speed Limit");
            notificationChannel.enableLights(true);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Speed Limit notification")
                .setContentText("You passed Speed Limit")
                .setContentInfo("Info");

        notificationManager.notify(REQUEST_CODE, notificationBuilder.build());

    }

    //adds record to database when speed is above the limit
    private void addRecordWhenAboveSpeedLimit(double speed, double latitude, double longitude){
        String insSQL = "INSERT INTO aboveSpeedLimit(timestamp, speed, latitude, longitude) VALUES ('"+System.currentTimeMillis()+"','"+speed+"','"+latitude+"','"+longitude+"')";
        mDatabase.execSQL(insSQL);
        Toast.makeText(getApplicationContext(),"Passed Speed Limit",Toast.LENGTH_SHORT).show();
    }

}
