package com.example.myprayertimes;

import android.os.Build;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

public class SimpleDateTime {

    int year;
    int month;
    int day;
    int hour;
    int minute;
    int second;
    public SimpleDateTime(int year, int month, int day, int hour, int minute, int second) {
        super();
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public SimpleDateTime(SimpleDate sd) {
        super();
        this.year = sd.year;
        this.month = sd.month;
        this.day = sd.day;

        this.hour = 0;
        this.minute = 0;
        this.second = 0;
    }

    public SimpleDateTime(Date d) {
        super();
        this.year = d.getYear() + 1900;
        this.month = d.getMonth() + 1;
        this.day = d.getDate();
        this.hour = d.getHours();
        this.minute = d.getMinutes();
        this.second = d.getSeconds();
    }

    public long getUnixTimeSeconds() {
        if(this.year < 1970) {
            throw new Error("year is below -70");
        }

        Instant d = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            d = Instant.parse(toStr());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return d.toEpochMilli()/1000;
        }
        else{
            return 0;
        }
    }

    @Override
    public String toString() {
        return "SimpleDateTime [year=" + year + ", month=" + month + ", day=" + day + ", hour=" + hour + ", minute="
                + minute + ", second=" + second + "]";
    }

    public String toStr() {
        String mo = month < 10 ? ("0"+month) : ""+month;
        String da = day < 10 ? ("0"+day) : ""+day;
        String ho = hour < 10 ? ("0"+hour) : ""+hour;
        String min = minute < 10 ? ("0"+minute) : ""+minute;
        String sec = second < 10 ? ("0"+second) : ""+second;
        return year+"-"+mo+"-"+da+"T"+ho+":"+min+":"+sec+"Z";
    }

    static ZonedDateTime epochToDate(long epochMilli) {
        Instant i = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            i = Instant.ofEpochMilli(epochMilli);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return i.atZone(ZoneOffset.UTC);
        }
        else{
            return null;
        }
    }

    static ZonedDateTime epochToDateLocal(long epochMilli) {
        Instant i = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            i = Instant.ofEpochMilli(epochMilli);
        }
        ZoneId zId = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            zId = ZoneId.systemDefault();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return i.atZone(zId);
        }
        else{
            return null;
        }
    }
}
