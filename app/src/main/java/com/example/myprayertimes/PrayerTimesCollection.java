package com.example.myprayertimes;

public class PrayerTimesCollection {
    PrayerTime fajr;
    PrayerTime sunrise;
    PrayerTime zhuhr;
    PrayerTime asr;
    PrayerTime maghrib;
    PrayerTime ishaa;

    String locationName;
    String locationCoordinates;

    String date;

    Boolean activeDST;

    int year, month, day;

    /*
    public PrayerTimesCollection(PrayerTime fajr, PrayerTime sunrise, PrayerTime zhuhr, PrayerTime asr, PrayerTime maghrib, PrayerTime ishaa) {
        this.fajr = fajr;
        this.sunrise = sunrise;
        this.zhuhr = zhuhr;
        this.asr = asr;
        this.maghrib = maghrib;
        this.ishaa = ishaa;
    }

    public PrayerTimesCollection(PrayerTime fajr, PrayerTime sunrise, PrayerTime zhuhr, PrayerTime asr, PrayerTime maghrib, PrayerTime ishaa, String locationName_, String locationCoordinates_, String dateText_) {
        this.fajr = fajr;
        this.sunrise = sunrise;
        this.zhuhr = zhuhr;
        this.asr = asr;
        this.maghrib = maghrib;
        this.ishaa = ishaa;
        this.locationName = locationName_;
        this.locationCoordinates = locationCoordinates_;
        this.date = dateText_;
    }*/

    public PrayerTimesCollection(PrayerTime fajr, PrayerTime sunrise, PrayerTime zhuhr, PrayerTime asr, PrayerTime maghrib, PrayerTime ishaa, String locationName_, String locationCoordinates_, String dateText_, boolean activeDST_) {
        this.fajr = fajr;
        this.sunrise = sunrise;
        this.zhuhr = zhuhr;
        this.asr = asr;
        this.maghrib = maghrib;
        this.ishaa = ishaa;
        this.locationName = locationName_;
        this.locationCoordinates = locationCoordinates_;
        this.date = dateText_;
        this.activeDST = activeDST_;
    }


    public PrayerTimesCollection(PrayerTime fajr, PrayerTime sunrise, PrayerTime zhuhr, PrayerTime asr, PrayerTime maghrib, PrayerTime ishaa, String locationName_, String locationCoordinates_, String dateText_, boolean activeDST_, int year_, int month_, int day_) {
        this.fajr = fajr;
        this.sunrise = sunrise;
        this.zhuhr = zhuhr;
        this.asr = asr;
        this.maghrib = maghrib;
        this.ishaa = ishaa;
        this.locationName = locationName_;
        this.locationCoordinates = locationCoordinates_;
        this.date = dateText_;
        this.activeDST = activeDST_;

        this.year = year_;
        this.month = month_;
        this.day = day_;
    }

    @Override
    public String toString() {
        return locationName + "\n" +
                locationCoordinates + "\n" +
                date + "\n"+
                fajr + "\n" +
                sunrise + "\n" +
                zhuhr + "\n" +
                asr + "\n" +
                maghrib + "\n" +
                ishaa;
    }
}
