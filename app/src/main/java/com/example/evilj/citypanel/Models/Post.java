package com.example.evilj.citypanel.Models;

import android.support.annotation.Nullable;

/**
 * Created by JjaviMS on 19/06/2018.
 *
 * @author JJaviMS
 */
public class Post {
    private String message;
    @Nullable private String imageURL;
    private String creadorUID;
    private String city;
    private String creadorName;
    private String creadorImageURL;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }
    public Post (){

    }

    @Nullable
    public String getImageURL() {
        return imageURL;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCreadorUID(String creadorUID) {
        this.creadorUID = creadorUID;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCreadorName(String creadorName) {
        this.creadorName = creadorName;
    }

    public void setCreadorImageURL(String creadorImageURL) {
        this.creadorImageURL = creadorImageURL;
    }

    public Post(String message, @Nullable String imageURL, String creadorUID, String city, String creadorName, String creadorImageURL) {
        this.message = message;
        this.imageURL = imageURL;
        this.creadorUID = creadorUID;
        this.city = city;
        this.creadorName = creadorName;
        this.creadorImageURL = creadorImageURL;

    }

    public String getCity() {
        return city;
    }

    public String getCreadorUID() {
        return creadorUID;
    }

    public String getCreadorName() {
        return creadorName;
    }

    public String getCreadorImageURL() {
        return creadorImageURL;
    }

    public void setImageURL(@Nullable String imageURL) {
        this.imageURL = imageURL;
    }
}
