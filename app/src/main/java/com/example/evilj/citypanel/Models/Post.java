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



    public String getMessage() {
        return message;
    }
    public Post (){

    }

    @Nullable
    public String getImageURL() {
        return imageURL;
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
