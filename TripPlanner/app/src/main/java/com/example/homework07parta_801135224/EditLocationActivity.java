package com.example.homework07parta_801135224;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditLocationActivity extends AppCompatActivity implements LocationAdapter.IShareData {

    ListView lv_places;

    ArrayList<LocationDetails> places;
    String trip_id;
    TripInfo currentTrip;

    LocationAdapter adapter;

    HashMap<Integer,LocationDetails> placesMap;

    DatabaseReference mDatabaseReference;
    ValueEventListener databaseChangeListener;

    public boolean isConnectedOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (null != ni){
            if(ni.isConnected()){
                return true;
            }else{
                return false;
            }

        } else {
            return false;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        try{
            places = new ArrayList<>();
            placesMap = new HashMap<>();

            lv_places = (ListView) findViewById(R.id.lv_places);

            if(getIntent().getExtras().containsKey("trip_id")){
                trip_id = getIntent().getExtras().getString("trip_id");
            }

            if(!isConnectedOnline()){
                Toast.makeText(this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                finish();
            }

            adapter = new LocationAdapter(this,R.layout.adapter_location,places,true);
            lv_places.setAdapter(adapter);
            adapter.setNotifyOnChange(true);

            mDatabaseReference = FirebaseDatabase.getInstance().getReference("trips");


        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }


        databaseChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    currentTrip = dataSnapshot.child(trip_id).getValue(TripInfo.class);
                    Log.d("demo",currentTrip.toString());

                    List<LocationDetails> data = currentTrip.getPlaces();
                    if(data != null){
                        places.clear();
                        places.addAll(data);
                    }else{
                        places.clear();
                    }

                    for(int i = 0;i<places.size();i++){
                        placesMap.put(i, places.get(i));
                    }
                    Log.d("demo",places.toString());
                    adapter.notifyDataSetChanged();
                }catch (Exception e){
                    Toast.makeText(EditLocationActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabaseReference.addValueEventListener(databaseChangeListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabaseReference.removeEventListener(databaseChangeListener);
    }

    @Override
    public void editPlace(final int position) {

        final EditText et_number = new EditText(this);
        et_number.setHint("Swap this with position");

        new AlertDialog.Builder(this)
                .setTitle("Re-order this place in trip")
                .setMessage("Do you really want to edit this place's location?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        int i = Integer.valueOf(et_number.getText().toString());

                        Log.d("position",position + "");
                        Log.d("i",i + "");
                        Log.d("places",places.toString());

                        currentTrip.swapLocations(position,i-1);

                        Log.d("demo",currentTrip.toString());

                        //currentTrip.getPlaces().remove(position);


                        Map<String, Object> postValues = currentTrip.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(trip_id, postValues);
                        mDatabaseReference.updateChildren(childUpdates);

                        Toast.makeText(EditLocationActivity.this, "Places swapped successfully", Toast.LENGTH_SHORT).show();

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                    }
                })
                .setView(et_number)
                .show();
    }

    @Override
    public void deletePlace(final int position) {
        try{
            new AlertDialog.Builder(this)
                    .setTitle("Delete this place from trip")
                    .setMessage("Do you really want to delete this place")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            currentTrip.getPlaces().remove(position);


                            Map<String, Object> postValues = currentTrip.toMap();
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put(trip_id, postValues);
                            mDatabaseReference.updateChildren(childUpdates);

                            Toast.makeText(EditLocationActivity.this, "Place Deleted", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {


                        }
                    })
                    .show();
        }catch (Exception e){
            Toast.makeText(this, "Error Occured.", Toast.LENGTH_SHORT).show();
        }

    }
}
