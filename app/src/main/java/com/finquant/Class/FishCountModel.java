package com.finquant.Class;

public class FishCountModel {
    private int fishCount;
    private String timeStamp;
    private String tankName;
    private String tankKey; // New field for the tank's unique key

    public FishCountModel() {
        // Default constructor required for Firebase
    }

    public FishCountModel(int fishCount, String timeStamp, String tankName, String tankKey) {
        this.fishCount = fishCount;
        this.timeStamp = timeStamp;
        this.tankName = tankName;
        this.tankKey = tankKey;
    }

    public int getFishCount() {
        return fishCount;
    }

    public void setFishCount(int fishCount) {
        this.fishCount = fishCount;
    }

    public String getTimestamp() {
        return timeStamp;
    }

    public void setTimestamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTankName() {
        return tankName;
    }

    public void setTankName(String tankName) {
        this.tankName = tankName;
    }

    public String getTankKey() {
        return tankKey;
    }

    public void setTankKey(String tankKey) {
        this.tankKey = tankKey;
    }

    // You can also add other getter and setter methods as needed
}
