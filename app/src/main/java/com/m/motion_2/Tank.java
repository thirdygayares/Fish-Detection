package com.m.motion_2;

public class Tank {
    private String tankName;
    private int fishCount;
    private String timestamp;

    // Empty constructor required for Firebase
    public Tank() {
    }

    public Tank(String tankName, int fishCount, String timestamp) {
        this.tankName = tankName;
        this.fishCount = fishCount;
        this.timestamp = timestamp;
    }

    public String getTankName() {
        return tankName;
    }

    public void setTankName(String tankName) {
        this.tankName = tankName;
    }

    public int getFishCount() {
        return fishCount;
    }

    public void setFishCount(int fishCount) {
        this.fishCount = fishCount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

