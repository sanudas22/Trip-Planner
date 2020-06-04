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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ViewTripActivity extends AppCompatActivity implements MessengerAdapter.IShareData,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationAdapter.IShareData{

    ImageView imCoverPhoto;
    TextView tvTripDetails;
    ListView lvChats;
    EditText etMessage;
    ImageButton imImageSend;
    ImageButton imMessageSend;


    DatabaseReference databaseReference;
    ValueEventListener databaseChangeEventListener;
    int AUTOCOMPLETE_REQUEST_CODE = 1;
    StorageReference storageReference;

    final int ACTIVITY_SELECT_IMAGE = 1234;
    final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    ArrayList<Message> messages;
    MessengerAdapter adapterChat;

    TripInfo currentTrip;
    User currentUser;

    String userUid;
    String tripId;
    boolean isMemberOf;
    String userDisplayName;

    GoogleApiClient mGoogleApiClient;


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
        setContentView(R.layout.activity_view_trip);

        try {


            setTitle("Chatroom");

            if (!isConnectedOnline()) {
                Toast.makeText(this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                finish();
            }

            messages = new ArrayList<>();

            imCoverPhoto = (ImageView) findViewById(R.id.im_cover_photo);
            tvTripDetails = (TextView) findViewById(R.id.trip_details);
            lvChats = (ListView) findViewById(R.id.lv_chats);
            etMessage = (EditText) findViewById(R.id.et_message);
            imMessageSend = (ImageButton) findViewById(R.id.im_send);
            imImageSend = (ImageButton) findViewById(R.id.im_photo);

            imMessageSend.setOnClickListener(send_click_listener);
            imImageSend.setOnClickListener(gallery_click_listener);


            if (getIntent().getExtras().containsKey("trip_id")) {
                tripId = getIntent().getExtras().getString("trip_id");
            }

            if (getIntent().getExtras().containsKey("isMemberOf")) {
                isMemberOf = (boolean) getIntent().getExtras().get("isMemberOf");
            }

            Places.initialize(getApplicationContext(), String.valueOf(this));
            PlacesClient placesClient = Places.createClient(this);


            storageReference = FirebaseStorage.getInstance().getReference();

            databaseReference = FirebaseDatabase.getInstance().getReference();
            userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userDisplayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

            adapterChat = new MessengerAdapter(this, R.layout.adapter_messenger, messages);
            lvChats.setAdapter(adapterChat);
            adapterChat.setNotifyOnChange(true);

        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
            Log.d("demo",e.getLocalizedMessage());

        }
        databaseChangeEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {

                    currentUser = dataSnapshot.child("users").child(userUid).getValue(User.class);
                    messages.clear();
                    ArrayList<Message> data = new ArrayList<Message>();


                    Message message;
                    for (DataSnapshot snapshot : dataSnapshot.child("trips/" + tripId + "/messages").getChildren()) {
                        Log.d("data", snapshot.getValue(Message.class).toString());
                        message = snapshot.getValue(Message.class);
                        if (message.getUsersWhoDeletedThisMessage() != null) {
                            if (!message.getUsersWhoDeletedThisMessage().contains(userUid)) {
                                data.add(message);
                            }
                        } else {
                            data.add(message);
                        }
                    }

                    messages.addAll(data);
                    adapterChat.notifyDataSetChanged();


                    if (dataSnapshot.child("trips/" + tripId).exists()) {
                        currentTrip = dataSnapshot.child("trips/" + tripId).getValue(TripInfo.class);
                        Picasso.get().
                                load(currentTrip.getImageUrl()).
                                placeholder(R.mipmap.ic_loading_placeholder).
                                into(imCoverPhoto);
                        tvTripDetails.setText(currentTrip.getTitle() +  "\n" + currentTrip.getDescription());
                    }
                }catch (Exception e){
                    Toast.makeText(ViewTripActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                    Log.d("demo",e.getLocalizedMessage());
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

        try{
            if(userUid.equals(currentTrip.getOrganizer_id())){
                getMenuInflater().inflate(R.menu.menu_trip_organizer, menu);
                return true;
            }else{
                getMenuInflater().inflate(R.menu.menu_trip_non_organizer, menu);
                return true;
            }
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
            Log.d("demo",e.getLocalizedMessage());

        }

        return false;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        try {

            if (userUid.equals(currentTrip.getOrganizer_id())) {

                switch (item.getItemId()) {
                    case R.id.action_remove: {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                .setTitle("Do you want to delete this trip?")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try{
                                            databaseReference.child("trips/" + tripId).removeValue();
                                            Toast.makeText(ViewTripActivity.this, "Successfully deleted trip.", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }catch (Exception e){
                                            Toast.makeText(ViewTripActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        builder.show();
                        break;
                    }
                    case R.id.action_add_friends: {

                        Intent i = new Intent(ViewTripActivity.this, AddToTripActivity.class);
                        i.putExtra("user_id", userUid);
                        i.putExtra("trip_id", tripId);
                        startActivity(i);
                        break;

                    }
                    case R.id.action_add_location: {
                        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
                        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                                .build(this);
                        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                        break;

                    }
                    case R.id.action_edit_location: {
                        Intent i = new Intent(ViewTripActivity.this, EditLocationActivity.class);
                        i.putExtra("trip_id", tripId);
                        startActivity(i);
                        break;
                    }
                    case R.id.action_view_map: {
                        Intent i = new Intent(ViewTripActivity.this, LocationMapActivity.class);
                        i.putExtra("places", currentTrip.getPlaces());
                        startActivity(i);
                        break;
                    }
                    case R.id.action_navigate:{

                        LocationDetails place = currentTrip.getPlaces().get(0);

                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + place.getLat() + ", " + place.getLng());

                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");

                        // Attempt to start an activity that can handle the Intent
                        startActivityForResult(mapIntent,100);
                        //}

                        break;
                    }

                }

            } else {
                switch (item.getItemId()) {
                    case R.id.action_add_friends: {

                        Intent i = new Intent(ViewTripActivity.this, AddToTripActivity.class);
                        i.putExtra("user_id", userUid);
                        i.putExtra("trip_id", tripId);
                        startActivity(i);
                        break;

                    }
                    case R.id.action_add_location: {
                        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
                        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                                .build(this);
                        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                        break;
                    }
                    case R.id.action_edit_location: {
                        Intent i = new Intent(ViewTripActivity.this, EditLocationActivity.class);
                        i.putExtra("trip_id", tripId);
                        startActivity(i);
                        break;
                    }
                    case R.id.action_view_map: {
                        Intent i = new Intent(ViewTripActivity.this, LocationMapActivity.class);
                        i.putExtra("places", currentTrip.getPlaces());
                        startActivity(i);
                        break;
                    }
                    case R.id.action_exit_group:{
                        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                .setTitle("Do you want to exit this trip?")
                                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try{

                                            currentTrip.removeMemberFromTrip(userUid);
                                            currentUser.removeTripId(tripId);

                                            Map<String, Object> postValues = currentTrip.toMap();
                                            Map<String, Object> postUser = currentUser.toMap();

                                            Map<String, Object> childUpdates = new HashMap<>();
                                            childUpdates.put("trips/" + tripId, postValues);
                                            childUpdates.put("users/" + userUid,postUser);

                                            databaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    Toast.makeText(ViewTripActivity.this, "Successfully exited trip.", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            });

                                        }catch (Exception e){
                                            Toast.makeText(ViewTripActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        builder.show();
                        break;
                    }

                    case R.id.action_navigate:{

                        LocationDetails place = currentTrip.getPlaces().get(0);

                        //for(PlaceDetails place : currentTrip.getPlaces()){
                        // Create a Uri from an intent string. Use the result to create an Intent.
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + place.getLat() + ", " + place.getLng() + "&mode=d");

                        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        // Make the Intent explicit by setting the Google Maps package
                        mapIntent.setPackage("com.google.android.apps.maps");

                        // Attempt to start an activity that can handle the Intent
                        startActivityForResult(mapIntent,100);
                        //}

                        break;
                    }
                }
            }
            return true;
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
            Log.d("demo",e.getLocalizedMessage());

        }
        return false;
    }



    View.OnClickListener send_click_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {


                String text = etMessage.getText().toString();
                Date date = Calendar.getInstance().getTime();
                String key = UUID.randomUUID().toString();

                Message messageDetails = new Message(text, userDisplayName, date, "", false, key);

                currentTrip.addMessage(messageDetails);

                Map<String, Object> postValues = currentTrip.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("trips/" + tripId, postValues);
                databaseReference.updateChildren(childUpdates);

                etMessage.setText("");
            }catch (Exception e){
                Toast.makeText(ViewTripActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                Log.d("demo",e.getLocalizedMessage());

            }

        }
    };

    View.OnClickListener gallery_click_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try{
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);//
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), ACTIVITY_SELECT_IMAGE);
            }catch (Exception e){
                Toast.makeText(ViewTripActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                Log.d("demo",e.getLocalizedMessage());

            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case ACTIVITY_SELECT_IMAGE:{

                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        try {

                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                            storeImage(bitmap);

                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("demo",e.getLocalizedMessage());

                        }

                    }
                }
                break;
            }
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:{

                try{
                    if (resultCode == RESULT_OK) {

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

                                        Log.d("demo",placeToAdd.toString());
                                        boolean isAdded = currentTrip.addPlaceToTrip(placeToAdd);

                                        if(isAdded){
                                            Map<String, Object> postValues = currentTrip.toMap();
                                            Log.d("demo",currentTrip.toString());
                                            Map<String, Object> childUpdates = new HashMap<>();
                                            childUpdates.put("trips/" + tripId, postValues);
                                            databaseReference.updateChildren(childUpdates);

                                            Toast.makeText(ViewTripActivity.this, "Successfully Added Place to trip.", Toast.LENGTH_SHORT).show();

                                        }else{
                                            Toast.makeText(ViewTripActivity.this, "Place already exists in trip.", Toast.LENGTH_SHORT).show();
                                        }


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
                    }}catch (Exception e){
                    Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
                    Log.d("demo",e.getLocalizedMessage());

                }
                break;
            }
            case 100:{

                break;
            }
        }


    }

    void storeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] dataArray = baos.toByteArray();

        final StorageReference reference = storageReference.child("messages_media/" + tripId + "/" + UUID.randomUUID().toString() + ".png");
        UploadTask uploadTask = reference.putBytes(dataArray);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    Toast.makeText(ViewTripActivity.this, "Error occured. Cover Photo not saved", Toast.LENGTH_SHORT).show();
                }

                return reference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Date date = Calendar.getInstance().getTime();
                    String key = UUID.randomUUID().toString();

                    Message messageDetails = new Message("", userDisplayName, date, task.getResult().toString(), true, key);

                    currentTrip.addMessage(messageDetails);

                    Map<String, Object> postValues = currentTrip.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put( "trips/" + tripId, postValues);
                    databaseReference.updateChildren(childUpdates);
                }
            }
        });
    }

    @Override
    public void postComment(final Message message) {

        try {


            final EditText et_comment = new EditText(this);
            et_comment.setHint("Enter Comment");

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Enter Comment")
                    .setPositiveButton("Post", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String commentKey = UUID.randomUUID().toString();
                            Post post = new Post(userDisplayName, et_comment.getText().toString(), Calendar.getInstance().getTime(), commentKey);

                            for (Message messageLoop : currentTrip.getMessages()) {
                                if (messageLoop.getId().equals(message.getId())) {
                                    messageLoop.addComment(post);
                                }
                            }

                            Map<String, Object> postValues = currentTrip.toMap();
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("trips/" + tripId, postValues);
                            databaseReference.updateChildren(childUpdates);


                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setView(et_comment);
            builder.show();
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
            Log.d("demo",e.getLocalizedMessage());

        }
    }

    @Override
    public void deleteMessage(final Message message) {

        try {


            new AlertDialog.Builder(this)
                    .setTitle("Delete this message")
                    .setMessage("Do you really want to delete this message")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            boolean index = currentTrip.getMessages().remove(message);
                            for (Message messageLoop : currentTrip.getMessages()) {
                                if (messageLoop.getId().equals(message.getId())) {
                                    messageLoop.addToUserWhoDeletedThisMessage(userUid);
                                }
                            }

                            Map<String, Object> postValues = currentTrip.toMap();
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("trips/" + tripId, postValues);
                            databaseReference.updateChildren(childUpdates);


                            Toast.makeText(ViewTripActivity.this, "Message Deleted", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {


                        }
                    })
                    .show();
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
            Log.d("demo",e.getLocalizedMessage());

        }

    }

    @Override
    public void onBackPressed() {
        try{
            Intent intent = new Intent(ViewTripActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Removes other Activities from stack
            startActivity(intent);
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
            Log.d("demo",e.getLocalizedMessage());

        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void editPlace(int position) {

    }

    @Override
    public void deletePlace(final int position) {


    }
}
