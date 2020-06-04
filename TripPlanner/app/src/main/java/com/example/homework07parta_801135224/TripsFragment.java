package com.example.homework07parta_801135224;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class TripsFragment extends Fragment {
    User currentUser;

    Button addTrip;

    ListView lv_your_trips;
    ListView lv_friend_trips;

    TripAdapter your_adapter;
    TripAdapter friend_adapter;

    private TripListner mListener;

    ArrayList<TripInfo> your_trips;
    ArrayList<TripInfo> friends_trips;
    ArrayList<TripInfo> listOfTrips;

    ArrayList<String> friendsUids;
    ArrayList<String> tripUids;



    DatabaseReference tripsDatabaseReference = FirebaseDatabase.getInstance().getReference();;
    String uid = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trips, container, false);
        uid = (String) getArguments().get("currentUser");

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            your_trips = new ArrayList<>();
            friends_trips = new ArrayList<>();
            listOfTrips = new ArrayList<>();

            friendsUids = new ArrayList<>();
            tripUids = new ArrayList<>();

            addTrip = getView().findViewById(R.id.fab);
            lv_your_trips = (ListView) getView().findViewById(R.id.your_trips);
            lv_friend_trips = (ListView) getView().findViewById(R.id.friends_trips);


            your_adapter = new TripAdapter(getContext(), R.layout.adapter_trip, your_trips, false);
            lv_your_trips.setAdapter(your_adapter);
            your_adapter.setNotifyOnChange(true);

            friend_adapter = new TripAdapter(getContext(), R.layout.adapter_trip, friends_trips, true);
            lv_friend_trips.setAdapter(friend_adapter);
            friend_adapter.setNotifyOnChange(true);

            addTrip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.addTrip();
                }
            });

        }catch (Exception e){
            Toast.makeText(getContext(), "Error occured.", Toast.LENGTH_SHORT).show();
        }

        tripsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    if (dataSnapshot.child("users").child(uid).exists()) {

                        friendsUids.clear();
                        tripUids.clear();
                        friends_trips.clear();
                        your_trips.clear();

                        String friendUid;
                        for (DataSnapshot data : dataSnapshot.child("users").child(uid).child("friendsUids").getChildren()) {
                            friendUid = (String) data.getValue();
                            friendsUids.add(friendUid);
                        }
                    }

                    if (dataSnapshot.child("users").child(uid).child("tripUids").exists()) {

                        TripInfo trip;
                        String tripId;
                        for (DataSnapshot data : dataSnapshot.child("users").child(uid).child("tripUids").getChildren()) {
                            tripId = (String) data.getValue();
                            if (!tripUids.contains(tripId)){
                                tripUids.add(tripId);
                                trip = dataSnapshot.child("trips").child(tripId).getValue(TripInfo.class);
                                if (trip != null) {
                                 your_trips.add(trip);
                                }
                            }
                        }
                    }

                    for (String friendUid : friendsUids) {
                        if (dataSnapshot.child("users").child(friendUid).child("tripUids").exists()) {
                            String tripUid;
                            TripInfo trip;

                            for (DataSnapshot data : dataSnapshot.child("users").child(friendUid).child("tripUids").getChildren()) {
                                tripUid = (String) data.getValue();

                                if (!tripUids.contains(tripUid)) {
                                    trip = dataSnapshot.child("trips").child(tripUid).getValue(TripInfo.class);
                                    if (trip != null) {
                                        friends_trips.add(trip);
                                    }
                                }
                            }
                        }
                    }

                    your_adapter.notifyDataSetChanged();
                    friend_adapter.notifyDataSetChanged();

                }catch (Exception e){
                    Toast.makeText(getContext(), "Error occured.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EditProfileFragment.handleSaveChanges) {
            mListener = (TripListner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    interface TripListner{
        void addTrip();
    }
}
