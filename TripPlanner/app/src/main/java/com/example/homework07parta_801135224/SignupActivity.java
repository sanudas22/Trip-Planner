package com.example.homework07parta_801135224;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignupActivity extends AppCompatActivity {

    final int ACTIVITY_SELECT_IMAGE = 1234;

    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    DatabaseReference mUserRef;

    EditText etFName;
    EditText etLName;
    EditText etEmail;
    EditText etChoosePassword;
    EditText etRepeatPassword;
    Button btnSignup;
    Button btnCancel;
    ImageButton btnChooseAvtar;
    RadioGroup genderRadioGroup;

    String fName,lName, imageUid;
    Uri imageUri;
    Bitmap bitmap;
    User.GENDER gender;

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


    View.OnClickListener signUp_click_listner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            try{
                final String  email,choose_password, repeat_password;
                fName = etFName.getText().toString();
                lName = etLName.getText().toString();
                email = etEmail.getText().toString();
                choose_password = etChoosePassword.getText().toString();
                repeat_password = etRepeatPassword.getText().toString();

                if(fName.equals("") || lName.equals("") || email.equals("") ||choose_password.equals("") || repeat_password.equals("")){
                    Toast.makeText(SignupActivity.this,"Enter all details",Toast.LENGTH_SHORT).show();
                }else{
                    if(!choose_password.equals(repeat_password)){
                        Toast.makeText(SignupActivity.this,"Passwords don't match",Toast.LENGTH_SHORT).show();
                    }else {

                        mFirebaseAuth.createUserWithEmailAndPassword(email,choose_password)
                                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(!task.isSuccessful()){
                                            Toast.makeText(SignupActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(SignupActivity.this,"Account successfully created",Toast.LENGTH_SHORT).show();
                                            FirebaseAuth.getInstance().signOut();

                                            Intent intent = new Intent(SignupActivity.this,LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);

                                            //TODO LOGIN ACTIVITY

                                        }
                                    }
                                });
                    }
                }
            }catch (Exception e){
                Toast.makeText(SignupActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
            }



        }
    };
    View.OnClickListener cancel_click_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
    View.OnClickListener choose_avtar_click_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try{
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);//
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),ACTIVITY_SELECT_IMAGE);
            }catch (Exception e){
                Toast.makeText(SignupActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        setTitle("Sign Up");

        try{
            if(!isConnectedOnline()){
                Toast.makeText(this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
                finish();
            }

            mFirebaseAuth = FirebaseAuth.getInstance();
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();


            etFName = (EditText) findViewById(R.id.et_firstName);
            etLName = (EditText) findViewById(R.id.et_lastName);
            etEmail = (EditText) findViewById(R.id.et_email);
            etChoosePassword = (EditText) findViewById(R.id.et_password);
            etRepeatPassword = (EditText) findViewById(R.id.et_confirm_password);
            btnSignup = (Button) findViewById(R.id.btn_signup);
            btnCancel = (Button) findViewById(R.id.btn_cancel);
            btnChooseAvtar = (ImageButton) findViewById(R.id.im_choose_avtar);
            genderRadioGroup = (RadioGroup) findViewById(R.id.genderRadioGroup);


            btnSignup.setOnClickListener(signUp_click_listner);
            btnCancel.setOnClickListener(cancel_click_listener);
            btnChooseAvtar.setOnClickListener(choose_avtar_click_listener);

            databaseReference = FirebaseDatabase.getInstance().getReference();
            mUserRef = databaseReference.child("users");

            imageUid = UUID.randomUUID().toString();
        }catch (Exception e){
            Toast.makeText(this, "Error occured.", Toast.LENGTH_SHORT).show();
        }



        genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch(checkedId){
                    case R.id.male: gender = User.GENDER.MALE;break;
                    case R.id.female: gender = User.GENDER.FEMALE;break;
                }
            }
        });


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                try{
                    final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    if (currentUser != null) {

                        UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                                .setDisplayName(fName + "," + lName)
                                .setPhotoUri(imageUri)
                                .build();


                        currentUser.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    if(currentUser != null){
                                            String imageUrl = "";
                                            if(currentUser.getPhotoUrl() != null) {
                                                imageUrl = currentUser.getPhotoUrl().toString();
                                            }
                                                String user_id =  currentUser.getUid();
                                                User user = new User(fName,lName,imageUrl,user_id);
                                                user.setImageUid(imageUid);
                                                user.setGender(gender);

                                                Map<String, Object> postValues = user.toMap();
                                                Map<String, Object> childUpdates = new HashMap<>();
                                                childUpdates.put("/users/" + user_id,postValues);
                                                databaseReference.updateChildren(childUpdates);
                                                //mUserRef.child(currentUser.getUid()).setValue(postValues);
                                                //mUserRef.child(currentUser.getUid()).setValue(user);

                                    }
                                }
                            }
                        });

                    }
                }catch (Exception e){
                    Toast.makeText(SignupActivity.this, "Error occured.", Toast.LENGTH_SHORT).show();
                }

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
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
                            storeImage(bitmap);

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
    void storeImage(Bitmap bitmap){


        //TODO Round button
        this.bitmap = bitmap;
        btnChooseAvtar.setImageBitmap(bitmap);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] dataArray = baos.toByteArray();

        final StorageReference reference = storageReference.child( "profile_images/" + imageUid + ".png");
        UploadTask uploadTask = reference.putBytes(dataArray);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "Error occured. Cover Photo not saved", Toast.LENGTH_SHORT).show();
                }

                return reference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    imageUri = task.getResult();
                }
            }
        });


    }

}
