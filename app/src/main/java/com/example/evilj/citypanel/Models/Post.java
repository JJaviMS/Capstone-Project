package com.example.evilj.citypanel.Models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * Created by JjaviMS on 19/06/2018.
 *
 * @author JJaviMS
 */
public class Post implements Parcelable {
    private String message;
    @Nullable private String imageURL;
    private String creadorUID;
    private String city;
    private String creadorName;
    @Nullable private String creadorImageURL;
    private String id;

    private Post(Parcel in) {
        message = in.readString();
        imageURL = in.readString();
        creadorUID = in.readString();
        city = in.readString();
        creadorName = in.readString();
        creadorImageURL = in.readString();
        id = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

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

    public void setCreadorImageURL(@Nullable String creadorImageURL) {
        this.creadorImageURL = creadorImageURL;
    }

    public Post(String message, @Nullable String imageURL, String creadorUID, String city, String creadorName, @Nullable String creadorImageURL) {
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

    @Nullable
    public String getCreadorImageURL() {
        return creadorImageURL;
    }

    public void setImageURL(@Nullable String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(message);
        parcel.writeString(imageURL);
        parcel.writeString(creadorUID);
        parcel.writeString(city);
        parcel.writeString(creadorName);
        parcel.writeString(creadorImageURL);
        parcel.writeString(id);
    }
}
