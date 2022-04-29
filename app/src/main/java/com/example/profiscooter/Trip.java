package com.example.profiscooter;

public class Trip {

    public String dateTime, tripName, totalDistance, distanceTime, averageSpeed, batteryDrain;

    public Trip(){

    }



    public Trip(String dateTime, String tripName, String totalDistance, String distanceTime, String averageSpeed, String batteryDrain){
        this.dateTime = dateTime;
        this.tripName = tripName;
        this.totalDistance = totalDistance;
        this.distanceTime = distanceTime;
        this.averageSpeed = averageSpeed;
        this.batteryDrain = batteryDrain;
    }

    public String getTotalDistance(){
        return totalDistance;
    }

    public String getDistanceTime(){
        return distanceTime;
    }

    public String getAverageSpeed() { return averageSpeed; }

    public String getBatteryDrain(){
        return batteryDrain;
    }

    public String getTripName(){ return tripName; }

    public String getDateTime(){ return dateTime; }

}
