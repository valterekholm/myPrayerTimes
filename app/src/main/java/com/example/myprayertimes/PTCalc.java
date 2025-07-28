package com.example.myprayertimes;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PTCalc {
    static double RADEG = ( 180.0 / Math.PI );
    static double DEGRAD = ( Math.PI / 180.0 );

    double dayLen, civLen, nautLen, astrLen;//length in time
    double rise, set;
    double timespanSet; // added 241029
    DoubleWrapperClass asrStart = new DoubleWrapperClass();
    DoubleWrapperClass asrEnd = new DoubleWrapperClass(); //only one used


    //Sun's Right Ascension, declination, distance (AU)
    double sRA, sunDec, sr;//sRA, sunDec become class

    int rs, civ, naut, astr, ishaa;

    int timeZoneHour;
    int timeZoneMinute;//to replace hour, to cover for quarter/half hour zones

    double lon;
    double lat;
    String location = "";
    int year = 2024;
    int month = 6;
    int day = 1;
    //TODO: read those from file, try

    LocalDate timeInfo;

    boolean isDst;
    //TODO: test

    double solarLongitude;
    double solarDistance;

    //test
    //TODO: replace Classes SRAVars etc with DoubleWrapperClass
    //double solarLongitude2;//unsure if need several "versions"
    DoubleWrapperClass solarLongitude_;//to use it by reference in sunpos2
    DoubleWrapperClass solarDistance_;
    DoubleWrapperClass sRA_;
    DoubleWrapperClass sunDec_;

    static int zhuhrAfterZenitMinutes = 10;
    boolean useBottomLimb=false;


    //double noon;

    static double sind(double x) {
        return Math.sin((x)*DEGRAD);
    }

    static double cosd(double x) {
        return Math.cos((x)*DEGRAD);
    }
    static double tand(double x) {
        return Math.tan((x)*DEGRAD);
    }

    static double atand(double x) {
        return (RADEG*Math.atan(x));
    }

    static double asind(double x) {
        return RADEG*Math.asin(x);
    }
    static double acosd(double x) {
        return RADEG*Math.acos(x);
    }
    static double atan2d(double y,double x) {
        return RADEG*Math.atan2(y,x);
    }


    public PTCalc(LocalDate date, double lon, double lat, String location, int timeZoneMinute_){
        this.timeInfo = date;
        this.lon = lon;
        this.lat = lat;
        this.location = location;
        this.timeZoneMinute = timeZoneMinute_;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.year = timeInfo.getYear();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.month = timeInfo.getMonthValue();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.day = timeInfo.getDayOfMonth();
        }

        solarLongitude_ = new DoubleWrapperClass();
        solarDistance_ = new DoubleWrapperClass();
        sRA_ = new DoubleWrapperClass();
        sunDec_ = new DoubleWrapperClass();


        //TimeZone madridTimeZone = TimeZone.getTimeZone("Europe/Stockholm");
        //TimeZone
        //timeZoneHour = 1;//TODO: läs från user/fil
        String timeZoneTxt = "02:00";
        ZoneId fixedOffset = null; // Example: UTC+02:00
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fixedOffset = ZoneId.of("UTC+" + timeZoneTxt);
        }

        TimeZone myTimeZone = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myTimeZone = TimeZone.getTimeZone(fixedOffset);
        }

        //int DST = myTimeZone.getDSTSavings();

        //System.out.println("DST: " + DST);

        //rätt, för lokal app
        boolean isDST = TimeZone.getDefault().inDaylightTime( new Date() );
        this.isDst = isDST;

        String hourString;
        if(isDST) {
            timeZoneMinute += 60;
        }
        hourString = String.format("%02d", timeZoneHour) + ":00";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fixedOffset = ZoneId.of("UTC+" + hourString);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myTimeZone = TimeZone.getTimeZone(fixedOffset);
        }


        String myDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myDate = timeInfo.getYear() + "." + timeInfo.getMonthValue() + "." + timeInfo.getDayOfMonth();
        }

        System.out.println(location + ", lon " + lon + ", lat " + lat + ", date: " + myDate);
        System.out.println("isDst: " + isDst);

    }

    /**
     * Creates PTCalc with date auto set (today)
     * @param lon - longitude
     * @param lat - latitude
     * @param location - an informative string
     * @param timeZoneMinute_ - for sweden: 60
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public PTCalc(double lon, double lat, String location, int timeZoneMinute_) {
        this(LocalDate.now(),lon,lat,location,timeZoneMinute_);
    }

    PrayerTimesCollection run() {
        //System.out.println("run");
        dayLen = dayLen();
        civLen = civLen();
        nautLen = nautLen();
        astrLen = astrLen();


        //nr.1
        //PrayerTime fadjr = new PrayerTime("Fadjr", true, (short) this.timeZoneMinute,false, year, month, day, lat, lon, useBottomLimb);
        PrayerTime sunrise = new PrayerTime("Sunrise", true, (short) this.timeZoneMinute,false, year, month, day, lat, lon, useBottomLimb);
        PrayerTime maghrib = new PrayerTime("Maghrib", false, (short) this.timeZoneMinute,true, year, month, day, lat, lon, useBottomLimb);
        sunrise.calculate(PrayerTime.TimeName.riseSet);
        maghrib.calculate(PrayerTime.TimeName.riseSet);

        //test
        //PrayerTime srise = new PrayerTime("SUNR", true, (short) timeZoneMinute, true, year, month, day, lat, lon, useBottomLimb);
        //srise.calculate(PrayerTime.TimeName.riseSet);
        //System.out.println("======>>>" + srise);

        //Added 241029 to find out timespan for sun set
        //useBottomLimb = true;
        //rs = sunRiset2();
        PrayerTime sunrise2 = new PrayerTime("Sunrise2", true, (short) this.timeZoneMinute, true, year, month, day, lat, lon, useBottomLimb);
        PrayerTime maghrib2 = new PrayerTime("Maghrib2", false, (short) this.timeZoneMinute, false, year, month, day, lat, lon, useBottomLimb);
        sunrise2.calculate(PrayerTime.TimeName.riseSet2);
        maghrib2.calculate(PrayerTime.TimeName.riseSet2);
        
        //useBottomLimb = false;
        //changed 20241127 to use functions moved to PrayerTime class

        //nr.x
        //System.out.println("Zhuhr: " + getDecimalTimeWithCorrectionH((this.rise + this.set)/2, this.isDst, this.timeZoneHour));
        PrayerTime _zhuhr = new PrayerTime("Zhuhr", false, (short) this.timeZoneMinute, true, year, month, day, lat, lon, useBottomLimb);
        //now rise/set is what they "promise" to be?
        //no i guess, but the times still sit evenly from zenith
        //_zhuhr.time2 = (this.rise + this.set)/2 +
                //((double)zhuhrAfterZenitMinutes/60);//to apply a f rule that a decrease of shadow from zenith is needed
        //System.out.println("Zhuhr rå värde: " + _zhuhr.time2);
        _zhuhr.calculate(PrayerTime.TimeName.zhuhr);
        //System.out.println("Zhuhr: " + formatRiseTime((this.rise + this.set)/2, true, this.timeZoneHour, 0));

        //nr.2
        civ = civilTwilight();


        //nr.3
        naut = nauticalTwilight();

        //nr.4
        //astr = astronomicalTwilight();
        PrayerTime fadjr = new PrayerTime("Fadjr", true, (short) this.timeZoneMinute,false, year, month, day, lat, lon, useBottomLimb);
        //fadjr.time1 = this.rise;
        //fadjr.time2 = this.set;
        //System.out.println("Fadjr: " + formatRiseTime(this.rise, true, this.timeZoneHour, astr));
        //System.out.println("astr tw: " + formatRiseTime(this.set, true, this.timeZoneHour, astr));

        fadjr.calculate(PrayerTime.TimeName.fajr);

        //nr.5
        //ishaa = ishaaTwilight();
        PrayerTime _3ishaa = new PrayerTime("Ishaa", false, (short) this.timeZoneMinute, true, year, month, day, lat, lon, useBottomLimb);
        //_3ishaa.time1 = this.rise;
        //_3ishaa.time2 = this.set;
        //System.out.println("ishaa tw: " + formatRiseTime(this.rise, true, this.timeZoneHour, ishaa));
        //System.out.println("Ishaa2: " + formatRiseTime(this.set, true, this.timeZoneHour, ishaa));

        _3ishaa.calculate(PrayerTime.TimeName._3shaa);

        //nr.6
        double noo = (this.rise+this.set)/2;
        double elevationNoon = this.getElevationAtNoon(0, noo, this.timeZoneMinute);
        //System.out.println("elevationNoon: " + elevationNoon);

        //asr calculation, shadow lenghts
        double factor = 1/cosd(90-elevationNoon);//sin has 0 degr upwards, elevation has 0 degrees at the horizon
        //printf("factor: %f\n", factor);
        double shadow = sind(90-elevationNoon) * factor;
        //printf("shadow: %f\n", shadow);

        //double shadow_asr = shadow*2;//wrong
        double shadow_asr = 1+shadow;

        //printf("shadow asr: %f\n", shadow_asr);

        //double hypot_asr = sqrt(1+(shadow*2)*(shadow*2));
        double hypot_asr = Math.sqrt(1+(shadow_asr*shadow_asr));
        //printf("hypotenusan asr: %f\n", hypot_asr);

        //sine of x equals the opposite over the hypotenus => 1/hypot_asr

        double arc_sine_asr = asind(1/hypot_asr);
        //printf("arc_sine_asr: %f\n", arc_sine_asr);

        //double altit, boolean upperLimb
        //int asr = sunRiset_( arc_sine_asr, false);
        PrayerTime _3a$r = new PrayerTime("Asr", false, (short) this.timeZoneMinute, true, year, month, day, lat, lon, useBottomLimb);
        //_3a$r.time1 = this.rise;
        //_3a$r.time2 = this.set;

        _3a$r.calculate(PrayerTime.TimeName._3$r);

        System.out.println(fadjr);
        System.out.println(sunrise);
        System.out.println(_zhuhr);
        System.out.println(_3a$r);
        System.out.println(maghrib);
        System.out.println(_3ishaa);

        PrayerTimesCollection collection = new PrayerTimesCollection(fadjr, sunrise, _zhuhr, _3a$r, maghrib, _3ishaa, this.location, this.lon + " " + this.lat, this.timeInfo.toString(), this.isDst);

        //Added 241029, calc of sunset timespan
        double sunsetTimeSpan = maghrib.time2 - maghrib2.time2;
        String timeStr = PTCalc.formatRiseTime(sunsetTimeSpan, true, 0, maghrib2.status);
        System.out.println("Timespan sunset: " + timeStr);


        //test

        System.out.println(days_since_2000_Jan_0(2024,2,30));
        System.out.println(days_since_2000_Jan_0(2024,3,1));

        return collection;
    }

    double dayLen() {
        return dayLen_(-35.0/60.0, true);

    }
    //day_civil_twilight_length
    double civLen() {
        return dayLen_(-6, false);

    }
    //day_nautical_twilight_length
    double nautLen() {
        return dayLen_(-12, false);

    }
    //day_astronomical_twilight_length
    double astrLen() {
        return dayLen_(-18, false);

    }
    /**
     *
     * @param altit
     *
     * the altitude which the Sun should cross
     * Set to -35/60 degrees for rise/set, -6 degrees
     * for civil, -12 degrees for nautical and -18
     * degrees for astronomical twilight.
     * @param upperLimb
     * @return
     */
    double dayLen_(double altit, boolean upperLimb) {
        double d = days_since_2000_Jan_0(year, month, day) + 0.5 - lon / 360.0;
        //System.out.println("d = " + d);
        double obl_ecl = 23.4393 - 3.563E-7 * d;
        //sunpos
        //setting local variables
        sunPos2(d, this.solarLongitude_, this.solarDistance_);
        //Sine of Sun's declination
        double sinSDecline = sind(obl_ecl) * sind(this.solarLongitude_.value);
        //Cosine of Sun's declination
        double cosSDecline = Math.sqrt(1.0 - sinSDecline * sinSDecline);

        //Compute the Sun's apparent radius, degrees
        double sradius = 0.2666 / this.solarDistance_.value;//todo: check that member variable equals sr

        if(upperLimb) {
            altit -= sradius;
        }
        /* Compute the diurnal arc that the Sun traverses to reach */
        /* the specified altitude altit: */
        double cost;
        cost = ( sind(altit) - sind(lat) * sinSDecline ) /
                ( cosd(lat) * cosSDecline );
        //Diurnal arc
        double t;
        if ( cost >= 1.0 )
            t = 0.0;                      /* Sun always below altit */
        else if ( cost <= -1.0 )
            t = 24.0;                     /* Sun always above altit */
        else  t = (2.0/15.0) * acosd(cost); /* The diurnal arc, hours */

        return t;
    }

    //will affect rise, set
    /* Note: year,month,date = calendar date, 1801-2099 only.             */
    /*       Eastern longitude positive, Western longitude negative       */
    /*       Northern latitude positive, Southern latitude negative       */
    /*       The longitude value IS critical in this function!            */
    /*       altit = the altitude which the Sun should cross              */
    /*               Set to -35/60 degrees for rise/set, -6 degrees       */
    /*               for civil, -12 degrees for nautical and -18          */
    /*               degrees for astronomical twilight.                   */
    /*         upper_limb: non-zero -> upper limb, zero -> center         */
    /*               Set to non-zero (e.g. 1) when computing rise/set     */
    /*               times, and to zero when computing start/end of       */
    /*               twilight.                                            */
    /*        *rise = where to store the rise time                        */
    /*        *set  = where to store the set  time                        */
    /*                Both times are relative to the specified altitude,  */
    /*                and thus this function can be used to compute       */
    /*                various twilight times, as well as rise/set times   */
    /* Return value:  0 = sun rises/sets this day, times stored at        */
    /*                    *trise and *tset.                               */
    /*               +1 = sun above the specified "horizon" 24 hours.     */
    /*                    *trise set to time when the sun is at south,    */
    /*                    minus 12 hours while *tset is set to the south  */
    /*                    time plus 12 hours. "Day" length = 24 hours     */
    /*               -1 = sun is below the specified "horizon" 24 hours   */
    /*                    "Day" length = 0 hours, *trise and *tset are    */
    /*                    both set to the time when the sun is at south.  */
    /*                                                                    */
    int sunRiset_(double altit, boolean upperLimb) {

        // Sun's Right Ascension
        // double sRA;
        DoubleWrapperClass sRA = new DoubleWrapperClass();
        // dist
        // double sr;
        DoubleWrapperClass sr = new DoubleWrapperClass();
        // suns declination
        // double sdec;
        DoubleWrapperClass sdec = new DoubleWrapperClass();
        // apperent radius
        double sradius;
        // diurnal arc
        double t = 0;
        // time when sun is in south
        double tsouth;

        // Return cde from function - usually 0
        int rc = 0; // Return code from function - usually 0
        // Days since 2000 Jan 0.0 (negative before)
        double d = days_since_2000_Jan_0(this.year, this.month, this.day) + 0.5 - this.lon / 360.0;

        //System.out.println("d: " + d);
        // Local sidereal time
        double sidtime = revolution(GMST0(d) + 180.0 + this.lon);

        // Compute Sun's RA, Decl and distance at this moment //537 in code
        sunRADec(d, sRA, sdec, sr);


        /* Compute time when Sun is at south - in hours UT */
        tsouth = 12.0 - rev180(sidtime - sRA.value) / 15.0;

        /* Compute the Sun's apparent radius in degrees */
        sradius = 0.2666 / sr.value;

        /* Do correction to upper limb, if necessary */

        if (upperLimb) {
            //System.out.println(">>>" + sradius);
            altit -= sradius;
        }
        else if(useBottomLimb){
            altit += sradius;
        }

        /* Compute the diurnal arc that the Sun traverses to reach */
        /* the specified altitude altit: */
        double cost;
        //System.out.println(altit);
        //System.out.println(lat);
        //System.out.println(sdec.sunDec);

        cost = (sind(altit) - sind(lat) * sind(sdec.value)) / (cosd(lat) * cosd(sdec.value));
        //System.out.println("(sind("+altit+") - sind("+lat+") * sind("+sdec.sunDec+")) / (cosd("+lat+") * cosd("+sdec.sunDec+"))");
        //System.out.println("cost: " + cost);
        if (cost >= 1.0) {
            rc = -1;
            t = 0.0; /* Sun always below altit */
        }

        else if (cost <= -1.0) {
            rc = +1;
            t = 12.0; /* Sun always above altit */
        }

        else {
            t = acosd(cost) / 15.0; /* The diurnal arc, hours */
        }

        /* Store rise and set times - in hours UT */
        //System.out.println("t: " + t);
        this.rise = tsouth - t;
        this.set = tsouth + t;

        return rc;
    }

    int sunRiset__(double altit, boolean upperLimb, int year_, int month_, int day_) {

        // Sun's Right Ascension
        // double sRA;
        DoubleWrapperClass sRA = new DoubleWrapperClass();
        // dist
        // double sr;
        DoubleWrapperClass sr = new DoubleWrapperClass();
        // suns declination
        // double sdec;
        DoubleWrapperClass sdec = new DoubleWrapperClass();
        // apperent radius
        double sradius;
        // diurnal arc
        double t = 0;
        // time when sun is in south
        double tsouth;

        // Return cde from function - usually 0
        int rc = 0; // Return code from function - usually 0
        // Days since 2000 Jan 0.0 (negative before)
        double d = days_since_2000_Jan_0(year_, month_, day_) + 0.5 - this.lon / 360.0;

        //System.out.println("d: " + d);
        // Local sidereal time
        double sidtime = revolution(GMST0(d) + 180.0 + this.lon);

        // Compute Sun's RA, Decl and distance at this moment //537 in code
        sunRADec(d, sRA, sdec, sr);


        /* Compute time when Sun is at south - in hours UT */
        tsouth = 12.0 - rev180(sidtime - sRA.value) / 15.0;

        /* Compute the Sun's apparent radius in degrees */
        sradius = 0.2666 / sr.value;

        /* Do correction to upper limb, if necessary */

        if (upperLimb) {
            //System.out.println(">>>" + sradius);
            altit -= sradius;
        }
        else if(useBottomLimb){
            altit += sradius;
        }

        /* Compute the diurnal arc that the Sun traverses to reach */
        /* the specified altitude altit: */
        double cost;
        //System.out.println(altit);
        //System.out.println(lat);
        //System.out.println(sdec.sunDec);

        cost = (sind(altit) - sind(lat) * sind(sdec.value)) / (cosd(lat) * cosd(sdec.value));
        //System.out.println("(sind("+altit+") - sind("+lat+") * sind("+sdec.sunDec+")) / (cosd("+lat+") * cosd("+sdec.sunDec+"))");
        //System.out.println("cost: " + cost);
        if (cost >= 1.0) {
            rc = -1;
            t = 0.0; /* Sun always below altit */
        }

        else if (cost <= -1.0) {
            rc = +1;
            t = 12.0; /* Sun always above altit */
        }

        else {
            t = acosd(cost) / 15.0; /* The diurnal arc, hours */
        }

        /* Store rise and set times - in hours UT */
        //System.out.println("t: " + t);
        this.rise = tsouth - t;
        this.set = tsouth + t;

        return rc;
    }

    int sunRiset() {
        return sunRiset_(-35.0/60.0, true);
    }
    int sunRiset2(){
        return sunRiset_(-35.0/60.0, false);
    }

    int civilTwilight() {
        return sunRiset_(-6.0, false);
    }

    int nauticalTwilight() {
        return sunRiset_(-12.0, false);
    }

    int astronomicalTwilight() {
        return sunRiset_(-18.0, false);
    }

    //TODO: use argument for ishaa2?
    int ishaaTwilight() {
        return sunRiset_(-17.0, false);
    }

    /**
     *
     * @param d - days since 2000 jan 0.0
     * This should give the values solar longitude and
     * Solar distance, astronomical units
     * Computes the Sun's ecliptic longitude and distance
     * at an instant given in d, number of days since
     * 2000 Jan 0.0.  The Sun's ecliptic latitude is not
     * computed, since it's always very near 0.
     */

    void sunPos(double d) {
        /* Compute mean elements */
        //Mean anomaly of the Sun
        double M = revolution( 356.0470 + 0.9856002585 * d );
        /* Mean longitude of perihelion */
        /* Note: Sun's mean longitude = M + w */
        double w = 282.9404 + 4.70935E-5 * d;
        /* Eccentricity of Earth's orbit */
        double e = 0.016709 - 1.151E-9 * d;
        /* Eccentric anomaly */
        double E;
        /* Compute true longitude and radius vector */
        E = M + e * RADEG * sind(M) * ( 1.0 + e * cosd(M) );
        // x,y coordinates in orbit
        double x = cosd(E) - e;
        double y = Math.sqrt( 1.0 - e*e ) * sind(E);

        //making effect
        this.solarDistance = Math.sqrt(x*x + y*y);
        /* True anomaly */
        double v = atan2d( y, x );
        /* True solar longitude */
        this.solarLongitude = v + w;

        if(this.solarLongitude >= 360.0) {
            this.solarLongitude -= 360.0;
        }
    }

    /**
     * A variant that mimic the original c function more
     * @param d
     * @param sunLon
     */
    void sunPos2(double d, DoubleWrapperClass sunLon, DoubleWrapperClass sunDist) {
        /* Compute mean elements */
        //Mean anomaly of the Sun
        double M = revolution( 356.0470 + 0.9856002585 * d );
        /* Mean longitude of perihelion */
        /* Note: Sun's mean longitude = M + w */
        double w = 282.9404 + 4.70935E-5 * d;
        /* Eccentricity of Earth's orbit */
        double e = 0.016709 - 1.151E-9 * d;
        /* Eccentric anomaly */
        double E;
        /* Compute true longitude and radius vector */
        E = M + e * RADEG * sind(M) * ( 1.0 + e * cosd(M) );
        // x,y coordinates in orbit
        double x = cosd(E) - e;
        double y = Math.sqrt( 1.0 - e*e ) * sind(E);

        //making effect
        //this.solarDistance = Math.sqrt(x*x + y*y);
        sunDist.value = Math.sqrt(x*x + y*y);

        /* True anomaly */
        double v = atan2d( y, x );
        /* True solar longitude */
        //this.solarLongitude = v + w;
        sunLon.value = v + w;

        if(sunLon.value >= 360.0) {
            sunLon.value -= 360.0;
        }
    }


    /******************************************************************/
    /* This function reduces any angle to within the first revolution */
    /* by subtracting or adding even multiples of 360.0 until the     */
    /* result is >= 0.0 and < 360.0                                   */
    /******************************************************************/

    static double INV360 = ( 1.0 / 360.0 );
    double revolution( double x )
    /*****************************************/
        /* Reduce angle to within 0..360 degrees */
    /*****************************************/
    {
        return( x - 360.0 * Math.floor( x * INV360 ) );
    }  /* revolution */

    static double rev180( double x )
    /*********************************************/
        /* Reduce angle to within +180..+180 degrees */
    /*********************************************/
    {
        return( x - 360.0 * Math.floor( x * INV360 + 0.5 ) );
    }  /* revolution */

    //from stjarnhimlen.se
    static int days_since_2000_Jan_0(int y, int m, int d) {
        //System.out.println("days_since_2000_Jan_0 from " + y + "," + m + "," + d);
        return (367*(y)-((7*((y)+(((m)+9)/12)))/4)+((275*(m))/9)+(d)-730530);
    }

    /* Compute Sun's RA, Decl and distance at this moment */
    //sun_RA_dec( d, &sRA, &sdec, &sr );

    //Sets Sun's Right Ascension, Sun's declination, Solar distance, astronomical units
    /**
     *
     * @param d - number of days since 2000 jan 0.0
     */
    void sunRADec(double d, DoubleWrapperClass RA, DoubleWrapperClass dec, DoubleWrapperClass r) {
        double obl_ecl, x, y, z;
        DoubleWrapperClass sLon = new DoubleWrapperClass();
        //sDistVars sDist = new sDistVars(); //den verkade ha tagit r:s plats
        /* Compute Sun's ecliptical coordinates */

        sunPos2( d, sLon, r);


        /* Compute ecliptic rectangular coordinates (z=0) */
        x = r.value * cosd(sLon.value);
        y = r.value * sind(sLon.value);


        /* Compute obliquity of ecliptic (inclination of Earth's axis) */
        obl_ecl = 23.4393 - 3.563E-7 * d;


        /* Convert to equatorial rectangular coordinates - x is unchanged */
        z = y * sind(obl_ecl);
        y = y * cosd(obl_ecl);


        /* Convert to spherical coordinates */
        RA.value = atan2d(y, x);
        dec.value = atan2d(z, Math.sqrt(x*x + y*y));
    }

    /*******************************************************************/
    /* This function computes GMST0, the Greenwich Mean Sidereal Time  */
    /* at 0h UT (i.e. the sidereal time at the Greenwhich meridian at  */
    /* 0h UT).  GMST is then the sidereal time at Greenwich at any     */
    /* time of the day.  I've generalized GMST0 as well, and define it */
    /* as:  GMST0 = GMST - UT  --  this allows GMST0 to be computed at */
    /* other times than 0h UT as well.  While this sounds somewhat     */
    /* contradictory, it is very practical:  instead of computing      */
    /* GMST like:                                                      */
    /*                                                                 */
    /*  GMST = (GMST0) + UT * (366.2422/365.2422)                      */
    /*                                                                 */
    /* where (GMST0) is the GMST last time UT was 0 hours, one simply  */
    /* computes:                                                       */
    /*                                                                 */
    /*  GMST = GMST0 + UT                                              */
    /*                                                                 */
    /* where GMST0 is the GMST "at 0h UT" but at the current moment!   */
    /* Defined in this way, GMST0 will increase with about 4 min a     */
    /* day.  It also happens that GMST0 (in degrees, 1 hr = 15 degr)   */
    /* is equal to the Sun's mean longitude plus/minus 180 degrees!    */
    /* (if we neglect aberration, which amounts to 20 seconds of arc   */
    /* or 1.33 seconds of time)                                        */
    /*                                                                 */
    /*******************************************************************/

    double GMST0( double d )
    {
        double sidtim0;
        /* Sidtime at 0h UT = L (Sun's mean longitude) + 180.0 degr  */
        /* L = M + w, as defined in sunpos().  Since I'm too lazy to */
        /* add these numbers, I'll let the C compiler do it for me.  */
        /* Any decent C compiler will add the constants at compile   */
        /* time, imposing no runtime or code overhead.               */
        sidtim0 = revolution( ( 180.0 + 356.0470 + 282.9404 ) +
                ( 0.9856002585 + 4.70935E-5 ) * d );
        return sidtim0;
    }  /* GMST0 */

    String getDecimalTime(double dt, boolean shortform) {
        String ret;
        int hour = (int)Math.floor(dt);
        double fraction = dt - hour;
        int minute = (int)Math.floor(fraction * 60);

        if (shortform) {
            //sprintf(ret, "%02d:%02d", hour, minute);
            ret = String.format("%02d:%02d", hour, minute);
        } else {
            //sprintf(ret, "%d hours and %d minutes", hour, minute);
            ret = String.format("%d hours and %d minutes", hour, minute);
        }

        return ret;
    }

    static String getDecimalTimeWithCorrection(double dt, boolean shortform, int minutes_correction) {
        //System.out.println("gDTWC " + dt + " " + shortform + " " + minutes_correction);
        String ret;
        dt += (minutes_correction/60.0);

        if(dt>=24){
            dt -= 24;
        }
        else if(dt<0){
            dt += 24;
        }//note: the date is unknown in this function


        int hour = (int)Math.floor(dt);
        double fraction = dt - hour;
        int minute = (int)Math.floor(fraction * 60);

        if (shortform) {
            ret = String.format("%02d:%02d", hour, minute);
        } else {
            ret = String.format("%d hours and %d minutes", hour, minute);
        }

        return ret;
    }

    static String getDecimalTimeWithCorrection(double dt, boolean shortform, int minutes_correction, boolean doRounding) {
        //System.out.println("gDTWC " + dt + " " + shortform + " " + minutes_correction);
        String ret;
        int roundLimit = 1;//a limit for seconds that if reached leads to a rounding up
        dt += (minutes_correction/60.0);

        if(dt>=24){
            dt -= 24;
        }
        else if(dt<0){
            dt += 24;
        }//note: the date is unknown in this function


        int hour = (int)Math.floor(dt);
        double fraction = dt - hour;
        int minute = 0;//(int)Math.floor(fraction * 60);
        IntWrapperClass minu = new IntWrapperClass();

        double frac = minute_from_fraction_double(fraction, minu);

        minute = minu.value;

        if(doRounding){
            IntWrapperClass sec = new IntWrapperClass();
            minute_from_fraction_double(frac - minu.value, sec);

            if(sec.value >= roundLimit){
                minute++;
            }
        }

        if(minute==60){
            hour++;
            minute=0;
        }

        if (shortform) {
            ret = String.format("%02d:%02d", hour, minute);
        } else {
            ret = String.format("%d hours and %d minutes", hour, minute);
        }

        return ret;
    }

    static String getDecimalTimeWithCorrectionH(double dt, boolean shortform, int hours_correction) {
        return getDecimalTimeWithCorrection(dt, shortform, hours_correction*60);
    }

    static String formatRiseTime(double dt, boolean shortform, int hours_correction, int status) {
        String ret = "formatRiseTime";
        //System.out.println(ret + ", " + dt + ", " + shortform + ", " + hours_correction);

        switch(status) {
            case 0:
                ret = getDecimalTimeWithCorrectionH(dt, shortform, hours_correction);
                break;
            case 1:
                ret = "Never gets that dark";
                break;
            case -1:
                ret = "Never gets that bright";
                break;
        }
        return ret;
    }



    static String formatRiseTimeM(double dt, boolean shortform, int minutes_correction, int status) {
        String ret = "formatRiseTime";
        //System.out.println(ret + ", " + dt + ", " + shortform + ", " + hours_correction);

        switch(status) {
            case 0:
                ret = getDecimalTimeWithCorrection(dt, shortform, minutes_correction);
                break;
            case 1:
                ret = "Never gets that dark";
                break;
            case -1:
                ret = "Never gets that bright";
                break;
        }
        return ret;
    }

    static String formatRiseTimeMRounding(double dt, boolean shortform, int minutes_correction, int status, boolean useRounding) {
        String ret = "formatRiseTime";
        //System.out.println(ret + ", " + dt + ", " + shortform + ", " + hours_correction);

        switch(status) {
            case 0:
                if(useRounding){
                    ret = getDecimalTimeWithCorrection(dt, shortform, minutes_correction, true);
                }
                else{
                    ret = getDecimalTimeWithCorrection(dt, shortform, minutes_correction);
                }

                break;
            case 1:
                ret = "Never gets that dark";
                break;
            case -1:
                ret = "Never gets that bright";
                break;
        }
        return ret;
    }

    double julian_day(long unixTimeS) {
        System.out.println("julian_day");
        // Extract UTC Time
        Date d = new Date();

        //struct tm* tm = gmtime(&utc_time_point);
        double year = this.year;//tm->tm_year + 1900;
        double month = this.month;//tm->tm_mon + 1;
        double day = this.day;//tm->tm_mday;
        double hour = d.getHours();//tm->tm_hour;
        double min = d.getMinutes();
        double sec = d.getSeconds();
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        //System.out.printf("year,month,day,hour,min,sec %.1f %.1f %.1f %.1f %.1f %.1f\n", year,month,day,hour,min,sec);
        double jd = Math.floor(365.25*(year + 4716.0)) + Math.floor(30.6001*(month + 1.0)) + 2.0 -
                Math.floor(year / 100.0) + Math.floor(Math.floor(year / 100.0) / 4.0) + day - 1524.5 +
                (hour + min / 60 + sec / 3600) / 24;
        //System.out.println("jd = " + jd);
        return jd;
    }

    double julianDay(ZonedDateTime time) {
        //System.out.println("julianDay");
        // Extract UTC Time
        //Date d = new Date();

        //struct tm* tm = gmtime(&utc_time_point);
        double year = this.year;//tm->tm_year + 1900;
        double month = this.month;//tm->tm_mon + 1;
        double day = this.day;//tm->tm_mday;
        double hour = time.getHour();//d.getHours();//tm->tm_hour;
        double min = time.getMinute();
        double sec = time.getSecond();
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        //System.out.printf("year,month,day,hour,min,sec %.1f %.1f %.1f %.1f %.1f %.1f\n", year,month,day,hour,min,sec);
        double jd = Math.floor(365.25*(year + 4716.0)) + Math.floor(30.6001*(month + 1.0)) + 2.0 -
                Math.floor(year / 100.0) + Math.floor(Math.floor(year / 100.0) / 4.0) + day - 1524.5 +
                (hour + min / 60 + sec / 3600) / 24;
        //System.out.println("jd = " + jd);
        //System.out.println("julianDay END---");
        return jd;
    }

    //time_t utc_time_point, double Lat, double Lon, double Alt, double* Az, double* El
    //unixTimeS, this.lat, this.lon, 0, Az, El
    void SolarAzEl(long unixTimeS, double Alt, DoubleWrapperClass Az, DoubleWrapperClass El) {
        //System.out.println("SolarAzEl");


        //System.out.println(SimpleDateTime.epochToDate(unixTimeS * 1000));
        ZonedDateTime zdt = SimpleDateTime.epochToDate(unixTimeS*1000);
        //ZonedDateTime zdt2 = SimpleDateTime.epochToDateLocal(unixTimeS*1000);

        //System.out.println(SimpleDateTime.epochToDate(UTCVersion * 1000));

        double jd = julianDay(zdt);//julian_day(UTCVersion);
        //System.out.println("jd: " + jd);
        double d = jd - 2451543.5;
        //System.out.println("d: " + d);

        // Keplerian Elements for the Sun(geocentric)
        double w = 282.9404 + 4.70935e-5*d; // (longitude of perihelion degrees)
        // a = 1.000000; % (mean distance, a.u.)
        double e = 0.016709 - 1.151e-9*d; // (eccentricity)
        //fmod
        double M = (356.0470 + 0.9856002585*d) % 360.0; // (mean anomaly degrees)

        //System.out.printf("%f %f %f %f %f\n", jd, d, w, e, M);

        double L = w + M; // (Sun's mean longitude degrees)
        double oblecl = 23.4393 - 3.563e-7*d; // (Sun's obliquity of the ecliptic)
        // auxiliary angle
        double  E = M + (180 / Math.PI)*e*Math.sin(M*(Math.PI / 180))*(1 + e*Math.cos(M*(Math.PI / 180)));
        // rectangular coordinates in the plane of the ecliptic(x axis toward perhilion)
        double x = Math.cos(E*(Math.PI / 180)) - e;
        double y = Math.sin(E*(Math.PI / 180))*Math.sqrt(1 - Math.pow(e, 2));
        // find the distance and true anomaly
        double r = Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
        double v = Math.atan2(y, x)*(180 / Math.PI);

        //System.out.printf("%f %f %f %f %f %f %f \n", L,oblecl,E,x,y,r,v);

        // find the longitude of the sun
        double lon_ = v + w;
        // compute the ecliptic rectangular coordinates
        double xeclip = r*Math.cos(lon_*(Math.PI / 180));
        //System.out.println("lon_: " + lon_);
        //System.out.println("---" + Math.cos(lon_*(Math.PI / 180)));
        //System.out.println(r+"*Math.cos("+lon_+"*(Math.PI / 180))");
        //System.out.printf("xeclip : %f\n", xeclip);
        double yeclip = r*Math.sin(lon_*(Math.PI / 180));
        double zeclip = 0.0;
        //rotate these coordinates to equitorial rectangular coordinates
        double xequat = xeclip;

        //System.out.printf("%f %f %f %f %f \n", lon_, xeclip, yeclip, zeclip, xequat);

        double yequat = yeclip*Math.cos(oblecl*(Math.PI / 180)) + zeclip * Math.sin(oblecl*(Math.PI / 180));
        double zequat = yeclip*Math.sin(23.4406*(Math.PI / 180)) + zeclip * Math.cos(oblecl*(Math.PI / 180));
        // convert equatorial rectangular coordinates to RA and Decl:
        r = Math.sqrt(Math.pow(xequat, 2) + Math.pow(yequat, 2) + Math.pow(zequat, 2)) - (Alt / 149598000); //roll up the altitude correction
        double RA = Math.atan2(yequat, xequat)*(180 / Math.PI);
        double delta = Math.asin(zequat / r)*(180 / Math.PI);

        //xeclip och xequat verkar vara samma

        //System.out.printf("%f %f %f %f %f\n", yequat,zequat,r,RA,delta);

        // Following the RA DEC to Az Alt conversion sequence explained here :
        // http ://www.stargazing.net/kepler/altaz.html
        //	Find the J2000 value
        //	J2000 = jd - 2451545.0;
        //hourvec = datevec(UTC);
        //UTH = hourvec(:, 4) + hourvec(:, 5) / 60 + hourvec(:, 6) / 3600;
        // Get UTC representation of time / C++ Specific
        //struct tm *ptm;
        //ptm = gmtime(&utc_time_point);

        //System.out.printf("UTH? %f + %f + %f\n", (double)date.getHours(), (double)date.getMinutes() / 60, (double)date.getSeconds() / 3600);
        //System.out.printf("UTH? %f + %f + %f\n", (double)zdt.getHour(), (double)zdt.getMinute()/60, (double)zdt.getSecond()/3600);


        double UTH = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            UTH = (double)zdt.getHour() + (double)zdt.getMinute()/60 + (double)zdt.getSecond()/3600;
        }
        //(double)date.getHours() + (double)date.getMinutes() / 60 + (double)date.getSeconds() / 3600;

        //System.out.printf("UTH: %f\n", UTH);
        // Calculate local siderial time
        //fmod
        double GMST0 = ((L + 180) % 360.0) / 15;

        //System.out.println("GMSTO: " + GMST0);

        double SIDTIME = GMST0 + UTH + this.lon / 15;

        //System.out.println("SIDTIME: " + SIDTIME);

        // Replace RA with hour angle HA
        double HA = (SIDTIME*15 - RA);
        // convert to rectangular coordinate system
        x = Math.cos(HA*(Math.PI / 180))*Math.cos(delta*(Math.PI / 180));
        y = Math.sin(HA*(Math.PI / 180))*Math.cos(delta*(Math.PI / 180));
        double z = Math.sin(delta*(Math.PI / 180));
        // rotate this along an axis going east - west.
        double xhor = x * Math.cos((90 - this.lat) * (Math.PI / 180)) - z * Math.sin((90 - this.lat) * (Math.PI / 180));
        double yhor = y;
        double zhor = x * Math.sin((90 - this.lat) * (Math.PI / 180)) + z * Math.cos((90 - this.lat)*(Math.PI / 180));

        // Find the h and AZ
        Az.value = Math.atan2(yhor, xhor)*(180 / Math.PI) + 180;
        El.value = Math.asin(zhor)*(180 / Math.PI);
    }



    double getElevationAtNoon(int correctionHours, double noon, int correctionMinutes){
        //Double Az, El;
        DoubleWrapperClass Az, El;
        Az = new DoubleWrapperClass();
        El = new DoubleWrapperClass();

        //double noon_ = (this.rise+this.set)/2;

        //System.out.println("noon = " + noon);
        //int localNoon = (int)noon + correctionHours;
        int localNoon = (int)noon + correctionMinutes/60;

        //System.out.println("localNoon: " + localNoon);

        long unixTimeS = System.currentTimeMillis() / 1000;


        LocalDateTime ldti = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ldti = LocalDateTime.of(year, month, day, (int)noon, minute_from_fraction(noon - (int)noon));
        }
        //ZoneId zid = ZoneId.systemDefault();
        //epoch - noon date-time
        long epoch = 0;//atZone(zid).toEpochSecond();//unix timestamp
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            epoch = ldti.toEpochSecond(ZoneOffset.UTC);
        }

        //System.out.println("unixTimeS: " + unixTimeS);
        Date da = dateFromUnixTime(unixTimeS);

        //System.out.println("epoch " + epoch);

        SimpleDateTime sdt = new SimpleDateTime(da);
        sdt.hour = localNoon;
        sdt.minute = minute_from_fraction(noon - (int)noon);
        sdt.second = 0;

        //System.out.println(sdt);

        //long newUnixT = sdt.getUnixTimeSeconds();

        //System.out.println(newUnixT);

        //TODO: use correctionHours?

        //System.out.println("Will call SolarAzEl with " + epoch + ", 0, Az El");

        SolarAzEl(epoch, 0, Az, El);

        //System.out.println("Az El now " + Az.value + " " + El.value);
        return El.value;
    }

    /**
     *
     * @param uTimeStampSec - unix timestamp in seconds
     */
    static void printUnixTime(long uTimeStampSec) {

        Date time = new Date((long)uTimeStampSec*1000);

        System.out.println((time.getYear()+1900) + "-" + (time.getMonth()+1) + "-" + time.getDate() + " " + time.getHours() + ":" + time.getMinutes() + ":" + time.getSeconds());
    }

    static Date dateFromUnixTime(long uTimeStampSec) {

        Date time = new Date((long)uTimeStampSec*1000);

        return time;
    }


    /**
     *
     * @param fraction - part of an hour
     * @return
     */
    static int minute_from_fraction(double fraction){
        if(fraction>=1){
            return 0;
        }
        return (int)(fraction * 60);
    }

    //Added 241030
    static double minute_from_fraction_double(double fraction, IntWrapperClass minute){
        //printf("minute_from_fraction_double %f\n", fraction);
        if(fraction>=1){
            System.out.println("error: fraction not below 1\n");
            return 0;
        }
        minute.value = (int)(fraction * 60);
        return fraction * 60;
    }

    void stepOneDay(){
        //this.day++;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.timeInfo = timeInfo.plusDays(1);

            this.year = this.timeInfo.getYear();
            this.month = this.timeInfo.getMonthValue();
            this.day = this.timeInfo.getDayOfMonth();
        }
    }
}
