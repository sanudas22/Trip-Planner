package com.example.homework07parta_801135224;

import android.util.Log;

import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TripInfo {
    private String title, location, imageUrl, trip_id, description, organizer_id;

    ArrayList<String> friendsUids;

    ArrayList<Message> messages;

    ArrayList<LocationDetails> places;

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public ArrayList<String> getFriendsUids() {
        return friendsUids;
    }

    public void setFriendsUids(ArrayList<String> friendsUids) {
        this.friendsUids = friendsUids;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrganizer_id() {
        return organizer_id;
    }

    public void setOrganizer_id(String organizer_id) {
        this.organizer_id = organizer_id;
    }

    public ArrayList<LocationDetails> getPlaces() {
        return places;
    }

    public void setPlaces(ArrayList<LocationDetails> places) {
        this.places = places;
    }

    public void addFriendUid (String friendUid){
        if(friendsUids == null){
            friendsUids = new ArrayList<>();
        }
        friendsUids.add(friendUid);
    }

    public void addMessage (Message message){
        if(messages == null){
            messages = new ArrayList<>();
        }
        messages.add(message);
    }

    public boolean addPlaceToTrip(LocationDetails place){

        boolean isAdded = false;
        if(places == null){
            places =  new ArrayList<>();
        }
        if(!places.contains(place)){
            places.add(place);
            isAdded = true;
            Log.d("demo",isAdded +"");
        }
        return isAdded;
    }

    public void removeMemberFromTrip(String uid){
        if(friendsUids != null){
            friendsUids.remove(uid);
        }
    }

    public void removePlaceFromTrip(Place place){
        if(places != null){
            places.remove(place);
        }
    }

    public void addFriends(ArrayList<String> friendUid){
        if(friendsUids == null){
            friendsUids = new ArrayList<>();
        }
        friendsUids.addAll(friendUid);


    }

    public void addLocations(ArrayList<LocationDetails> place){
        if(places == null){
            places = new ArrayList<>();
        }
        places.addAll(place);
    }

    public void swapLocations (int i,int j){
        if(places != null){
            Collections.swap(places,i,j);
        }
    }

    public TripInfo() {
    }

    public TripInfo(String title, String location, String imageUrl, String trip_id, String description,String organizer_id) {
        this.title = title;
        this.location = location;
        this.imageUrl = imageUrl;
        this.trip_id = trip_id;
        this.description = description;
        this.organizer_id = organizer_id;
    }

    public Map<String,Object> toMap(){

        HashMap<String,Object> result = new HashMap<>();
        result.put("title",title);
        result.put("location",location);
        result.put("imageUrl",imageUrl);
        result.put("trip_id",trip_id);
        result.put("description",description);
        result.put("organizer_id",organizer_id);
        result.put("friendsUids",friendsUids);
        result.put("messages",messages);
        result.put("places",places);


        return result;
    }

    @Override
    public String toString() {
        return "TripDetails{" +
                "title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", trip_id='" + trip_id + '\'' +
                ", description='" + description + '\'' +
                ", organizer_id='" + organizer_id + '\'' +
                ", friendsUids=" + friendsUids +
                ", messages=" + messages +
                '}';
    }
}
