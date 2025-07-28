package com.example.myprayertimes;

public class SimpleDate {
    int year;
    int month;
    int day;

    public SimpleDate(int year, int month, int day) {
        super();
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public SimpleDate(SimpleDateTime sdt) {
        super();
        this.year = sdt.year;
        this.month = sdt.month;
        this.day = sdt.day;
    }
}
