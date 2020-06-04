package com.example.homework07parta_801135224;

import android.content.Context;
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

import java.util.List;

public class NotificationAdapter extends ArrayAdapter<User> {

    Context mContext;

    List<User> friends;

    int mResource;

    IShareData iShareData;

    public NotificationAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.friends = objects;
        this.mResource = resource;
        iShareData = (IShareData) context;
    }

    @Override
    public int getCount() {
        return friends.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;
        final User user = friends.get(position);

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //convertView = inflater.inflate(R.layout.list_row_item,parent,false);
            convertView = inflater.inflate(mResource,parent,false);

            holder = new ViewHolder();
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_name);
            holder.btn_accept = (ImageButton) convertView.findViewById(R.id.btn_add);
            holder.btn_reject = (ImageButton) convertView.findViewById(R.id.btn_reject);
            holder.imageView2 = convertView.findViewById(R.id.imageView2);



            convertView.setTag(holder);
        }

        holder = (ViewHolder) convertView.getTag();
        TextView title = holder.tv_title;
        ImageButton accept = holder.btn_accept;
        ImageButton reject = holder.btn_reject;

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iShareData.handleFriendRequest(user,true);
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iShareData.handleFriendRequest(user,false);
            }
        });

        title.setText(user.getfName() + " " + user.getlName());
        if(user.getImageUrl().equals("")) {
            Picasso.get().
                    load("https://images.app.goo.gl/WpKWUfYVWb75X7jL9").
                    placeholder(R.mipmap.ic_loading_placeholder).
                    into(holder.imageView2);
            holder.imageView2.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_person));
        }else{
            Picasso.get().
                    load(user.getImageUrl()).
                    placeholder(R.mipmap.ic_loading_placeholder).
                    into(holder.imageView2);
        }


        return convertView;
    }

    class ViewHolder{
        TextView tv_title;
        ImageButton btn_accept;
        ImageButton btn_reject;
        ImageView imageView2;

    }

    interface IShareData{
        void handleFriendRequest(User friend, boolean accept);
    }
}
