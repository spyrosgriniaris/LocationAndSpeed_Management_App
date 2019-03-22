package com.myhost.spyros.location_n_accelerationdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class StatisticsActivity extends AppCompatActivity {

    public RecyclerView recyclerView;
    private ArrayList<ViolationClass> violations;
    private ViolationsClassAdapter adapter;

    SQLiteDatabase mDatabase;

    SharedPreferences preferences;

    private long DAY_IN_MS = 1000 * 60 * 60 * 24;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        String pref_key = SettingsActivity.pref_key_stats_period;
        preferences = getSharedPreferences(pref_key,Context.MODE_PRIVATE);

        //initialize arraylist to store statistics
        violations = new ArrayList<>();

        //init recycler view
        recyclerView = (RecyclerView)findViewById(R.id.statistics_recyler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //opening database
        mDatabase = openOrCreateDatabase(POIsActivity.DATABASE_NAME, MODE_PRIVATE, null);
        //createinRadiusEnterTable();
        int period = preferences.getInt(SettingsActivity.stats_period, 1);
        setRecyclerViewData(); //adding data to array list
        adapter = new ViolationsClassAdapter(this, violations,R.layout.violations_layout,recyclerView,period);
        recyclerView.setAdapter(adapter);
    }


//    private void createinRadiusEnterTable(){
//        mDatabase.execSQL(
//                "CREATE TABLE IF NOT EXISTS inRadiusEnter (timestamp long NOT NULL, poi_title varchar NOT NULL, latitude double NOT NULL, longitude double NOT NULL);"
//        );
//    }


    private ArrayList<String> pois_have_examined = new ArrayList<>();
    private void setRecyclerViewData(){
        int limit = preferences.getInt(SettingsActivity.stats_period,10);
        int times = 0;
        String title;

        //take POI radius entering ----------------------------------------------------------------
        Cursor cursorPOIS = mDatabase.rawQuery("SELECT * FROM pois;",null);
        //if the cursor has some data
        if(cursorPOIS.moveToFirst()){
            //looping through all records
            do{
                //pairnw ton titlo kai kanw count ksana sti vasi gia to sugkekrimeno poi gia tis teleutaies 10 meres
                title = cursorPOIS.getString(0);
                if(!pois_have_examined.contains(title))
                {
                    pois_have_examined.add(title);
                    long lastdays = System.currentTimeMillis() - (limit*DAY_IN_MS);
                    Cursor newCursorPOIS = mDatabase.rawQuery("SELECT COUNT(*) FROM inRadiusEnter WHERE poi_title = '"+title+"' AND timestamp >= '"+lastdays+"' ;",null);
                    if(newCursorPOIS.moveToFirst()){
                        times = newCursorPOIS.getInt(0);
                        if(times >0){
                            violations.add(new ViolationClass(
                                    title,
                                    0,//timestamp
                                    0,//speed
                                    cursorPOIS.getDouble(3),//latitude
                                    cursorPOIS.getDouble(4),//longitude
                                    "poi_enter",//identifier
                                    times
                            ));
                        }
                    }
                    newCursorPOIS.close();

                }
            }while (cursorPOIS.moveToNext());
        }
        cursorPOIS.close();

        //end of POI radius entering data collection-----------------------------------------------

        //take speed limit violations--------------------------------------------------------------
        long lastdays = System.currentTimeMillis() - (limit*DAY_IN_MS);
        Cursor cursorSpeed = mDatabase.rawQuery("SELECT * FROM aboveSpeedLimit WHERE timestamp >= '"+lastdays+"';",null);
        if(cursorSpeed.moveToFirst()){
            do{
                violations.add(new ViolationClass(
                        "",
                        cursorSpeed.getLong(0),
                        cursorSpeed.getDouble(1),
                        cursorSpeed.getDouble(2),
                        cursorSpeed.getDouble(3),
                        "speed_limit",
                        0
                ));

            }while (cursorSpeed.moveToNext());
        }
        cursorSpeed.close();
    }
}
