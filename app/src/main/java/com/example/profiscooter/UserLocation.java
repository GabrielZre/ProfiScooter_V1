package com.example.profiscooter;

public class UserLocation {

    public Double date, latitude, longitude;

    public UserLocation(){

    }

    public UserLocation(Double date, Double latitude, Double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getDate() {
        return date;
    }
}
