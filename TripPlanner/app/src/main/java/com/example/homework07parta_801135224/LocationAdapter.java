package com.example.homework07parta_801135224;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class LocationAdapter extends ArrayAdapter<LocationDetails> {

    ArrayList<LocationDetails> mData;
    IShareData iShareData;
    Context mContext;
    int mResource;
    boolean showActionButtons;



    public LocationAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<LocationDetails> objects, boolean showActionButtons) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.mData = objects;
        this.showActionButtons = showActionButtons;

        this.iShareData = (LocationAdapter.IShareData) context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final LocationDetails place = mData.get(position);

        ViewHolder_Trip holder;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,parent,false);

            holder = new ViewHolder_Trip();
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_place);
            holder.im_edit = (ImageButton) convertView.findViewById(R.id.im_edit);
            holder.im_delete = (ImageButton) convertView.findViewById(R.id.im_delete);

            convertView.setTag(holder);
        }

        holder = (ViewHolder_Trip) convertView.getTag();
        TextView title = holder.tv_name;
        ImageButton im_edit = holder.im_edit;
        ImageButton im_delete = holder.im_delete;

        if(showActionButtons){
            im_delete.setVisibility(View.VISIBLE);
            im_edit.setVisibility(View.VISIBLE);
        }else{
            im_delete.setVisibility(View.INVISIBLE);
            im_edit.setVisibility(View.INVISIBLE);
        }

        title.setText(place.getAddress());

        im_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iShareData.editPlace(position);
            }
        });

        im_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iShareData.deletePlace(position);
            }
        });

        return convertView;

    }

    interface  IShareData{
        void editPlace(int position);

        void deletePlace(int position);

    }

    class ViewHolder_Trip{
        TextView tv_name;
        ImageButton im_edit;
        ImageButton im_delete;
    }
}
