package com.example.homework07parta_801135224;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FriendsAdapter extends ArrayAdapter<User> {

    ArrayList<User> friends;

    Context mContext;

    int mResource;

    boolean isLongClickReq;

    boolean isOnClickReq;

    boolean showActionButton;


    IHandleConnect iHandleConnect;


    public FriendsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<User> objects, boolean isOnClickRequired, boolean isLongClickRequired, boolean showActionButton) {
        super(context, resource, objects);
        this.mContext = context;
        this.friends = objects;
        this.mResource = resource;
        this.iHandleConnect = (IHandleConnect) context;
        this.isLongClickReq = isLongClickRequired;
        this.isOnClickReq = isOnClickRequired;
        this.showActionButton = showActionButton;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final User friend = friends.get(position);
        FriendViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,parent,false);

            holder = new FriendViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.tv_name);
            holder.imageButton = (ImageButton) convertView.findViewById(R.id.im_connect);
            holder.imageView = (ImageView) convertView.findViewById(R.id.im_photo);

            convertView.setTag(holder);
        }

        holder = (FriendViewHolder) convertView.getTag();
        TextView title = holder.textView;
        ImageView imageView = holder.imageView;


        final ImageButton imageButton = holder.imageButton;

        if(showActionButton){
            imageButton.setVisibility(View.VISIBLE);
        }else{
            imageButton.setVisibility(View.INVISIBLE);
        }



        String details = friend.getfName() + "," + friend.getlName();/* + "\n" + ((friend.getGender() == null) ? "" : friend.getGender());*/

        title.setText(details);
        if(friend.getImageUrl().equals("")) {
            Picasso.get().
                    load("https://images.app.goo.gl/WpKWUfYVWb75X7jL9").
                    placeholder(R.mipmap.ic_loading_placeholder).
                    into(imageView);
           holder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_person));
        }else{
            Picasso.get().
                    load(friend.getImageUrl()).
                    placeholder(R.mipmap.ic_loading_placeholder).
                    into(imageView);
        }


        switch (friend.getStatus()){
            case FRIEND: imageButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_friend));
                break;
            case SENT: imageButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.sent));
                break;
            case RECEIVED:imageButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.received));
                imageButton.setClickable(false);
                imageButton.setEnabled(false);
                break;
            case UNCONNECTED: imageButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_person_add));
                break;

        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (friend.getStatus()){
                    case FRIEND: {
                        iHandleConnect.removeFriend(friend);
                        break;

                    }
                    case SENT:
                    {
                        iHandleConnect.displaySentMessage();
                        break;

                    }
                    case RECEIVED:{
                        iHandleConnect.displayReceivedMessage(friend);
                        imageButton.setImageResource(R.drawable.ic_friend);
                        break;
                    }

                    case UNCONNECTED:
                    {
                        iHandleConnect.addFriend(friend,imageButton);
                        //imageButton.setImageResource(R.mipmap.ic_sent);
                        break;


                    }


                }
                //change icon
            }
        });

        if(isOnClickReq){
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iHandleConnect.selectFriend(position,v);
                }
            });
        }

        if(isLongClickReq){
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    iHandleConnect.removeFriendFromTrip(position);
                    return false;
                }
            });
        }




        return convertView;
    }

    interface IHandleConnect{
        void addFriend(User user, View v);
        void displayReceivedMessage(User friend);
        void displaySentMessage();
        void removeFriend(User user);
        void selectFriend(int position, View v);
        void removeFriendFromTrip(int position);
    }
}
