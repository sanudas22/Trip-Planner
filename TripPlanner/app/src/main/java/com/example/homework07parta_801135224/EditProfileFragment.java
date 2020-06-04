package com.example.homework07parta_801135224;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class EditProfileFragment extends Fragment {
    EditText etFirstName;
    EditText etLastName;
    EditText etOldPassword;
    EditText etNewPassword;

    Button btnSave;
    Button btnChangePassword;

    ImageButton imChangeImage;
    User currentUser;
    String uid;
    RadioGroup radioGroup;
    RadioButton maleBtn;
    RadioButton femaleBtn;
    User.GENDER gender;

    private handleSaveChanges mListener;
    DatabaseReference databaseReference;
    ValueEventListener databaseChangeListener;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        uid = (String) getArguments().get("currentUser");
        databaseReference = FirebaseDatabase.getInstance().getReference();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {


            btnSave = (Button) getView().findViewById(R.id.btn_save);
            etFirstName = (EditText) getView().findViewById(R.id.et_first_name);
            etLastName = (EditText) getView().findViewById(R.id.et_last_name);
            imChangeImage = (ImageButton) getView().findViewById(R.id.im_change_image);
            etOldPassword = (EditText) getView().findViewById(R.id.et_password);
            etNewPassword = (EditText) getView().findViewById(R.id.et_newPassword);
            btnChangePassword = (Button) getView().findViewById(R.id.btn_change_password);

            radioGroup = (RadioGroup) getView().findViewById(R.id.radioGroup);
            maleBtn = (RadioButton) getView().findViewById(R.id.male);
            femaleBtn = (RadioButton) getView().findViewById(R.id.female);


        }catch (Exception e){
            Toast.makeText(getContext(), "Error occured.", Toast.LENGTH_SHORT).show();
        }
        databaseChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {


                    if (uid != null) {
                        if (dataSnapshot.child("users").child(uid).exists()) {
                            currentUser = dataSnapshot.child("users").child(uid).getValue(User.class);
                            Log.d("user", currentUser.toString());
                            etFirstName.setText(currentUser.getfName());
                            etLastName.setText(currentUser.getlName());
                            btnSave.setOnClickListener(save_click_listener);
                            btnChangePassword.setOnClickListener(change_password_listener);
                            imChangeImage.setOnClickListener(change_image_listener);
                            if(!currentUser.getImageUrl().equals("")){
                                Picasso.get().
                                        load(currentUser.getImageUrl()).
                                        placeholder(R.mipmap.ic_loading_placeholder).
                                        into(imChangeImage);
                            }


                            if (currentUser.getGender() == User.GENDER.FEMALE) {
                                femaleBtn.setChecked(true);
                            } else if(currentUser.getGender() == User.GENDER.MALE){
                                maleBtn.setChecked(true);
                            }
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(getContext(), "Error occured.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        databaseReference.addValueEventListener(databaseChangeListener);





        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId){
                    case R.id.male: gender = User.GENDER.MALE;break;
                    case R.id.female: gender = User.GENDER.FEMALE; break;
                }
            }
        });


    }

    View.OnClickListener save_click_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            try{


                String fName = etFirstName.getText().toString();
                String lName = etLastName.getText().toString();

                mListener.saveChanges(fName,lName,gender);
            }catch (Exception e){
                Toast.makeText(getContext(), "Error occured.", Toast.LENGTH_SHORT).show();
            }

        }
    };

    View.OnClickListener change_password_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            try{
                String oldPassword = etOldPassword.getText().toString();
                String newPassword = etNewPassword.getText().toString();
                if(oldPassword.equals("") || newPassword.equals("")){
                    Toast.makeText( getContext(), "Passwords cannot be empty.", Toast.LENGTH_SHORT).show();
                }
                mListener.changePassword (oldPassword,newPassword);
            }catch (Exception e){
                Toast.makeText(getContext(), "Error occured.", Toast.LENGTH_SHORT).show();
            }


        }
    };

    View.OnClickListener change_image_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.changeImage();
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof handleSaveChanges) {
            mListener = (handleSaveChanges) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void postCurrentUserImage(String url){
        Picasso.get().
                load(url).
                placeholder(R.mipmap.ic_loading_placeholder).
                into(imChangeImage);



    }

    interface handleSaveChanges{
        void changeImage();
        void saveChanges(String fName, String lName,User.GENDER gender );
        void changePassword(String oldPassword, String newPassword);
    }
}
