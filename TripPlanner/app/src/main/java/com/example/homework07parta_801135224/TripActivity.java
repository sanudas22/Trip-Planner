package com.example.homework07parta_801135224;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TripActivity extends AppCompatActivity implements ISharePlaces, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        FriendsAdapter.IHandleConnect,
        LocationAdapter.IShareData {

    final int ACTIVITY_SELECT_IMAGE = 1234;
    final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    final int ACTIVITY_ADD_FRIENDS = 2;

    ImageButton imCoverPhoto;
    Button btnCreate;
    Button btnCancel;
    EditText etName;
    EditText etDescription;
    AutoCompleteTextView at_destination;

    ListView ltPlaces;
    ListView ltFriends;
    ArrayList<User> members;



    TripInfo trip;
    private GoogleMap mMap;

    StorageReference storageReference;
    DatabaseReference databaseReference;
    ValueEventListener databaseChangeEventListener;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser currentUser;
    int AUTOCOMPLETE_REQUEST_CODE = 1;

    User user;
    String uid;
    String imageUri;
    ArrayList<String> trips;

    ArrayList<LocationDetails> placesToAdd;
    ArrayList<String> friendsToAdd;

    GoogleApiClient mGoogleApiClient;

    FetchLocation placesTask;

    ArrayList<LocationDetails> places;
    HashMap<Integer,LocationDetails> placesMap;


    FriendsAdapter adapter;
    LocationAdapter adapterPlaces;



    View.OnClickListener coverPhotoChangeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);//
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),ACTIVITY_SELECT_IMAGE);
        }
    };
    View.OnClickListener createTripListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String name = etName.getText().toString();
            String description = etDescription.getText().toString();
            //String location  = at_destination.getText().toString();

            trip = new TripInfo(name,"",imageUri,uid,description,currentUser.getUid());

            trip.addFriends(friendsToAdd);
            trip.addLocations(placesToAdd);

            trip.addFriendUid(currentUser.getUid());

            Log.d("demo",trip.toString());
            user.addTripUid(uid);

            Map<String, Object> postTripValues = trip.toMap();
            Map<String, Object> postUserValues = user.toMap();

            Map<String, Object> childUpdates = new HashMap<>();

            childUpdates.put("/trips/" + uid ,postTripValues);
            childUpdates.put("/users/" + user.getUid(),postUserValues);

            databaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError == null){
                        Toast.makeText(TripActivity.this, "Trip created successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }
            });
        }
    };
    View.OnClickListener cancelTripListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

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
        setContentView(R.layout.activity_trip);

        setTitle("Add Trip");

        try{
            if(!isConnectedOnline()){
                Toast.makeText(this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                finish();
            }

            imCoverPhoto = (ImageButton)findViewById(R.id.im_coverphoto);
            btnCreate = (Button) findViewById(R.id.btn_create);
            btnCancel = (Button) findViewById(R.id.btn_cancel);
            etName = (EditText) findViewById(R.id.et_trip_name);
            etDescription = (EditText)findViewById(R.id.et_trip_details);

            ltFriends = (ListView) findViewById(R.id.lt_friends);
            ltPlaces = (ListView) findViewById(R.id.lt_places);

            places = new ArrayList<>();
            placesMap = new HashMap<>();
            members = new ArrayList<>();

            adapterPlaces = new LocationAdapter(this,R.layout.adapter_location,places,true);
            ltPlaces.setAdapter(adapterPlaces);
            adapterPlaces.setNotifyOnChange(true);

            adapter = new FriendsAdapter(this, R.layout.adapter_friends,members,false,true,false);
            adapter.setNotifyOnChange(true);
            ltFriends.setAdapter(adapter);


            imCoverPhoto.setOnClickListener(coverPhotoChangeListener);
            btnCreate.setOnClickListener(createTripListener);
            btnCancel.setOnClickListener(cancelTripListener);

            storageReference = FirebaseStorage.getInstance().getReference();
            databaseReference = FirebaseDatabase.getInstance().getReference();
            mFirebaseAuth = FirebaseAuth.getInstance();
            currentUser = mFirebaseAuth.getCurrentUser();

            uid = UUID.randomUUID().toString();
            String apiKey = "AIzaSyATjjPA3wkGBcI2iSG26mzvlA5wAIZNTJY";
            Places.initialize(getApplicationContext(), apiKey);
            PlacesClient placesClient = Places.createClient(this);

            /*RectangularBounds bounds = RectangularBounds.newInstance(
                    new LatLng(-33.880490, 151.184363), //dummy lat/lng
                    new LatLng(-33.858754, 151.229596));
            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                    getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    Intent intent = new Intent(TripActivity.this, LocationMapActivity.class);
                    intent.putExtra("places", place);
                    startActivity(intent);
                }

                @Override
                public void onError(Status status) {

                }
            });*/

            trips = new ArrayList<>();
            placesToAdd = new ArrayList<>();
            friendsToAdd = new ArrayList<>();

        }catch (Exception e){
            Toast.makeText(TripActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
            Log.d("demo",e.getLocalizedMessage());
        }


        databaseChangeEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("users").child(currentUser.getUid()).exists()){
                    try{
                        user = dataSnapshot.child("users").child(currentUser.getUid()).getValue(User.class);
                        Log.d("user in reading",user.toString());
                    }catch (Exception e){
                        Toast.makeText(TripActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                        Log.d("demo",e.getLocalizedMessage());

                    }
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
        databaseReference.addValueEventListener(databaseChangeEventListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.removeEventListener(databaseChangeEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_trip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        try{
            switch (item.getItemId()){
                case R.id.action_add_friends:{
                    Intent i = new Intent(TripActivity.this, AddToNewTripActivity.class);
                    i.putExtra("user_id", user.getUid());
                    i.putExtra("trip_id", uid);
                    startActivityForResult(i,ACTIVITY_ADD_FRIENDS);
                    break;
                }
                case R.id.  action_add_location:{
                    List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this);
                    startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                    break;
                }


            }

        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
            Log.d("demo",e.getLocalizedMessage());

        }



        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        try{
            switch (requestCode){
                case ACTIVITY_SELECT_IMAGE:{
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
                    break;
                }

                case PLACE_AUTOCOMPLETE_REQUEST_CODE:{
                    if (resultCode == AUTOCOMPLETE_REQUEST_CODE) {

                        new AlertDialog.Builder(this)
                                .setTitle("Add Place")
                                .setMessage("Do you really want to add this place to trip")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {

                                        Place place = Autocomplete.getPlaceFromIntent(data);

                                        LocationDetails placeToAdd = new LocationDetails();
                                        placeToAdd.setAddress(String.valueOf(place.getAddress()));
                                        placeToAdd.setName(String.valueOf(place.getName()));
                                        placeToAdd.setLat(place.getLatLng().latitude);
                                        placeToAdd.setLng(place.getLatLng().longitude);

                                        places.add(placeToAdd);
                                        adapterPlaces.notifyDataSetChanged();

                                        if(!placesToAdd.contains(placeToAdd)){
                                            placesToAdd.add(placeToAdd);

                                            Toast.makeText(TripActivity.this, "Successfully Added Place to trip", Toast.LENGTH_SHORT).show();

                                        }else{
                                            Toast.makeText(TripActivity.this, "Place already exists in trip.", Toast.LENGTH_SHORT).show();
                                        }


                                        Log.i("demo", "Place: " + place.getName());
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {


                                    }
                                })
                                .show();


                    } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {

                        Toast.makeText(this, "Error occured. Try after sometime.", Toast.LENGTH_SHORT).show();
                        Status status = Autocomplete.getStatusFromIntent(data);
                        // TODO: Handle the error.
                        Log.d("demo", status.getStatus().toString());
                        Log.i("demo", status.getStatusMessage());

                    } else if (resultCode == RESULT_CANCELED) {
                        // The user canceled the operation.
                    }
                    break;
                }
                case ACTIVITY_ADD_FRIENDS:{
                    if(resultCode == RESULT_OK){
                        ArrayList<String> friends = (ArrayList<String>) data.getSerializableExtra("friends_to_add");
                        ArrayList<User> mem = (ArrayList<User>) data.getSerializableExtra("members");
                        friendsToAdd.addAll(friends);
                        members.addAll(mem);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Successfully added friends to activity", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

            }

        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
            Log.d("demo",e.getLocalizedMessage());

        }


    }

    void changeProfileImage(final Bitmap bitmap){


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] dataArray = baos.toByteArray();

        final StorageReference reference = storageReference.child("profile_images/" + uid + ".png");

        UploadTask uploadTask = reference.putBytes(dataArray);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    Toast.makeText(TripActivity.this, "Error occured. Cover Photo not saved", Toast.LENGTH_SHORT).show();
                }

                return reference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    imageUri = task.getResult().toString();
                    imCoverPhoto.setImageBitmap(bitmap);
                    Toast.makeText(TripActivity.this, "Cover photo saved", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

        /*uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(TripActivity.this, "Error occured. Cover Photo not saved", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //TODO Change image in image button
                imageUri = taskSnapshot.get
                imCoverPhoto.setImageBitmap(bitmap);
                Toast.makeText(TripActivity.this, "Cover photo saved", Toast.LENGTH_SHORT).show();

            }
        });
    }*/

    @Override
    public void postPlacesResult(List<HashMap<String, String>> result) {
        try{
            String[] from = new String[] { "description"};
            int[] to = new int[] { android.R.id.text1 };

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

            // Setting the adapter
            at_destination.setAdapter(adapter);
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
            Log.d("demo",e.getLocalizedMessage());

        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

                        Collections.swap(places,position,i-1);
                        adapterPlaces.notifyDataSetChanged();

                        //currentTrip.getPlaces().remove(position);



                        Toast.makeText(TripActivity.this, "Places swapped successfully", Toast.LENGTH_SHORT).show();

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

                            places.remove(position);
                            adapterPlaces.notifyDataSetChanged();

                            Toast.makeText(TripActivity.this, "Place Deleted", Toast.LENGTH_SHORT).show();

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
    public void selectFriend(int position, View v) {

    }

    @Override
    public void removeFriendFromTrip(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(TripActivity.this)
                .setTitle("Do you want to remove this member from trip?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            if(members.get(position).getUid().equals(currentUser.getUid())){
                                Toast.makeText(TripActivity.this, "If you want to exit group. Try from trip home page.", Toast.LENGTH_SHORT).show();
                            }else {


                                if (friendsToAdd.contains(members.get(position).getUid())) {

                                    friendsToAdd.remove(position);
                                    members.remove(position);
                                    adapter.notifyDataSetChanged();

                                    // databaseReference.child("trips/" + tripId + "/friendsUids" + ).removeValue();
                                    Toast.makeText(TripActivity.this, "Successfully removed member from trip.", Toast.LENGTH_SHORT).show();
                                    //finish();
                                } else {
                                    Toast.makeText(TripActivity.this, "Not your friend to remove from trip.", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }catch (Exception e){
                            Toast.makeText(TripActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                            Log.d("demo",e.getLocalizedMessage());

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
}
