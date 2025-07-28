package com.example.myprayertimes;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

public class PrayerTime {

    public static enum TimeName{
        fajr, riseSet, zhuhr, _3$r, _3shaa, fajr2, riseSet2
    }
    String location = "";
    int year = 2024;
    int month = 6;
    int day = 1;

    double lat,lon;
    boolean isAM; //is before noon
    double time1, time2;
    String name;
    //int hourCorrection;
    int minuteCorrection; //replacing hourCorrection to cover for quarter/half-hour time zones
    int status;
    /**
     * this var should enforce rounding up minute-wise on prayer times, BUT only in formatting / output, not in data
     */
    boolean useRounding;

    boolean useBottomLimb=false;

    double rise, set;

    public PrayerTime() {
        super();
    }

    public PrayerTime(boolean isAM_) {
        super();
        this.isAM = isAM_;
    }



    public PrayerTime(String name_, boolean isAM_, int minuteCorrection_, int status_, boolean useRounding) {
        super();
        this.name = name_;
        this.isAM = isAM_;
        this.minuteCorrection = minuteCorrection_;
        this.status = status_;
        this.useRounding = useRounding;
    }

    //var x added to make differance from previous constructor, no use...

    /***
     *
     * @param name_
     * @param isAM_
     * @param minuteCorrection_ - made short to differ from other constructor
     * @param status_
     */
    public PrayerTime(String name_, boolean isAM_, short minuteCorrection_, int status_, int year_, int month_, int day_, double lat_, double lon_, boolean useBottomLimb_) {
        super();
        this.name = name_;
        this.isAM = isAM_;
        this.minuteCorrection = minuteCorrection_;
        this.status = status_;
        this.useRounding = false;

        this.year = year_;
        this.month = month_;
        this.day = day_;

        this.lat = lat_;
        this.lon = lon_;

        this.useBottomLimb = useBottomLimb_;
    }

    public PrayerTime(String name_, boolean isAM_, int minuteCorrection_, int status_, boolean useRounding, int year_, int month_, int day_, double lat_, double lon_, boolean useBottomLimb_) {
        super();
        this.name = name_;
        this.isAM = isAM_;
        this.minuteCorrection = minuteCorrection_;
        this.status = status_;
        this.useRounding = useRounding;

        this.year = year_;
        this.month = month_;
        this.day = day_;

        this.lat = lat_;
        this.lon = lon_;

        this.useBottomLimb = useBottomLimb_;
    }

    public PrayerTime(String name_, boolean isAM_, int minuteCorrection_, boolean useRounding, int year_, int month_, int day_, double lat_, double lon_, boolean useBottomLimb_) {
        super();
        this.name = name_;
        this.isAM = isAM_;
        this.minuteCorrection = minuteCorrection_;
        this.useRounding = useRounding;

        this.year = year_;
        this.month = month_;
        this.day = day_;

        this.lat = lat_;
        this.lon = lon_;

        this.useBottomLimb = useBottomLimb_;
    }

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
        double d = PTCalc.days_since_2000_Jan_0(this.year, this.month, this.day) + 0.5 - this.lon / 360.0;

        //System.out.println("d: " + d);
        // Local sidereal time
        double sidtime = revolution(GMST0(d) + 180.0 + this.lon);

        // Compute Sun's RA, Decl and distance at this moment //537 in code
        sunRADec(d, sRA, sdec, sr);


        /* Compute time when Sun is at south - in hours UT */
        tsouth = 12.0 - PTCalc.rev180(sidtime - sRA.value) / 15.0;

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

        cost = (PTCalc.sind(altit) - PTCalc.sind(lat) * PTCalc.sind(sdec.value)) / (PTCalc.cosd(lat) * PTCalc.cosd(sdec.value));
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
            t = PTCalc.acosd(cost) / 15.0; /* The diurnal arc, hours */
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

    public String toString() {
        String timeStr;
        double time = isAM ? time1 : time2;

        timeStr = PTCalc.formatRiseTimeMRounding(time, true, minuteCorrection, status, useRounding);
        String ret = this.name + ", " + timeStr;

        return ret;//"PrayerTime [isAM=" + isAM + ", time1=" + time1 + ", time2=" + time2 + "]";
    }

    double revolution( double x )
    /*****************************************/
        /* Reduce angle to within 0..360 degrees */
    /*****************************************/
    {
        return( x - 360.0 * Math.floor( x * PTCalc.INV360 ) );
    }  /* revolution */

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
        x = r.value * PTCalc.cosd(sLon.value);
        y = r.value * PTCalc.sind(sLon.value);


        /* Compute obliquity of ecliptic (inclination of Earth's axis) */
        obl_ecl = 23.4393 - 3.563E-7 * d;


        /* Convert to equatorial rectangular coordinates - x is unchanged */
        z = y * PTCalc.sind(obl_ecl);
        y = y * PTCalc.cosd(obl_ecl);


        /* Convert to spherical coordinates */
        RA.value = PTCalc.atan2d(y, x);
        dec.value = PTCalc.atan2d(z, Math.sqrt(x*x + y*y));
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
        E = M + e * PTCalc.RADEG * PTCalc.sind(M) * ( 1.0 + e * PTCalc.cosd(M) );
        // x,y coordinates in orbit
        double x = PTCalc.cosd(E) - e;
        double y = Math.sqrt( 1.0 - e*e ) * PTCalc.sind(E);

        //making effect
        //this.solarDistance = Math.sqrt(x*x + y*y);
        sunDist.value = Math.sqrt(x*x + y*y);

        /* True anomaly */
        double v = PTCalc.atan2d( y, x );
        /* True solar longitude */
        //this.solarLongitude = v + w;
        sunLon.value = v + w;

        if(sunLon.value >= 360.0) {
            sunLon.value -= 360.0;
        }
    }

    double getElevationAtNoon(double noon, int correctionMinutes){
        //Double Az, El;
        DoubleWrapperClass Az, El;
        Az = new DoubleWrapperClass();
        El = new DoubleWrapperClass();

        //double noon_ = (this.rise+this.set)/2;

        //System.out.println("noon = " + noon);
        //TODO: verify this works with a city with fraction-hour-timezone
        int localNoon = (int)noon + correctionMinutes/60;

        //System.out.println("localNoon: " + localNoon);

        long unixTimeS = System.currentTimeMillis() / 1000;


        LocalDateTime ldti = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ldti = LocalDateTime.of(year, month, day, (int)noon, PTCalc.minute_from_fraction(noon - (int)noon));
        }
        //ZoneId zid = ZoneId.systemDefault();
        //epoch - noon date-time
        long epoch = 0;//atZone(zid).toEpochSecond();//unix timestamp
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            epoch = ldti.toEpochSecond(ZoneOffset.UTC);
        }

        //System.out.println("unixTimeS: " + unixTimeS);
        Date da = PTCalc.dateFromUnixTime(unixTimeS);

        //System.out.println("epoch " + epoch);

        SimpleDateTime sdt = new SimpleDateTime(da);
        sdt.hour = localNoon;
        sdt.minute = PTCalc.minute_from_fraction(noon - (int)noon);
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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

    public void calculate(TimeName timeName){

        switch (timeName){
            case fajr:
                status = astronomicalTwilight();
                time1 = rise;
                time2 = set;
                break;
            case riseSet:
                status = sunRiset();
                time1 = this.rise;
                time2 = this.set;
                break;
            case zhuhr:
                status = sunRiset();//test
                time1 = rise;
                time2 = set;
                time2 = (this.rise + this.set)/2 +
                        ((double)PTCalc.zhuhrAfterZenitMinutes/60);//to apply a f rule that a decrease of shadow from zenith is needed

                break;
            case _3$r://TODO: fixa
                status = sunRiset();//to set rise / set
                double noo = (this.rise+this.set)/2 +
                        ((double)PTCalc.zhuhrAfterZenitMinutes/60); //added row 250103
                double elevationNoon = getElevationAtNoon(noo, this.minuteCorrection);
                //System.out.println("elevationNoon: " + elevationNoon);

                //asr calculation, shadow lenghts
                double factor = 1/PTCalc.cosd(90-elevationNoon);//sin has 0 degr upwards, elevation has 0 degrees at the horizon
                //printf("factor: %f\n", factor);
                double shadow = PTCalc.sind(90-elevationNoon) * factor;
                //printf("shadow: %f\n", shadow);

                //double shadow_asr = shadow*2;//wrong
                double shadow_asr = 1+shadow;

                //printf("shadow asr: %f\n", shadow_asr);

                //double hypot_asr = sqrt(1+(shadow*2)*(shadow*2));
                double hypot_asr = Math.sqrt(1+(shadow_asr*shadow_asr));
                //printf("hypotenusan asr: %f\n", hypot_asr);

                //sine of x equals the opposite over the hypotenus => 1/hypot_asr

                double arc_sine_asr = PTCalc.asind(1/hypot_asr);
                //printf("arc_sine_asr: %f\n", arc_sine_asr);

                status = sunRiset_( arc_sine_asr, false);
                time1 = this.rise;
                time2 = this.set;
                break;
            case _3shaa:
                status = ishaaTwilight();
                time1 = this.rise;
                time2 = this.set;
                break;
            case riseSet2:
                useBottomLimb = true;
                status = sunRiset2();
                time1 = this.rise;
                time2 = this.set;
                useBottomLimb = false;
        }
    }

}
