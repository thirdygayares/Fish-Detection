package com.finquant.Class;
// TankLog.java
public class TankLog {
    private String tankName;
    private int fishCount;
    private String timeStamp;

    public TankLog() {
        // Default constructor required for Firebase
    }

    public TankLog(String tankName, int fishCount, String timeStamp) {
        this.tankName = tankName;
        this.fishCount = fishCount;
        this.timeStamp = timeStamp;
    }

    public String getTankName() {
        return tankName;
    }

    public int getFishCount() {
        return fishCount;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}
