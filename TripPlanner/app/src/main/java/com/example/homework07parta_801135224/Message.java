package com.example.homework07parta_801135224;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Message {

    String text,user_name,  image_url, id;

    Date posted_time;

    ArrayList<Post> posts;

    ArrayList<String> usersWhoDeletedThisMessage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getPosted_time() {
        return posted_time;
    }

    boolean post_type;

    public void setPosted_time(Date posted_time) {

        this.posted_time = posted_time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }


    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public boolean getPost_type() {
        return post_type;
    }

    public void setPost_type(boolean post_type) {
        this.post_type = post_type;
    }

    public ArrayList<Post> getComments() {
        return posts;
    }

    public void setComments(ArrayList<Post> comments) {
        this.posts = comments;
    }

    public ArrayList<String> getUsersWhoDeletedThisMessage() {
        return usersWhoDeletedThisMessage;
    }

    public void setUsersWhoDeletedThisMessage(ArrayList<String> usersWhoDeletedThisMessage) {
        this.usersWhoDeletedThisMessage = usersWhoDeletedThisMessage;
    }

    public void addComment(Post comment) {
        if(posts==null)
            posts= new ArrayList<>();

        posts.add(comment);
    }

    public Message() {
    }

    public Map<String,Object> toMap(){


        HashMap<String,Object> result = new HashMap<>();
        result.put("text",text);
        result.put("user_name",user_name);
        result.put("posted_time",posted_time);
        result.put("image_url",image_url);
        result.put("posts",posts);
        result.put("post_type",post_type);
        result.put("id",id);





        return result;
    }

    public void addToUserWhoDeletedThisMessage(String uid){
        if(usersWhoDeletedThisMessage == null){
            usersWhoDeletedThisMessage = new ArrayList<>();
        }
        usersWhoDeletedThisMessage.add(uid);
    }

    public Message(String text, String user_name, Date posted_time, String image_url, boolean post_type,String key) {

        this.text = text;
        this.user_name = user_name;
        this.posted_time = posted_time;
        this.image_url = image_url;
        this.post_type = post_type;
        this.id = key;
    }

    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                ", user_name='" + user_name + '\'' +
                ", image_url='" + image_url + '\'' +
                ", id='" + id + '\'' +
                ", posted_time=" + posted_time +
                ", posts=" + posts +
                ", post_type=" + post_type +
                '}';
    }
}
