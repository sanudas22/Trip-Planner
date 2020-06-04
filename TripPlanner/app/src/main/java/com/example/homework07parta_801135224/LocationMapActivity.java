package com.example.homework07parta_801135224;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class LocationMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        IShareDirections{


    private GoogleMap mMap;
    LocationManager locationManager;
    ArrayList<LocationDetails> places;
    ArrayList<LatLng> pointsOnMap;
    LatLngBounds.Builder bounds_builder;

    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_map);

        setTitle("Places to Visit");

        try{
            places = new ArrayList<>();
            pointsOnMap = new ArrayList<>();

            bounds_builder = new LatLngBounds.Builder();



            if(getIntent().getExtras().containsKey("places")){
                places = (ArrayList<LocationDetails>) getIntent().getExtras().getSerializable("places");
            }

            if(places != null){
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);


                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }else{
                Toast.makeText(this, "No places to mark on map.", Toast.LENGTH_SHORT).show();
                finish();
            }


        }catch (Exception e){
            Toast.makeText(this, "Error occurred.", Toast.LENGTH_SHORT).show();
            Log.d("demo", e.getLocalizedMessage());
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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

    public void onMapReady(GoogleMap googleMap) {

        final int zoomWidth = getResources().getDisplayMetrics().widthPixels;
        final int zoomHeight = getResources().getDisplayMetrics().heightPixels;
        final int zoomPadding = (int) (zoomWidth * 0.10);

        try{
            mMap = googleMap;
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    LatLngBounds bounds = bounds_builder.build();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,zoomWidth,zoomHeight,zoomPadding));

                }
            });
            markPlaces();

        }catch (Exception e){
        }

    }

    private void markPlaces(){

        for(LocationDetails place : places){
            LatLng latlng = new LatLng(place.getLat(),place.getLng());

            mMap.addMarker(new MarkerOptions().position(latlng).title(place.getName()));

            pointsOnMap.add(latlng);

            // Creating MarkerOptions
            MarkerOptions options = new MarkerOptions();

            // Setting the position of the marker
            options.position(latlng);

            if(pointsOnMap.size()==1){
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }else if(pointsOnMap.size()==2){
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }else{
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            }

            bounds_builder.include(latlng);
        }

        for(int i = 0;i< pointsOnMap.size() ;i++){

            LatLng origin = pointsOnMap.get(i);

            LatLng dest = pointsOnMap.get(i+1);

            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);

            DirectionTask task = new DirectionTask(this);

            // Start downloading json data from Google Directions API
            task.execute(url);
        }

        /*if(pointsOnMap.size() >= 2){
            LatLng origin = pointsOnMap.get(0);
            LatLng dest = pointsOnMap.get(pointsOnMap.size()-1);

            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);

            DownloadTask downloadTask = new DownloadTask(this);

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }*/

       /* PolylineOptions rectOptions = new PolylineOptions();
        rectOptions.color(Color.BLACK);
        rectOptions.isVisible();
        rectOptions.addAll(pointsOnMap);

        mMap.addPolyline(rectOptions);*/

    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Waypoints
        String waypoints = "";
        for(int i=2;i<pointsOnMap.size();i++){
            LatLng point  = (LatLng) pointsOnMap.get(i);
            if(i==2)
                waypoints = "waypoints=";
            waypoints += point.latitude + "," + point.longitude + "|";
        }

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+waypoints;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }


    @Override
    public void postLine(PolylineOptions lineOptions) {
        if(lineOptions != null){
            mMap.addPolyline(lineOptions);
            progressDialog.hide();
        }
    }
}
