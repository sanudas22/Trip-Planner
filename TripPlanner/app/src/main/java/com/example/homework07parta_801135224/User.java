package com.example.homework07parta_801135224;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {

    private String fName, lName, imageUrl,uid, imageUid;

    private ArrayList<String> friendsUids;

    private ArrayList<String> receivedFriendRequestUids;

    private ArrayList<String> sentFriendRequestUids;

    private ArrayList<String> tripUids;

    enum GENDER{
        MALE , FEMALE,NOT_SET;
    }

    enum FRIEND_STATUS{
        FRIEND, SENT, RECEIVED, UNCONNECTED;
    }

    FRIEND_STATUS status;

    GENDER gender;

    public FRIEND_STATUS getStatus() {
        return status;
    }

    public void setStatus(FRIEND_STATUS status) {
        this.status = status;
    }

    public GENDER getGender() {
        return gender;
    }

    public void setGender(GENDER gender) {
        this.gender = gender;
    }

    public String getImageUid() {
        return imageUid;
    }

    public void setImageUid(String imageUid) {
        this.imageUid = imageUid;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String> getFriendsUids() {
        if(friendsUids == null){
            friendsUids = new ArrayList<>();
        }
        return friendsUids;
    }

    public void setFriendsUids(ArrayList<String> friendsUids) {
        this.friendsUids = friendsUids;
    }

    public ArrayList<String> getTripUids() {
        if(tripUids == null){
            tripUids = new ArrayList<>();
        }
        return tripUids;
    }

    public void setTripUids(ArrayList<String> tripUids) {
        this.tripUids = tripUids;
    }

    public ArrayList<String> getReceivedFriendRequestUids() {
        if(receivedFriendRequestUids == null){
            receivedFriendRequestUids = new ArrayList<>();
        }
        return receivedFriendRequestUids;
    }

    public void setReceivedFriendRequestUids(ArrayList<String> receivedFriendRequestUids) {
        this.receivedFriendRequestUids = receivedFriendRequestUids;
    }

    public ArrayList<String> getSentFriendRequestUids() {
        if(sentFriendRequestUids == null){
            sentFriendRequestUids = new ArrayList<>();
        }
        return sentFriendRequestUids;
    }

    public void setSentFriendRequestUids(ArrayList<String> sentFriendRequestUids) {
        this.sentFriendRequestUids = sentFriendRequestUids;
    }

    public void removeTripId(String tripId){
        if(tripUids != null){
            tripUids.remove(tripId);
        }
    }

    public User(String fName, String lName, String imageUrl, String uid) {
        this.fName = fName;
        this.lName = lName;
        this.imageUrl = imageUrl;
        this.uid = uid;

    }

    public void addTripUid(String tripUid){
        if(tripUids == null){
            tripUids = new ArrayList<>();
        }
        tripUids.add(tripUid);
    }

    public void addFriendUid(String friendUid){
        if(friendsUids == null){
            friendsUids = new ArrayList<>();

        }
        friendsUids.add(friendUid);
    }

    public void removeFriendUid(String friendUid){
        if(friendsUids != null){
            friendsUids.remove(friendUid);
        }
    }

    public void addToSentFriendRequestUid(String friendUid){
        if(sentFriendRequestUids == null){
            sentFriendRequestUids = new ArrayList<>();
        }
        sentFriendRequestUids.add(friendUid);
    }

    public void addToReceivedFriendRequestUid(String friendUid){
        if(receivedFriendRequestUids == null){
            receivedFriendRequestUids = new ArrayList<>();
        }
        receivedFriendRequestUids.add(friendUid);
    }

    public void removeFromReceivedRequestUid(String friendUid){
        if(receivedFriendRequestUids.contains(friendUid)){
            receivedFriendRequestUids.remove(friendUid);
        }
    }

    public void removeFromSentFriendRequestUid(String friendUid){
        if(sentFriendRequestUids.contains(friendUid)){
            sentFriendRequestUids.remove(friendUid);
        }
    }

    public User() {
    }

    public Map<String,Object> toMap(){

        HashMap<String,Object> result = new HashMap<>();
        result.put("fName",fName);
        result.put("fname",fName.toLowerCase());
        result.put("lName",lName);
        result.put("lname",lName.toLowerCase());
        result.put("imageUrl",imageUrl);
        result.put("uid",uid);
        result.put("friendsUids",friendsUids);
        result.put("tripUids",tripUids);
        result.put("imageUid",imageUid);
        result.put("gender",gender);
        result.put("sentFriendRequestUids",sentFriendRequestUids);
        result.put("receivedFriendRequestUids",receivedFriendRequestUids);

        return result;
    }

    @Override
    public String toString() {
        return  fName + ", " + lName;
    }
}
