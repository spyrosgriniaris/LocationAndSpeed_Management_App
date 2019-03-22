package com.myhost.spyros.location_n_accelerationdemo;

import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double latitudeToShow,longitudeToShow;
    String chooseWhatToShow;
    boolean home,work,other;
    SQLiteDatabase mDatabase;
    //database
    public static final String DATABASE_NAME = "unipimeterdatabase";

    //variables for adding a POI to database
    EditText titleEditText, descriptionEditText, latitudeEditText, longitudeEditText;
    Spinner spinnerCategory;

//    private RecyclerView recyclerView;
//    private PointOfInterestAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();

        latitudeToShow = intent.getDoubleExtra("latitude",GPS_Service.currentLatitudeForMaps);
        longitudeToShow = intent.getDoubleExtra("longitude",GPS_Service.currentLongitudeForMaps);
        chooseWhatToShow = intent.getStringExtra("show");
        mDatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        titleEditText = (EditText)findViewById(R.id.titleEditText);
        descriptionEditText = (EditText)findViewById(R.id.descriptionEditText);
        latitudeEditText = (EditText)findViewById(R.id.latitudeEditText);
        longitudeEditText = (EditText)findViewById(R.id.longitudeEditText);
        spinnerCategory = (Spinner)findViewById(R.id.categorySpinner);

//        recyclerView = (RecyclerView) findViewById(R.id.recyle_view);
//        recyclerView.setHasFixedSize(true);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(POIsActivity.activityForMaps);
//        recyclerView.setLayoutManager(layoutManager);
        //Toast.makeText(getApplicationContext(),"mpike",Toast.LENGTH_SHORT).show();
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
        LatLng point;
        if(latitudeToShow == 0 || longitudeToShow == 0){
            latitudeToShow = 37.982036;
            longitudeToShow = 23.725977;
            Toast.makeText(getApplicationContext(),"No GPS signal availiable. Try again later.",Toast.LENGTH_SHORT).show();
        }
        point = new LatLng(latitudeToShow,longitudeToShow);
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(point).title("Point"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,14F));
        setMapLongClick(mMap);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setMapLongClick(final GoogleMap map) {
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String snippet = String.format(Locale.getDefault(),
                        "Lat: %1$.5f, Long: %2$.5f",
                        latLng.latitude,
                        latLng.longitude);
                map.addMarker(new MarkerOptions().position(latLng).title("new point").snippet(snippet));
                //Toast.makeText(getApplicationContext(),
                //        String.valueOf(latLng.latitude)+""+String.valueOf(latLng.longitude),Toast.LENGTH_SHORT).show();
                final Dialog dialog = new Dialog(MapsActivity.this);
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

                latitude.setText(String.valueOf(latLng.latitude));
                longitude.setText(String.valueOf(latLng.longitude));
                latitude.setActivated(false);
                longitude.setActivated(false);

                //set spinner adapter
                ArrayList<String> categoryList = new ArrayList<>();
                categoryList.add("Home");
                categoryList.add("Work");
                categoryList.add("Other");
                ArrayAdapter<String> spnAdapter = new ArrayAdapter<String>(MapsActivity.this,
                        android.R.layout.simple_dropdown_item_1line, categoryList);
                spnCategory.setAdapter(spnAdapter);

                //set handling event for 2 buttons and spinner
                spnCategory.setOnItemSelectedListener(onItemSelectedListener());
                btnAdd.setOnClickListener(onConfirmListener(title, description,latitude,longitude,spnCategory, dialog));
                btnCancel.setOnClickListener(onCancelListener(dialog));

                dialog.show();

            }
        });
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

                //adapter.notifyDataSetChanged();

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
                POIsActivity.pointArrayList.add(point);
                POIsActivity.number_of_points = POIsActivity.pointArrayList.toArray().length;
                //PointOfInterestAdapter.points.add(point);
                int temp_i = 0;
                int temp [][] = new int[POIsActivity.notification_manager_array.length+1][2];
                for(int i = 0; i < POIsActivity.notification_manager_array.length; i++){
                    temp[i][1] = POIsActivity.notification_manager_array[i][1];
                    temp_i = i;
                }
                temp[temp_i+1][1] = 0;
                POIsActivity.notification_manager_array = temp;
                startActivity(new Intent(getApplicationContext(),POIsActivity.class));
                //adapter.notifyDataSetChanged();
            }
        }
        catch(Exception e){

        }

    }

    private boolean inputsAreCorrect(String title, String description, double latitude, double longitude){
        boolean addIsOk = false;
        if(title.isEmpty()){
//            titleEditText.setError("Please enter a title");
//            titleEditText.requestFocus();
//            return false;
            addIsOk = false;
        }
        else if(description.isEmpty()){
//            descriptionEditText.setError("Please Enter a description");
//            descriptionEditText.requestFocus();
//            return false;
            addIsOk = false;
        }
        else if(String.valueOf(latitude).isEmpty()){
//            latitudeEditText.setError("Please Enter Latitude");
//            latitudeEditText.requestFocus();
//            return false;
            addIsOk = false;
        }
        else if(String.valueOf(longitude).isEmpty()){
//            longitudeEditText.setError("Please Enter Longitude");
//            longitudeEditText.requestFocus();
//            return false;
            addIsOk = false;
        }
        else
            addIsOk = true;

        if(addIsOk){
            Toast.makeText(getApplicationContext(),"Point Entered",Toast.LENGTH_LONG).show();
            return true;
        }
        else{
            Toast.makeText(getApplicationContext(),"Check Your Inputs",Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
