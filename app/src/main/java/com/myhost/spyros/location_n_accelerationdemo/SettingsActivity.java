package com.myhost.spyros.location_n_accelerationdemo;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    Button speed_limit_btn, radius_btn, statistics_limit_btn;
    int number_picker_value = 0;//gia radius
    int speed_number_picker_value = 0;//gia speed_limit
    int stats_limit_number_picker_value = 1;
    //pairnei ta preferences gia to radius
    SharedPreferences preferences;

    //pairnei ta preferences gia to speed limit
    SharedPreferences speed_preferences;

    //gets preferences for stats period
    SharedPreferences stats_preferences;

    //shared preferences fields for radius
    public static String pref_key_radius = "radius";
    public static String np_value = "np_value";

    //shared preferences fields for speed limit
    public static String pref_key_speed_limit = "speed_limit";
    public static String speed_limit_value = "speed_limit_value";

    //shared preferences fields for statistics period
    public static String pref_key_stats_period = "stats_period";
    public static String stats_period = "stats_period_value";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //init preferences
        preferences = getSharedPreferences(pref_key_radius, Context.MODE_PRIVATE);
        speed_preferences = getSharedPreferences(pref_key_speed_limit, Context.MODE_PRIVATE);
        stats_preferences = getSharedPreferences(pref_key_stats_period, Context.MODE_PRIVATE);

        speed_limit_btn = (Button) findViewById(R.id.speed_limit_btn);
        radius_btn = (Button) findViewById(R.id.radius_btn);
        statistics_limit_btn = (Button) findViewById(R.id.statistics_limit_btn);

        radius_btn.setOnClickListener(onRadiusClick());
        speed_limit_btn.setOnClickListener(onSpeedLimitClick());
        statistics_limit_btn.setOnClickListener(onStatsLimitClick());


        //number_picker_value = preferences.getInt(np_value,0);

        //NA PAINREI APO SHARED PREFERENCES TO NUMBER PICKER

    }


    private View.OnClickListener onRadiusClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(SettingsActivity.this);
                dialog.setContentView(R.layout.radius_change);
                dialog.setCancelable(true);

                // set the custom dialog components - texts and image
                Button save_btn = (Button) dialog.findViewById(R.id.radius_save_btn);
                final NumberPicker np = (NumberPicker) dialog.findViewById(R.id.numberPicker);
                np.setMaxValue(2000);
                np.setMinValue(0);
                np.setValue(preferences.getInt(np_value, 0));//NA TO DIAVAZEI APO TO SHARED PREFERENCES
                np.setWrapSelectorWheel(true);


                save_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //put number picker value in shared preferences
                        number_picker_value = np.getValue();
                        preferences = getSharedPreferences(pref_key_radius, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt(np_value, np.getValue());
                        editor.commit();
                        //np.setValue(np.getValue());
                        //Toast.makeText(getApplicationContext(), String.valueOf(np.getValue()), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        };


    }


    private View.OnClickListener onSpeedLimitClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(SettingsActivity.this);
                dialog.setContentView(R.layout.speed_limit_change);
                dialog.setCancelable(true);

                // set the custom dialog components - texts and image
                Button save_btn = (Button) dialog.findViewById(R.id.speed_limit_save_btn);
                final NumberPicker np = (NumberPicker) dialog.findViewById(R.id.speed_limit_numberPicker);
                np.setMaxValue(100);
                np.setMinValue(0);
                np.setValue(speed_preferences.getInt(speed_limit_value, 0));//NA TO DIAVAZEI APO TO SHARED PREFERENCES
                np.setWrapSelectorWheel(true);


                save_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //put number picker value in shared preferences
                        speed_number_picker_value = np.getValue();
                        speed_preferences = getSharedPreferences(pref_key_speed_limit, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = speed_preferences.edit();
                        editor.putInt(speed_limit_value, np.getValue());
                        editor.commit();
                        //np.setValue(np.getValue());
                        //Toast.makeText(getApplicationContext(), String.valueOf(np.getValue()), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        };


    }


    private View.OnClickListener onStatsLimitClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(SettingsActivity.this);
                dialog.setContentView(R.layout.stats_limit_change);
                dialog.setCancelable(true);

                // set the custom dialog components - texts and image
                Button save_btn = (Button) dialog.findViewById(R.id.stats_limit_save_btn);
                final NumberPicker np = (NumberPicker) dialog.findViewById(R.id.stats_limit_numberPicker);
                np.setMaxValue(20);
                np.setMinValue(1);
                np.setValue(stats_preferences.getInt(stats_period, 0));//NA TO DIAVAZEI APO TO SHARED PREFERENCES
                np.setWrapSelectorWheel(true);


                save_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //put number picker value in shared preferences
                        stats_limit_number_picker_value = np.getValue();
                        stats_preferences = getSharedPreferences(pref_key_stats_period, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = stats_preferences.edit();
                        editor.putInt(stats_period, np.getValue());
                        editor.commit();
                        //np.setValue(np.getValue());
                        //Toast.makeText(getApplicationContext(), String.valueOf(np.getValue()), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        };

    }
}