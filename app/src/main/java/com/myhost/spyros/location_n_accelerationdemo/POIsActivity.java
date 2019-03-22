package com.myhost.spyros.location_n_accelerationdemo;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
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
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class POIsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PointOfInterestAdapter adapter;
    public static ArrayList<PointOfInterest> pointArrayList;
    private FloatingActionButton fab;
    private boolean home, work, other;
    private Button btn_start, btn_stop,map;
    public static int number_of_points;//counts how many points there are in database
    public static Activity activityForMaps;

    SQLiteDatabase mDatabase;

    //database
    public static final String DATABASE_NAME = "unipimeterdatabase";

    //variables for adding a POI to database
    EditText titleEditText, descriptionEditText, latitudeEditText, longitudeEditText;
    Spinner spinnerCategory;

    SharedPreferences preferences;

    private GPS_Service poisService;
    private boolean isServiceBound = false;
    private ServiceConnection serviceConnection;
    private Intent poisServiceIntent;

    final int REQUEST_CODE = 1;
    final String NOTIFICATION_CHANNEL_ID = "my_chanel_id_1";

    public static int[][] notification_manager_array;
    public static int radius = 0;

    int numMessages = 0;
    int notification_request_code;


    @Override
    protected void onResume() {
        Toast.makeText(getApplicationContext(),"onResume in POIS",Toast.LENGTH_SHORT);
        adapter.notifyDataSetChanged();
        GPS_Service.service = "pois_request";
        LocalBroadcastManager.getInstance(POIsActivity.this).registerReceiver(mMessageReceiver, new IntentFilter("pois_update"));
        super.onResume();
    }
    @Override
    protected void onPause() {
        Toast.makeText(getApplicationContext(),"onPause in POIS",Toast.LENGTH_SHORT);
        unbindService();
        isServiceBound = false;
        LocalBroadcastManager.getInstance(POIsActivity.this).unregisterReceiver(
                mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(getApplicationContext(),"onDestroy in POIS",Toast.LENGTH_SHORT);
        unbindService();
        isServiceBound = false;
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                mMessageReceiver);
        super.onDestroy();

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String pref_key = SettingsActivity.pref_key_radius;
            preferences = getSharedPreferences(pref_key, Context.MODE_PRIVATE);
            radius = preferences.getInt(SettingsActivity.np_value, 500);// 500 is default value if no value exists in SettingActivity.np_value
            Bundle b = intent.getBundleExtra("Location");
            Location loc = (Location) b.getParcelable("Location");
            checkIfInRadius(loc.getLatitude(),loc.getLongitude(),radius);
            adapter = new PointOfInterestAdapter(POIsActivity.this, pointArrayList,mDatabase,R.layout.pois_layout,recyclerView,loc.getLatitude(),loc.getLongitude());
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pois);
        mDatabase = openOrCreateDatabase(POIsActivity.DATABASE_NAME, MODE_PRIVATE, null);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                mMessageReceiver, new IntentFilter("pois_update"));
        poisServiceIntent = new Intent(POIsActivity.this,GPS_Service.class);
        poisServiceIntent.putExtra("key","pois_request");

        //Intent intent = getIntent();
        String pref_key = SettingsActivity.pref_key_radius;
        preferences = getSharedPreferences(pref_key,Context.MODE_PRIVATE);


        pointArrayList = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.recyle_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //creating a database
        mDatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        //createPOIsTable();

        setRecyclerViewData(); //adding data to array list
        adapter = new PointOfInterestAdapter(this, pointArrayList,mDatabase,R.layout.pois_layout,recyclerView,0,0);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(onAddingListener());



        titleEditText = (EditText)findViewById(R.id.titleEditText);
        descriptionEditText = (EditText)findViewById(R.id.descriptionEditText);
        latitudeEditText = (EditText)findViewById(R.id.latitudeEditText);
        longitudeEditText = (EditText)findViewById(R.id.longitudeEditText);
        spinnerCategory = (Spinner)findViewById(R.id.categorySpinner);


        btn_start = (Button) findViewById(R.id.start_service);
        btn_stop = (Button) findViewById(R.id.stop_service);
        if(!runtime_permissions())
            enable_buttons();

        map = (Button) findViewById(R.id.map);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MapsActivity.class));
            }
        });
        activityForMaps = this;
        Toast.makeText(getApplicationContext(),"onCreate in POIS",Toast.LENGTH_SHORT);

    }



    private void bindService(){
        if(serviceConnection == null){
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    GPS_Service.myServiceBinder myServiceBinder = (GPS_Service.myServiceBinder)iBinder;
                    poisService = myServiceBinder.getService();
                    isServiceBound = true;
                    fab.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    isServiceBound = false;
                }
            };
        }
        poisServiceIntent = new Intent(POIsActivity.this,GPS_Service.class);
        poisServiceIntent.putExtra("key","pois_request");
        LocalBroadcastManager.getInstance(POIsActivity.this).registerReceiver(
                mMessageReceiver, new IntentFilter("pois_update"));
        bindService(poisServiceIntent,serviceConnection,Context.BIND_AUTO_CREATE);

        //setRecyclerViewData(); //adding data to array list
        adapter.notifyDataSetChanged();

    }

    private void unbindService(){
        if(isServiceBound && serviceConnection != null){
            unbindService(serviceConnection);
            fab.setVisibility(View.VISIBLE);
            //setRecyclerViewData(); //adding data to array list
            LocalBroadcastManager.getInstance(POIsActivity.this).unregisterReceiver(
                    mMessageReceiver);
            isServiceBound = false;
            if(notification_manager_array != null){
                for(int i = 0; i< notification_manager_array.length;i++){
                    notification_manager_array[i][1] = 0;
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void enable_buttons() {

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GPS_Service.service = "pois_request";
                bindService();
                init_pois_table();
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unbindService();
            }
        });

    }

    private void init_pois_table() {
        notification_manager_array = new int[POIsActivity.number_of_points][2];
        for (int i = 0; i < POIsActivity.number_of_points; i++) {
            notification_manager_array[i][0] = i;
            notification_manager_array[i][1] = 0;
        }
    }

    private void checkIfInRadius(double current_latitude,  double current_longitude, int radius){
        //Toast.makeText(getApplicationContext(),String.valueOf(number_of_points),Toast.LENGTH_SHORT).show();
        Cursor cursorPOIs = mDatabase.rawQuery("SELECT * FROM pois",null);
        //define curent location
        Location current_location = new Location("Current point");
        current_location.setLatitude(current_latitude);
        current_location.setLongitude(current_longitude);

        //define point from database
        Location point = new Location("Point");

        int counter = 0;
        //if the cursor has some data
        if(cursorPOIs.moveToFirst()){


            //looping through all records
            do{
                String title = cursorPOIs.getString(0);
                double pois_latitude = cursorPOIs.getDouble(3);
                double pois_longitude = cursorPOIs.getDouble(4);
                point.setLatitude(pois_latitude);
                point.setLongitude(pois_longitude);
                float distance = current_location.distanceTo(point);
                if(distance <= (float) radius && notification_manager_array[counter][1] == 0){
                    numMessages+=1;
                    notification_request_code = numMessages;
                    insideRadiusNotification(title,notification_request_code);
                    addEnterToDatabase(title,current_latitude,current_longitude);
                    notification_manager_array[counter][1] = 1;//an paramenei stin aktina
                }
                else if(distance <= (float) radius){

                }
                else{
                    notification_manager_array[counter][1] = 0;//an vgike apo tin aktina
                }
                counter++;
            }while (cursorPOIs.moveToNext());
        }
        cursorPOIs.close();
    }

    private void insideRadiusNotification(String title,int notification_request_code){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Speed Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("POI Entering");
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
                .setContentTitle("POI Entering")
                .setContentText("You Entered "+title)
                .setContentInfo("Info");

        notificationManager.notify(notification_request_code, notificationBuilder.build());

    }

    private void addEnterToDatabase(String poi_title, double current_latitude, double current_longitude){
        String insSQL = "INSERT INTO inRadiusEnter(timestamp, poi_title, latitude, longitude) VALUES ('"+System.currentTimeMillis()+"','"+poi_title+"','"+current_latitude+"','"+current_longitude+"')";
        mDatabase.execSQL(insSQL);
        Toast.makeText(getApplicationContext(),"Entered "+poi_title,Toast.LENGTH_SHORT).show();
    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                enable_buttons();
            }else {
                runtime_permissions();
            }
        }
    }


    private void addPOI(String title, String description,String category, double latitude, double longitude){

        boolean home,work,other;
        if(category.equals("Home")){
            home = true;
            work = false;
            other = false;
        }
        else if(category.equals("Work")){
            home = false;
            work = true;
            other = false;
        }
        else{
            home = false;
            work = false;
            other = true;
        }

        //validating inputs
        try {
            if (inputsAreCorrect(title, description, latitude, longitude)) {
                String insSQL = "INSERT INTO pois (title, description, category, latitude, longitude) VALUES ('" + title + "','" + description + "','" + category + "','" + latitude + "','" + longitude + "')";

                mDatabase.execSQL(insSQL);

                PointOfInterest point = new PointOfInterest(title, description, home, work, other, latitude, longitude);
                pointArrayList.add(point);
                number_of_points = pointArrayList.toArray().length;
                //PointOfInterestAdapter.points.add(point);
                int temp_i = 0;
                int temp [][] = new int[notification_manager_array.length+1][2];
                for(int i = 0; i < notification_manager_array.length; i++){
                    temp[i][1] = notification_manager_array[i][1];
                    temp_i = i;
                }
                temp[temp_i+1][1] = 0;
                notification_manager_array = temp;
                adapter.notifyDataSetChanged();
            }
        }
        catch(Exception e){

        }

    }


    //method to check inputs for adding a POI
    private boolean inputsAreCorrect(String title, String description, double latitude, double longitude){
        if(title.isEmpty()){
            titleEditText.setError("Please enter a title");
            titleEditText.requestFocus();
            return false;
        }
        else if(description.isEmpty()){
            descriptionEditText.setError("Please Enter a description");
            descriptionEditText.requestFocus();
            return false;
        }
        else if(String.valueOf(latitude).isEmpty()){
            latitudeEditText.setError("Please Enter Latitude");
            latitudeEditText.requestFocus();
            return false;
        }
        else if(String.valueOf(longitude).isEmpty()){
            longitudeEditText.setError("Please Enter Longitude");
            longitudeEditText.requestFocus();
            return false;
        }
        return true;
    }



    private void setRecyclerViewData() {
        //we used rawQuery(sql, selectionargs) for fetching all the employees
        Cursor cursorPOIS = mDatabase.rawQuery("SELECT * FROM pois", null);

        //if the cursor has some data
        if(cursorPOIS.moveToFirst()){
            //looping through all records
            do{
                String category = cursorPOIS.getString(2);
                if(category.equals("Home")){
                    home = true;
                    work = false;
                    other = false;
                }
                else if(category.equals("Work")){
                    home = false;
                    work = true;
                    other = false;
                }
                else{
                    home = false;
                    work = false;
                    other = true;
                }

                //pushing each record to pointArrayList
                pointArrayList.add(new PointOfInterest(
                        cursorPOIS.getString(0),
                        cursorPOIS.getString(1),
                        home,work,other,
                        cursorPOIS.getDouble(3),
                        cursorPOIS.getDouble(4)
                ));

            }while (cursorPOIS.moveToNext());
        }
        cursorPOIS.close();
        number_of_points = pointArrayList.toArray().length;




    }

    private View.OnClickListener onAddingListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(POIsActivity.this);
                dialog.setContentView(R.layout.dialog_add); //layout for dialog
                dialog.setTitle("Add a new POI");
                dialog.setCancelable(false); //none-dismiss when touching outside Dialog

                // set the custom dialog components - texts and image
                EditText title = (EditText) dialog.findViewById(R.id.titleEditText);
                EditText description = (EditText) dialog.findViewById(R.id.descriptionEditText);
                EditText latitude = (EditText) dialog.findViewById(R.id.latitudeEditText);
                EditText longitude = (EditText) dialog.findViewById(R.id.longitudeEditText);
                Spinner spnCategory = (Spinner) dialog.findViewById(R.id.categorySpinner);
                View btnAdd = dialog.findViewById(R.id.btn_ok);
                View btnCancel = dialog.findViewById(R.id.btn_cancel);

                //set spinner adapter
                ArrayList<String> categoryList = new ArrayList<>();
                categoryList.add("Home");
                categoryList.add("Work");
                categoryList.add("Other");
                ArrayAdapter<String> spnAdapter = new ArrayAdapter<String>(POIsActivity.this,
                        android.R.layout.simple_dropdown_item_1line, categoryList);
                spnCategory.setAdapter(spnAdapter);

                //set handling event for 2 buttons and spinner
                spnCategory.setOnItemSelectedListener(onItemSelectedListener());
                btnAdd.setOnClickListener(onConfirmListener(title, description,latitude,longitude,spnCategory, dialog));
                btnCancel.setOnClickListener(onCancelListener(dialog));

                dialog.show();
            }
        };
    }

    private AdapterView.OnItemSelectedListener onItemSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                if (position == 0) {
                    home = true;
                    work = false;
                    other = false;
                }
                else if (position == 1){
                    work = true;
                    home = false;
                    other = false;
                }
                else {
                    other = true;
                    home = false;
                    work = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView parent) {

            }
        };
    }

    private View.OnClickListener onConfirmListener(final EditText title, final EditText description,final EditText latitude,final EditText longitude,final Spinner spnCategory, final Dialog dialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{
                    String titleToAdd = title.getText().toString();
                    String descriptionToAdd = description.getText().toString();
                    double latitudeToAdd = Double.parseDouble(latitude.getText().toString());
                    double longitudeToAdd = Double.parseDouble(longitude.getText().toString());
                    String categoryToAdd = spnCategory.getSelectedItem().toString();
                    addPOI(titleToAdd,descriptionToAdd,categoryToAdd,latitudeToAdd,longitudeToAdd);
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Not valid info given",Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();

                dialog.dismiss();

            }
        };
    }


    private View.OnClickListener onCancelListener(final Dialog dialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        };
    }
}

