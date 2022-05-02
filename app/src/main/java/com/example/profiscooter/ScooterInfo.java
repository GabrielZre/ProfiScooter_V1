package com.example.profiscooter;

public class ScooterInfo {
    public String batteryAh, batteryVoltage, motorWatt, bottomCutOff, upperCutOff;

    public ScooterInfo(String batteryAh, String batteryVoltage, String motorWatt, String bottomCutOff, String upperCutOff) {
        this.batteryAh = batteryAh;
        this.batteryVoltage = batteryVoltage;
        this.motorWatt = motorWatt;
        this.bottomCutOff = bottomCutOff;
        this.upperCutOff = upperCutOff;
    }

    public ScooterInfo(){

    }

    public String getBatteryAh() {
        return batteryAh;
    }

    public String getBatteryVoltage() {
        return batteryVoltage;
    }

    public String getMotorWatt() {
        return motorWatt;
    }

    public String getBottomCutOff() { return bottomCutOff; }

    public String getUpperCutOff() { return upperCutOff; }
}
