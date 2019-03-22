package com.myhost.spyros.location_n_accelerationdemo;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    CardView Speed,POIs,Statistics,Settings;
    SQLiteDatabase mDatabase;
    public static final String DATABASE_NAME = "unipimeterdatabase";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(getApplicationContext(),GPS_Service.class));
        Speed = (CardView)findViewById(R.id.Speed);
        Speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),SpeedometerActivity.class));
            }
        });

        POIs = (CardView)findViewById(R.id.POIs);
        POIs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),POIsActivity.class));
            }
        });

        Settings = (CardView)findViewById(R.id.Settings);
        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
            }
        });

        Statistics = (CardView)findViewById(R.id.Statistics);
        Statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),StatisticsActivity.class));
            }
        });

        mDatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        createTables();
    }


    private void createTables(){
        mDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS aboveSpeedLimit (timestamp long NOT NULL, speed double NOT NULL, latitude double NOT NULL, longitude double NOT NULL);"
        );

        mDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS pois (title varchar NOT NULL, description varchar NOT NULL, category varchar NOT NULL, latitude double NOT NULL, longitude double NOT NULL);"

        );

        mDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS inRadiusEnter (timestamp long NOT NULL, poi_title varchar NOT NULL, latitude double NOT NULL, longitude double NOT NULL);");


        //mDatabase.execSQL("DELETE FROM aboveSpeedLimit");
    }
}
