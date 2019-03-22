package com.myhost.spyros.location_n_accelerationdemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ViolationsClassAdapter extends  RecyclerView.Adapter<ViolationsClassAdapter.ViewHolder> {

    private int period;//periodos gia emfanisi statistikwn
    private Activity activity;
    private List<ViolationClass> violations;
    private int violations_layout;
    ViolationsClassAdapter adapter;
    RecyclerView recyclerView;
    SharedPreferences preferences;
    String pref_key = SettingsActivity.pref_key_stats_period;



    public ViolationsClassAdapter(Activity activity, List<ViolationClass> violations, int violations_layout, RecyclerView recyclerView, int period){
        this.activity = activity;
        this.violations = violations;
        this.violations_layout = violations_layout;
        this.recyclerView = recyclerView;
        this.period = period;
    }



    @NonNull
    @Override
    public ViolationsClassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate your layout and pass it to view holder
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.violations_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViolationsClassAdapter.ViewHolder viewHolder, int position) {
        //setting data to view holder elements
        if(violations.get(position).getIdentifier().equals("poi_enter")){
            viewHolder.general_info.setText("You entered "+violations.get(position).getTitle());
            viewHolder.how_many.setText(String.valueOf(violations.get(position).getTimes())+" times");
            viewHolder.time_id.setText("in last "+period+" days");
            viewHolder.location.setText("Latitude: "+violations.get(position).getLatitude()+"\nLongitude: "+violations.get(position).getLongitude());
            viewHolder.imageView.setImageResource(R.drawable.baseline_beenhere_black_48);
        }
        else if(violations.get(position).getIdentifier().equals("speed_limit")){
            viewHolder.general_info.setText("You violated speed limit");
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
            Date resultdate = new Date(violations.get(position).getTimestamp());
            viewHolder.how_many.setText("On "+resultdate.toString());
            viewHolder.time_id.setText("Speed based on limit was: "+String.valueOf(Math.round(violations.get(position).getSpeed()))+" Km");
            viewHolder.location.setText("Latitude: "+violations.get(position).getLatitude()+"\nLongitude: "+violations.get(position).getLongitude());
            viewHolder.imageView.setImageResource(R.drawable.danger);
        }

    }

    @Override
    public int getItemCount() {
        return (null != violations ? violations.size() : 0);
    }


    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView general_info;
        private TextView how_many;
        private TextView time_id;
        private TextView location;
        private View container;
        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.violations_image);
            general_info = (TextView) view.findViewById(R.id.general_info);
            how_many = (TextView) view.findViewById(R.id.how_many);
            time_id = (TextView) view.findViewById(R.id.time_id);
            location = (TextView) view.findViewById(R.id.location);
            container = view.findViewById(R.id.card_view);
        }
    }
}
