package com.example.homework07parta_801135224;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class NotificationFragment extends Fragment {

    User currentUser;

    ListView lt_notifications;

    ArrayAdapter<User> adapter;

    ArrayList<String> notifications;
    ArrayList<String> uids;
    ArrayList<User> users;

    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;

    String currentUserUid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        lt_notifications = (ListView)getView().findViewById(R.id.lt_notifications);
        notifications = new ArrayList<>();
        uids = new ArrayList<>();
        users = new ArrayList<>();

        adapter = new NotificationAdapter(getContext(),R.layout.adapter_notification,users);
        lt_notifications.setAdapter(adapter);
        adapter.setNotifyOnChange(true);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        currentUserUid = firebaseAuth.getCurrentUser().getUid();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {


                    uids.clear();
                    notifications.clear();
                    users.clear();

                    if (currentUserUid != null) {
                        if (dataSnapshot.child("users").child(currentUserUid).exists()) {
                            currentUser = dataSnapshot.child("users").child(currentUserUid).getValue(User.class);
                            Log.d("user", currentUser.toString());
                        }

                        if (dataSnapshot.child("users").child(currentUserUid).child("receivedFriendRequestUids").exists()) {
                            String uid;
                            for (DataSnapshot data : dataSnapshot.child("users").child(currentUserUid).child("receivedFriendRequestUids").getChildren()) {
                                uid = (String) data.getValue();
                                uids.add(uid);
                            }
                        }

                        User user;
                        for (String uid : uids) {

                            user = dataSnapshot.child("users").child(uid).getValue(User.class);
                            users.add(user);
                        }

                        adapter.notifyDataSetChanged();
                    }
                }catch (Exception e){
                    Toast.makeText(getContext(), "Error occured.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}
