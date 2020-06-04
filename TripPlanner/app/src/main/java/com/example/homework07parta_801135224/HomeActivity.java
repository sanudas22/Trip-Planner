package com.example.homework07parta_801135224;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements EditProfileFragment.handleSaveChanges,
        TripsFragment.TripListner,
        FriendsAdapter.IHandleConnect,
        NotificationAdapter.IShareData, GoogleApiClient.OnConnectionFailedListener {


    final int ACTIVITY_SELECT_IMAGE = 1234;
    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference databaseReference;
    DatabaseReference postsDatabaseReference;
    StorageReference storageReference;
    FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    ValueEventListener databaseChangeListener;

    User user;
    FirebaseUser currentUser;
    TabLayout tabLayout;
    Uri imageUri;
    String fName,lName;
    FriendsFragment tabFriends;
    EditProfileFragment tabSettings;
    TripsFragment tabTrips;
    NotificationFragment tabNotifications;
    ArrayList<TripInfo> trips;
    ArrayList<User> friends;
    String currentUserUid;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

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
        setContentView(R.layout.activity_home);

        setTitle("Trip Planner");

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        try{
            GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, options)
                    .build();


            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);

            tabLayout.addTab(tabLayout.newTab().setText("Friends"));
            tabLayout.getTabAt(0).setIcon(R.drawable.person);
            tabLayout.addTab(tabLayout.newTab().setText("Trips"));
            tabLayout.getTabAt(1).setIcon(R.drawable.trip);

            tabLayout.addTab(tabLayout.newTab().setText("Notifs"));
            tabLayout.getTabAt(2).setIcon(R.drawable.ic_notifications);
            tabLayout.addTab(tabLayout.newTab().setText("My Profile"));
            tabLayout.getTabAt(3).setIcon(R.drawable.profile);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


            final ViewPager viewPager = (ViewPager) findViewById(R.id.container);
            viewPager.setAdapter(mSectionsPagerAdapter);
            /*final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
            viewPager.setAdapter(adapter);*/
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });


            trips = new ArrayList<>();

            currentUser = mFirebaseAuth.getCurrentUser();

            if(!isConnectedOnline()){
                Toast.makeText(this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                finish();
            }

            if(currentUser != null){
                currentUserUid = currentUser.getUid();
            }
        }catch (Exception e){
            Toast.makeText(this, "Error occured.H", Toast.LENGTH_SHORT).show();
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                try{
                    currentUser = firebaseAuth.getCurrentUser();

                    if(currentUser != null){
                        currentUserUid = currentUser.getUid();
                    }
                }catch (Exception e){
                    Toast.makeText(HomeActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                }


            }
        };

        databaseChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try{
                    if(currentUserUid != null){
                        if(dataSnapshot.child("users").child(currentUserUid).exists()){
                            user = dataSnapshot.child("users").child(currentUserUid).getValue(User.class);
                            Log.d("user",user.toString());
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(HomeActivity.this, "Error occured..", Toast.LENGTH_SHORT).show();
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        try{
            getMenuInflater().inflate(R.menu.menu_items, menu);
            /*
            getMenuInflater().inflate(R.menu.menu_tabbed, menu);

            SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView =
                    (SearchView) menu.findItem(R.id.menu_search).getActionView();
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));*/


            return true;
        }catch (Exception e){
            Toast.makeText(this, "Error occured...", Toast.LENGTH_SHORT).show();
        }
        return false;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        try{
            if(item.getItemId() == R.id.logout) {
                signOut();
                mFirebaseAuth.signOut();
                startActivity(new Intent(HomeActivity.this,LoginActivity.class));
                finishAffinity();
                return true;
            }
            //FirebaseAuth.getInstance().signOut();
            Intent i=new Intent(HomeActivity.this,LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            return true;
        }catch (Exception e){
            Toast.makeText(this, "Error occured....", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
        databaseReference.addValueEventListener(databaseChangeListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
        databaseReference.removeEventListener(databaseChangeListener);
    }

    @Override
    public void changeImage() {
        try{
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);//
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),ACTIVITY_SELECT_IMAGE);
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void saveChanges(final String fName, final String lName, final User.GENDER gender) {
        try{
            this.fName = fName;
            this.lName = lName;
            UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fName + ", " + lName)
                    .build();

            currentUser.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        user.setfName(fName);
                        user.setlName(lName);
                        user.setGender(gender);

                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/users/" + currentUserUid + "/fName" ,fName);
                        childUpdates.put("/users/" + currentUserUid + "/lName",lName);
                        childUpdates.put("/users/" + currentUserUid + "/gender", gender);

                        databaseReference.updateChildren(childUpdates);

                        Toast.makeText(HomeActivity.this, "User Details updated", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void changePassword(String oldPassword, final String newPassword) {
        try{
            if(!newPassword.equals(oldPassword)){


                AuthCredential credential = EmailAuthProvider
                        .getCredential(currentUser.getEmail(), oldPassword);

// Prompt the user to re-provide their sign-in credentials
                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    currentUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Toast.makeText(HomeActivity.this, "Password Updated", Toast.LENGTH_SHORT).show();
                                                FirebaseAuth.getInstance().signOut();

                                                Intent intent = new Intent(HomeActivity.this,LoginActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);

                                                Log.d("demo", "Password updated");
                                            } else {
                                                Toast.makeText(HomeActivity.this, "Error password not updated.", Toast.LENGTH_SHORT).show();
                                                Log.d("demo", "Error password not updated");
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(HomeActivity.this, "Error. auth failed", Toast.LENGTH_SHORT).show();
                                    Log.d("demo", "Error auth failed");
                                }
                            }
                        });
            }else{
                Toast.makeText(this, "New Password cannot be same as old password.", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }




    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try{
            if (requestCode == ACTIVITY_SELECT_IMAGE) {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                            changeProfileImage(bitmap);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }

    }

    void changeProfileImage(Bitmap bitmap){


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] dataArray = baos.toByteArray();

        final StorageReference reference = storageReference.child("profile_images/" + currentUserUid + ".png");

        UploadTask uploadTask = reference.putBytes(dataArray);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Error occured. Cover Photo not saved", Toast.LENGTH_SHORT).show();
                }

                return reference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    imageUri = task.getResult();
                    UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(imageUri)
                            .build();

                    currentUser.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                tabSettings.postCurrentUserImage(imageUri.toString());
                                user.setImageUrl(imageUri.toString());

                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("/users/" + currentUserUid + "imageUrl",imageUri);
                                databaseReference.updateChildren(childUpdates);

                                Toast.makeText(HomeActivity.this, "Profile Image updated", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void addTrip() {
        try{
            Intent intent = new Intent(HomeActivity.this,TripActivity.class);
            startActivity(intent);
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void addFriend(final User friendUser, final View v) {

        try{
            new AlertDialog.Builder(this)
                    .setTitle("Add Friend")
                    .setMessage("Do you really want to add this friend")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            user.addToSentFriendRequestUid(friendUser.getUid());
                            friendUser.addToReceivedFriendRequestUid(user.getUid());

                            Map<String, Object> postCurrentUser = user.toMap();
                            Map<String, Object> postUser = friendUser.toMap();



                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/users/" + user.getUid()  ,postCurrentUser);
                            childUpdates.put("/users/" + friendUser.getUid(),postUser);

                            databaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    //((ImageButton)v).setImageResource(R.drawable.sent);
                                    friendUser.setStatus(User.FRIEND_STATUS.SENT);

                                }
                            });

                            Toast.makeText(HomeActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {


                        }
                    })
                    .show();
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }






    }

    @Override
    public void displayReceivedMessage(User friend) {

        try{
            user.addToSentFriendRequestUid(friend.getUid());
            friend.addToReceivedFriendRequestUid(user.getUid());

            Map<String, Object> postCurrentUser = user.toMap();
            Map<String, Object> postUser = friend.toMap();



            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/users/" + user.getUid()  ,postCurrentUser);
            childUpdates.put("/users/" + friend.getUid(),postUser);

            databaseReference.updateChildren(childUpdates);

            Toast.makeText(this, "Friend request sent", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void displaySentMessage() {
        Toast.makeText(this, "Friend Request sent already.", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void removeFriend(final User friendUser) {
        try{
            new AlertDialog.Builder(this)
                    .setTitle("Remove Friend")
                    .setMessage("Do you really want to remove this friend")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            user.removeFriendUid(friendUser.getUid());
                            friendUser.removeFriendUid(user.getUid());

                            Map<String, Object> postCurrentUser = user.toMap();
                            Map<String, Object> postUser = friendUser.toMap();



                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/users/" + user.getUid()  ,postCurrentUser);
                            childUpdates.put("/users/" + friendUser.getUid(),postUser);

                            databaseReference.updateChildren(childUpdates);

                            //Toast.makeText(this, "Friend request sent", Toast.LENGTH_SHORT).show();
                            Toast.makeText(HomeActivity.this, "Removed from friend list.", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {


                        }
                    })
                    .show();

        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }




    }

    @Override
    public void selectFriend(int position, View v) {

    }

    @Override
    public void removeFriendFromTrip(int position) {

    }

    @Override
    public void handleFriendRequest(User friend, boolean accept) {

        try{
            if(accept){
                user.removeFromReceivedRequestUid(friend.getUid());
                user.addFriendUid(friend.getUid());

                friend.addFriendUid(currentUserUid);
                friend.removeFromSentFriendRequestUid(currentUserUid);

                Map<String, Object> postCurrentUserValues = user.toMap();
                Map<String, Object> postFriendValues = friend.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("users/" + currentUserUid ,postCurrentUserValues);
                childUpdates.put("users/" + friend.getUid(),postFriendValues);
                databaseReference.updateChildren(childUpdates);
            }else{
                user.removeFromReceivedRequestUid(friend.getUid());

                friend.removeFromSentFriendRequestUid(currentUserUid);

                Map<String, Object> postCurrentUserValues = user.toMap();
                Map<String, Object> postFriendValues = friend.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("users/" + currentUserUid ,postCurrentUserValues);
                childUpdates.put("users/" + friend.getUid(),postFriendValues);
                databaseReference.updateChildren(childUpdates);
            }

        }catch (Exception e){
            Toast.makeText(this, "Error occured.H55", Toast.LENGTH_SHORT).show();
        }



    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            try{
                Bundle bundle = new Bundle();
                bundle.putSerializable("currentUser",currentUserUid);


                switch(position){
                    case 0: tabFriends = new FriendsFragment();
                        tabFriends.setArguments(bundle);
                        return tabFriends;

                    case 1: {
                        tabTrips = new TripsFragment();
                        tabTrips.setArguments(bundle);
                        return tabTrips;

                    }
                    case 2: tabNotifications = new NotificationFragment();
                        return tabNotifications;
                    case 3: {
                        tabSettings = new EditProfileFragment();
                        tabSettings.setArguments(bundle);
                        return tabSettings;
                    }

                    default: return null;
                }
            }catch (Exception e){
                Toast.makeText(HomeActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
            }
            return null;


        }

        @Override
        public int getCount() {
            return 4;
        }


    }
}
