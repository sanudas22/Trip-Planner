package com.example.homework07parta_801135224;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TripAdapter extends ArrayAdapter<TripInfo> {

    ArrayList<TripInfo> trips;

    Context mContext;

    int mResource;

    boolean isNew;



    public TripAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<TripInfo> tripDetails, boolean isNew) {
        super(context, resource, tripDetails);
        this.trips = tripDetails;
        this.mContext = context;
        this.mResource = resource;
        this.isNew = isNew;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        TripViewHolder holder;
        final TripInfo trip = trips.get(position);

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //convertView = inflater.inflate(R.layout.list_row_item,parent,false);
            convertView = inflater.inflate(mResource,parent,false);

            holder = new TripViewHolder();
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title_trip);
            holder.tv_location = (TextView)convertView.findViewById(R.id.tv_location);
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.button = (Button) convertView.findViewById(R.id.btn_view);

            convertView.setTag(holder);
        }

        holder = (TripViewHolder) convertView.getTag();
        TextView title = holder.tv_title;
        TextView location = holder.tv_location;
        ImageView imageView = holder.imageView;
        Button btn = holder.button;

        title.setText(trip.getTitle());
        location.setText(trip.getDescription());

        if(trip.getImageUrl() != null) {
            if(trip.getImageUrl().equals("")) {
                Picasso.get().
                        load(trip.getImageUrl()).
                        placeholder(R.mipmap.ic_loading_placeholder).
                        into(imageView);
            }
        }


        if(isNew){
            btn.setText("JOIN");
        }else{
            btn.setText("VIEW");
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isNew){
                    Intent intent = new Intent(mContext,AddTripActivity.class);
                    intent.putExtra("trip_id",trip.getTrip_id());
                    mContext.startActivity(intent);

                }else{
                    Intent intent = new Intent(mContext,AddTripActivity.class);
                    intent.putExtra("trip_id",trip.getTrip_id());
                    mContext.startActivity(intent);
                }

            }
        });





        return convertView;
    }

    @Override
    public int getCount() {
        return trips.size();
    }
}
