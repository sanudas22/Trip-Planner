package com.example.homework07parta_801135224;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddToTripActivity extends AppCompatActivity implements FriendsAdapter.IHandleConnect {

    User currentUser;
    TripInfo currentTrip;

    String tripId;
    String currentUserId;
    DatabaseReference databaseReference;
    ValueEventListener databaseChangeListener;

    ArrayList<String> friendUids;
    ArrayList<User> friends;
    ArrayList<User> members;
    ArrayList<String> friendsInCurrentTrips;
    ArrayList<Integer> selectedPositions;

    FriendsAdapter adapter;
    FriendsAdapter adapter_members;

    ListView lt_friends;
    ListView lt_members;
    TextView tv_status;



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
        setContentView(R.layout.activity_add_to_trip);

        try{
            if (getIntent().getExtras().containsKey("trip_id")) {
                tripId = getIntent().getExtras().getString("trip_id");
            }

            if(!isConnectedOnline()){
                Toast.makeText(this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                finish();
            }

            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            friends = new ArrayList<>();
            members = new ArrayList<>();
            friendUids = new ArrayList<>();
            friendsInCurrentTrips = new ArrayList<>();
            selectedPositions = new ArrayList<>();

            lt_friends = (ListView) findViewById(R.id.lt_friends);
            lt_members = (ListView) findViewById(R.id.lt_members);

            adapter = new FriendsAdapter(this, R.layout.adapter_friends, friends,true,false,false);
            lt_friends.setAdapter(adapter);
            adapter.setNotifyOnChange(true);

            adapter_members = new FriendsAdapter(this,R.layout.adapter_friends,members,false,true,false);
            lt_members.setAdapter(adapter_members);
            adapter_members.setNotifyOnChange(true);

            tv_status = (TextView) findViewById(R.id.tv_status);




            databaseReference = FirebaseDatabase.getInstance().getReference();
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }



        databaseChangeListener = new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try{

                    friendUids.clear();
                    friendsInCurrentTrips.clear();
                    friends.clear();
                    members.clear();
                    selectedPositions.clear();

                    currentUser = dataSnapshot.child("users").child(currentUserId).getValue(User.class);
                    currentTrip = dataSnapshot.child("trips").child(tripId).getValue(TripInfo.class);

                    if (currentTrip == null) {
                        currentTrip = new TripInfo();
                    }
                    friendsInCurrentTrips = currentTrip.getFriendsUids();
                    friendUids = currentUser.getFriendsUids();

                    User friend;
                    if (friendUids != null) {
                        for (String friendUid : friendUids) {
                            if (friendsInCurrentTrips != null) {
                                if (!friendsInCurrentTrips.contains(friendUid)) {
                                    friend = dataSnapshot.child("users").child(friendUid).getValue(User.class);
                                    if (friend != null) {
                                        friend.setStatus(User.FRIEND_STATUS.FRIEND);
                                        friends.add(friend);
                                    }
                                }
                            } else {
                                friend = dataSnapshot.child("users").child(friendUid).getValue(User.class);
                                if (friend != null) {
                                    friend.setStatus(User.FRIEND_STATUS.FRIEND);
                                    friends.add(friend);
                                }
                            }

                        }
                    }

                    if(friendsInCurrentTrips != null){
                        for(String uid : friendsInCurrentTrips){
                            friend = dataSnapshot.child("users").child(uid).getValue(User.class);
                            if(friend != null){
                                if(friendUids.contains(friend.getUid())){
                                    friend.setStatus(User.FRIEND_STATUS.FRIEND);
                                }else if(currentUser.getSentFriendRequestUids().contains(friend.getUid())){
                                    friend.setStatus(User.FRIEND_STATUS.SENT);
                                }else if(currentUser.getReceivedFriendRequestUids().contains(friend.getUid())){
                                    friend.setStatus(User.FRIEND_STATUS.RECEIVED);
                                }else {
                                    if(friend.getUid().equals(currentUserId)){
                                        friend.setlName(friend.getlName() + " (YOU)");
                                    }
                                    friend.setStatus(User.FRIEND_STATUS.UNCONNECTED);
                                }
                                if(!members.contains(friend)){
                                    members.add(friend);
                                }
                            }
                        }
                    }


                    if(friends.size() == 0){
                        tv_status.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                    adapter_members.notifyDataSetChanged();

                }catch (Exception e){
                    Toast.makeText(AddToTripActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                    Log.d("demo",e.getLocalizedMessage());
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public void addFriend(User user,View v) {

    }

    @Override
    public void displayReceivedMessage(User friend) {

    }

    @Override
    public void displaySentMessage() {

    }

    @Override
    public void removeFriend(User user) {

    }

    @Override
    public void selectFriend(final int position, View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(AddToTripActivity.this)
                .setTitle("Do you want to add this member to trip?")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{

                            currentTrip.addFriendUid(friends.get(position).getUid());

                            Map<String, Object> postTripValues = currentTrip.toMap();

                            Map<String, Object> childUpdates = new HashMap<>();

                            childUpdates.put("/trips/" + currentTrip.getTrip_id(), postTripValues);

                            databaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        Toast.makeText(AddToTripActivity.this, "Friend added successfully.", Toast.LENGTH_SHORT).show();
                                        //finish();
                                    }

                                }
                            });

                        }catch (Exception e){
                            Toast.makeText(AddToTripActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    @Override
    public void removeFriendFromTrip(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(AddToTripActivity.this)
                .setTitle("Do you want to remove this member from trip?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            if(members.get(position).getUid().equals(currentUserId)){
                                Toast.makeText(AddToTripActivity.this, "If you want to exit group. Try from trip home page.", Toast.LENGTH_SHORT).show();
                            }else {


                                if (friendUids.contains(members.get(position).getUid())) {

                                    currentTrip.removeMemberFromTrip(members.get(position).getUid());

                                    Map<String, Object> postTrip = currentTrip.toMap();

                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put("/trips/" + tripId, postTrip);

                                    databaseReference.updateChildren(childUpdates);

                                    // databaseReference.child("trips/" + tripId + "/friendsUids" + ).removeValue();
                                    Toast.makeText(AddToTripActivity.this, "Successfully removed member from trip.", Toast.LENGTH_SHORT).show();
                                    //finish();
                                } else {
                                    Toast.makeText(AddToTripActivity.this, "Not your friend to remove from trip.", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }catch (Exception e){
                            Toast.makeText(AddToTripActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.addValueEventListener( databaseChangeListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.removeEventListener(databaseChangeListener);
    }
}
