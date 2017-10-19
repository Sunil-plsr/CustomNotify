package com.plsr.sunil.customnotify;

/**
 * Created by sunil on 8/31/17.
 */

public class Time {
    int startHour, endHour, startMinute, endMinute;

    public Time(int startHour, int endHour, int startMinute, int endMinute) {
        this.startHour = startHour;
        this.endHour = endHour;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndMinute() {
        return endMinute;
    }
}
