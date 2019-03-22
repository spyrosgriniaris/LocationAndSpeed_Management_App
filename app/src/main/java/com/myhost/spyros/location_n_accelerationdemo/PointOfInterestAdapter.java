package com.myhost.spyros.location_n_accelerationdemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.content.ContextCompat.startActivity;

public class PointOfInterestAdapter extends RecyclerView.Adapter<PointOfInterestAdapter.ViewHolder> {

    private List<PointOfInterest> points;// it's pointArrayList from POIsAActivity
    private Activity activity;
    private boolean home, work, other;
    SQLiteDatabase mDatabase;
    private int pois_layout;
    PointOfInterestAdapter adapter;
    RecyclerView recyclerView;
    private double current_latitude, current_longitude;
    SharedPreferences preferences;



    public PointOfInterestAdapter(Activity activity, List<PointOfInterest> points,SQLiteDatabase mDatabase, int pois_layout, RecyclerView recyclerView, double latitude, double longitude) {
        this.points = points;
        this.activity = activity;
        this.mDatabase = mDatabase;
        this.pois_layout = pois_layout;
        this.recyclerView = recyclerView;
        this.current_latitude = latitude;
        this.current_longitude = longitude;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        //inflate your layout and pass it to view holder
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.pois_layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PointOfInterestAdapter.ViewHolder viewHolder, int position) {
        boolean home,work,other;
        String title, description;
        double latitude, longitude;
        //setting data to view holder elements
        viewHolder.title.setText(points.get(position).getTitle());
        viewHolder.description.setText("Description: "+points.get(position).getDescription());
        viewHolder.latitude.setText("Latitude: "+String.valueOf(points.get(position).getLatitude()));
        viewHolder.longitude.setText("Longitude: "+String.valueOf(points.get(position).getLongitude()));
        Location current_loc = new Location("current_location");
        current_loc.setLatitude(current_latitude);
        current_loc.setLongitude(current_longitude);
        Location point = new Location("point");
        point.setLatitude(points.get(position).getLatitude());
        point.setLongitude(points.get(position).getLongitude());
        float distance = current_loc.distanceTo(point);
        if(POIsActivity.notification_manager_array!= null && POIsActivity.notification_manager_array[position][1] == 1){
            viewHolder.distance.setText("Point is inside radius");
            viewHolder.container.getBackground().setTint(Color.GREEN);
        }
        else if(current_latitude == 0 || current_longitude == 0){
            viewHolder.distance.setText("Distance from radius: ---");
        }
        else{
            viewHolder.distance.setText(String.valueOf(Math.round(distance))+" meters away from point's radius");
            viewHolder.container.getBackground().setTint(Color.WHITE);
        }





        //times pou eixe to poi prin to edit
        title = viewHolder.title.getText().toString();
        description = points.get(position).getDescription();
        latitude = Double.parseDouble(String.valueOf(points.get(position).getLatitude()));
        longitude = Double.parseDouble(String.valueOf(points.get(position).getLongitude()));



        if (points.get(position).isHome()) {
            viewHolder.imageView.setImageResource(R.drawable.baseline_home_black_48);
            home = true;
            work = false;
            other = false;
        }
        else if(points.get(position).isWork()){
            viewHolder.imageView.setImageResource(R.drawable.sharp_business_black_48);
            home = false;
            work = true;
            other = false;
        }
        else {
            viewHolder.imageView.setImageResource(R.drawable.baseline_loupe_black_48);
            home = false;
            work = false;
            other = true;
        }
        //set on click listener for each element
        viewHolder.container.setOnClickListener(onClickListener(position));
        viewHolder.edit_btn.setOnClickListener(onClickListener(viewHolder.edit_btn,title,description,latitude,longitude));
        viewHolder.delete_btn.setOnClickListener(onClickListener(viewHolder.delete_btn,title,description,latitude,longitude));
        viewHolder.show_btn.setOnClickListener(onClickListener(viewHolder.show_btn,title,description,latitude,longitude));

    }



    //shows data in dialog box when clicking in item
    private void setDataToView(TextView title, TextView description,TextView latitude, TextView longitude, ImageView Icon,Button edit_btn, Button delete_btn, int position) {
        //Toast.makeText(activity,"Description: "+String.valueOf(points.get(position).getDescription())+"\n"+"Longitude: "+String.valueOf(points.get(position).getLongitude()),Toast.LENGTH_SHORT).show();
        title.setText(points.get(position).getTitle());
        description.setText("Description: "+points.get(position).getDescription());
        latitude.setText(String.valueOf("Latitude: "+points.get(position).getLatitude()));
        longitude.setText(String.valueOf("Longitude: "+points.get(position).getLongitude()));
        final String old_title = points.get(position).getTitle();
        final String old_description = points.get(position).getDescription();
        final double old_latitude = points.get(position).getLatitude();
        final double old_longitude = points.get(position).getLongitude();
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePOI(old_title,old_description,old_latitude,old_longitude);
            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePOI(old_title,old_description,old_latitude,old_longitude);
            }
        });
//        show_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showPOI(old_latitude,old_longitude);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return (null != points ? points.size() : 0);
    }



    private View.OnClickListener onClickListener(View view,final String oldTitle,final String oldDescription, final double old_latitude, final double old_longitude){
        return new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(v.getId() == (R.id.edit_btn)){
                    //Toast.makeText(activity,"editttt",Toast.LENGTH_SHORT).show();
                    updatePOI(oldTitle,oldDescription,old_latitude,old_longitude);
                }
                else if(v.getId() == (R.id.delete_btn)){
                    deletePOI(oldTitle,oldDescription,old_latitude,old_longitude);
                }
                else if(v.getId() == (R.id.show_btn)){
                    //Toast.makeText(activity,"showww",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity,MapsActivity.class);
                    intent.putExtra("latitude",old_latitude);
                    intent.putExtra("longitude",old_longitude);
                    intent.putExtra("show","point_show");//gia na elegxei an tha deiksei topothesia i point
                    v.getContext().startActivity(intent);
                }
            }
        };
    }


//    private void showPOI(double latitude, double longitude){
//        //Toast.makeText(activity,String.valueOf(latitude)+" "+String.valueOf(longitude),Toast.LENGTH_SHORT).show();
//    }
    private void deletePOI(final String oldTitle, final String oldDescription, final double old_Latitude, final double old_Longitude){
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Are you sure?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String sql = "DELETE FROM pois WHERE title='"+oldTitle+"' and description = '"+oldDescription+"' and latitude = '"+old_Latitude+"' and longitude = '"+old_Longitude+"';";
                mDatabase.execSQL(sql);
                adapter = new PointOfInterestAdapter(activity,points,mDatabase,pois_layout,recyclerView,0,0);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                reloadPOIsFromDatabase();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void updatePOI(String oldTitle, String oldDescription, double old_Latitude, double old_Longitude){
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.edit_poi, null);
        builder.setView(view);
        EditText title = view.findViewById(R.id.edit_title);
        EditText description = view.findViewById(R.id.edit_description);
        EditText latitude = (EditText) view.findViewById(R.id.edit_latitude);
        EditText longitude = (EditText) view.findViewById(R.id.edit_longitude);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        View btn_update = view.findViewById(R.id.buttonUpdatePoi);


        title.setText(oldTitle);
        latitude.setText(String.valueOf(old_Latitude));
        longitude.setText(String.valueOf(old_Longitude));
        //set spinner adapter
        ArrayList<String> categoryList = new ArrayList<>();
        categoryList.add("Home");
        categoryList.add("Work");
        categoryList.add("Other");
        ArrayAdapter<String> spnAdapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_dropdown_item_1line, categoryList);
        spinnerCategory.setAdapter(spnAdapter);

        spinnerCategory.setOnItemSelectedListener(onItemSelectedListener());
        final AlertDialog dialog = builder.create();
        dialog.show();
        btn_update.setOnClickListener(onUpdateListener(title,description,spinnerCategory,latitude,longitude,oldTitle,oldDescription,old_Latitude,old_Longitude,dialog));

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


    private View.OnClickListener onClickListener(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(activity);
                dialog.setContentView(R.layout.pois_layout);
                dialog.setTitle("Point " + (position+1));
                dialog.setCancelable(true); // dismiss when touching outside Dialog

                // set the custom dialog components - texts and image
                TextView title = (TextView) dialog.findViewById(R.id.title);
                TextView description = (TextView) dialog.findViewById(R.id.description);
                TextView latitude = (TextView) dialog.findViewById(R.id.latitude);
                TextView longitude = (TextView) dialog.findViewById(R.id.longitude);
                ImageView icon = (ImageView) dialog.findViewById(R.id.image);
                Button edit_btn = (Button) dialog.findViewById(R.id.edit_btn);
                Button delete_btn = (Button) dialog.findViewById(R.id.delete_btn);
                Button show_btn = (Button) dialog.findViewById(R.id.show_btn);
                setDataToView(title, description, latitude, longitude, icon,edit_btn,delete_btn, position);
                show_btn.setVisibility(View.INVISIBLE);
                dialog.show();
            }
        };
    }



    private View.OnClickListener onUpdateListener(final EditText title, final EditText description, final Spinner spinnerCategory,
                                                  final EditText latitude, final EditText longitude, final String old_title,
                                                  final String old_description, final double old_latitude, final double old_longitude,final AlertDialog dialog){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //EditText ed1 = (EditText)view.findViewById(R.id.edit_title);


                String new_title,new_description,new_category;
                double new_latitude,new_longitude;

                //new values from update form
                new_title = title.getText().toString();
                new_description = description.getText().toString();
                new_category = spinnerCategory.getSelectedItem().toString();
                try{
                    new_latitude = Double.parseDouble(latitude.getText().toString());

                }
                catch (Exception e){
                    latitude.setError("Wrong input");
                    latitude.requestFocus();
                    return;
                }

                try{
                    new_longitude = Double.parseDouble(longitude.getText().toString());
                }
                catch (Exception e){
                    longitude.setError("Wrong input");
                    longitude.requestFocus();
                    return;
                }

                if(new_title.isEmpty()){
                    new_title = old_title;
                }
                if(new_description.isEmpty()){
                    new_description = old_description;
                }

                String sql = "UPDATE pois SET title='"+new_title+"',description = '"+new_description+"',category = '"+new_category+"',latitude ='"+new_latitude+"',longitude = '"+new_longitude+"' WHERE title = '"+old_title+"';";
                mDatabase.execSQL(sql);

                adapter = new PointOfInterestAdapter(activity,points,mDatabase,pois_layout,recyclerView,0,0);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                reloadPOIsFromDatabase();
                dialog.dismiss();


                //Toast.makeText(activity,description.getText().toString(),Toast.LENGTH_SHORT).show();

            }
        };
    }

    private void reloadPOIsFromDatabase(){
        boolean home,work,other;
        Cursor cursorPOIs = mDatabase.rawQuery("SELECT * FROM pois", null);
        if (cursorPOIs.moveToFirst()) {
            points.clear();
            do {
                String category = cursorPOIs.getString(2);
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
                points.add(new PointOfInterest(
                        cursorPOIs.getString(0),
                        cursorPOIs.getString(1),
                        home,work,other,
                        cursorPOIs.getDouble(3),
                        cursorPOIs.getDouble(4)
                ));
            } while (cursorPOIs.moveToNext());
        }
        cursorPOIs.close();
        notifyDataSetChanged();
    }


    /**
     * View holder to display each RecylerView item
     * initializes views
     */
    protected class ViewHolder extends RecyclerView.ViewHolder {
        //THA BOUN TA PEDIA TOU LAYOUT POU THA FTIAKSW

        private ImageView imageView;
        private TextView title;
        private TextView description;
        private TextView latitude;
        private TextView longitude;
        private TextView distance;
        private Button edit_btn,delete_btn,show_btn;
        private View container;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.image);
            title = (TextView) view.findViewById(R.id.title);
            description = (TextView) view.findViewById(R.id.description);
            latitude = (TextView) view.findViewById(R.id.latitude);
            longitude = (TextView) view.findViewById(R.id.longitude);
            distance = (TextView) view.findViewById(R.id.distance);
            edit_btn = (Button)view.findViewById(R.id.edit_btn);
            delete_btn = (Button)view.findViewById(R.id.delete_btn);
            container = view.findViewById(R.id.card_view);
            show_btn = (Button)view.findViewById(R.id.show_btn);
        }
    }



}