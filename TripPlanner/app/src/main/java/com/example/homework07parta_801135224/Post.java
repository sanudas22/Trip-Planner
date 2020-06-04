package com.example.homework07parta_801135224;

import java.util.Date;

public class Post {
    String name;
    String text;
    String id;
    Date comment_time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getComment_time() {
        return comment_time;
    }

    public void setComment_time(Date comment_time) {
        this.comment_time = comment_time;
    }

    /*public Map<String,Object> toMap(){

        HashMap<String,Object> result = new HashMap<>();
        result.put("text",text);
        result.put("name",name);
        result.put("comment_time",comment_time);
        return result;
    }*/

    public Post(String name, String text, Date comment_time, String id) {
        this.name = name;
        this.text = text;
        this.comment_time = comment_time;
        this.id=id;
    }

    public Post() {
    }
}
