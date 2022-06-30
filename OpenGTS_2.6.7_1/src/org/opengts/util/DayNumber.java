// ----------------------------------------------------------------------------
// Copyright 2007-2020, GeoTelematic Solutions, Inc.
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Description:
//  This class provides "Day Number" utilities
// ----------------------------------------------------------------------------
// Change History:
//  2010/01/29  Martin D. Flynn
//     -Initial release
//  2011/08/21  Martin D. Flynn
//     -Added "getDateTime", "getDayStart", "getDayEnd" methods
//  2020/02/19  GTS Development Team
//     -Added "getMonthDelta"
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.util.*;
import java.io.*;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

/**
*** Performs convenience function on a "Day Number"
*** Number of days since October 15, 1582 (the first day of the Gregorian Calendar)
**/

public class DayNumber
    implements Comparable<Object>, Cloneable
{

    // ------------------------------------------------------------------------

    public static final String   DATE_FORMAT_YMD_1          = "yyyy/MM/dd";
    public static final String   DATE_FORMAT_YMD_2          = "yyyy-MM-dd";

    public static final String   DEFAULT_DATE_FORMAT        = DATE_FORMAT_YMD_1;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Convert specified string to DayNumber.  Valid formats are "yyyy/mm/dd" or "yyyy-mm-dd".
    *** @param ymdStr  The date to parse (range year 1583 to 2200)
    *** @return the parsed DayNumber, or null if the specified format is invalid.
    **/
    public static DayNumber parseDayNumber(String ymdStr)
    {

        /* invalid ymd? */
        if (StringTools.isBlank(ymdStr)) {
            // -- null/empty string
            return null;
        }

        /* find delimiter (format: "yyyy/mm/dd" or "yyyy-mm-dd") */
        char delim;
        if (ymdStr.indexOf("/") >= 0) {
            delim = '/';
        } else
        if (ymdStr.indexOf("-") >= 0) {
            delim = '-';
        } else {
            // -- invalid delimter
            return null;
        }

        /* split */
        String YMD[] = StringTools.split(ymdStr, delim);
        if (YMD.length != 3) {
            // -- invalid format
            return null;
        }

        /* parse */
        int yy = StringTools.parseInt(YMD[0],0);
        int mm = StringTools.parseInt(YMD[1],0);
        int dd = StringTools.parseInt(YMD[2],0);

        /* validate year */
        if ((yy <= DateTime.GREGORIAN_YEAR) || (yy > 2200)) {
            // -- invalid year
            return null;
        }

        /* validate month/day */
        if ((mm < 1) || (mm > 12)) {
            // -- invalid month
        } else
        if ((dd <= 0) || (dd > 31)) {
            // -- invalid day
            return null;
        } else
        if (dd > DateTime.getDaysInMonth(null, mm, yy)) {
            // -- invalid day
            return null;
        }

        /* return DayNumber instance */
        return new DayNumber(yy, mm, dd);

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private long            dayNumber       = 0L;
    private int             year            = 0;
    private int             month1          = 0;
    private int             dayOfMonth      = 0;

    /**
    *** Constructor
    *** @param dayNumber The number of days since October 15, 1582
    **/
    public DayNumber(long dayNumber)
    {
        this.dayNumber  = dayNumber;
        DateTime.ParsedDateTime pdt = DateTime.getDateFromDayNumber(this.dayNumber);
        this.year       = pdt.year;
        this.month1     = pdt.month1;
        this.dayOfMonth = pdt.day;
    }
    
    /**
    *** Constructor
    *** @param year   The year (>= 1583)
    *** @param month1 The month (1..12)
    *** @param day    The day of the month
    **/
    public DayNumber(int year, int month1, int day)
    {
        this.year       = year;
        this.month1     = month1;
        this.dayOfMonth = day;
        this.dayNumber  = DateTime.getDayNumberFromDate(year,month1,day);
    }

    /**
    *** Copy Constructor
    *** @param other The other DayNumber from which to copy
    **/
    public DayNumber(DayNumber other)
    {
        this.dayNumber  = (other != null)? other.dayNumber  : 0L;
        this.year       = (other != null)? other.year       : 0;
        this.month1     = (other != null)? other.month1     : 0;
        this.dayOfMonth = (other != null)? other.dayOfMonth : 0;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this DayNumber is valid
    *** @return True if this DayNumber is valid
    **/
    public boolean isValid()
    {
        return (this.dayNumber >= 0L)? true : false;
    }

    /**
    *** Gets the Day Number
    *** @return The day number
    **/
    public long getDayNumber()
    {
        return this.dayNumber;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Year
    *** @return The Year
    **/
    public int getYear()
    {
        return this.year;
    }

    /**
    *** Gets the Month (1..12)
    *** @return The Month
    **/
    public int getMonth()
    {
        return this.month1;
    }

    /**
    *** Gets the 0-based Month (0..11)
    *** @return The Month
    **/
    public int getMonth0()
    {
        return this.getMonth() - 1;
    }

    /**
    *** Gets the Day of Month (1..31)
    *** @return The Day of Month
    **/
    public int getDayOfMonth()
    {
        return this.dayOfMonth;
    }

    // ------------------------------------------------------------------------
    
    /**
    *** Returns the day of the week
    *** @return The day of the week (0=Sunday, 6=Saturday)
    **/
    public int getDayOfWeek()
    {
        return DateTime.getDayOfWeek(this.getYear(), this.getMonth(), this.getDayOfMonth());
    }

    // ------------------------------------------------------------------------
   
    /** 
    *** Returns true if this year represents a leap-year
    *** @return True if this is a leap-year, false otherwise
    **/
    public boolean isLeapYear()
    {
        return DateTime.isLeapYear(this.getYear());
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns a DateTime instance with the time set to the beginning of the day
    *** @param tmz  The TimeZone
    *** @param hour24  The 24-hour of the day
    *** @param minute  The minute of the hour
    *** @param second  The second of the minute
    *** @return The DateTime instance, or null if DayNumber is invalid
    **/
    public DateTime getDateTime(TimeZone tmz, int hour24, int minute, int second)
    {
        if (this.isValid()) {
            // -- must be valid DayNumber
            DateTime.ParsedDateTime pdt = DateTime.getDateFromDayNumber(this.getDayNumber(), tmz);
            int year   = pdt.getYear();
            int month1 = pdt.getMonth1();
            int day    = pdt.getDayOfMonth();
            return new DateTime(tmz, year, month1, day, hour24, minute, second);
        } else {
            // -- invalid DayNumber
            return null;
        }
    }

    /**
    *** Returns a DateTime instance with the time set to the beginning of the day
    *** @param tmz  The TimeZone
    *** @return The DateTime instance
    **/
    public DateTime getDayStart(TimeZone tmz)
    {
        return this.getDateTime(tmz, 0, 0, 0);
    }

    /**
    *** Returns a DateTime instance with the time set to the beginning of the day
    *** @param tmz  The TimeZone
    *** @return The DateTime instance
    **/
    public DateTime getDayEnd(TimeZone tmz)
    {
        return this.getDateTime(tmz, 23, 59, 59);
    }

    /**
    *** Returns a DateTime instance with the time set to Noon
    *** @param tmz  The TimeZone
    *** @return The DateTime instance
    **/
    public DateTime getDayNoon(TimeZone tmz)
    {
        return this.getDateTime(tmz, 12, 00, 00);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns the DayNumber representing the current DayNumber, plus the number of months offset.
    *** (ie. Same day-of-the-month on the delta month)
    *** @param deltaMo  The delta number of months (may be negative)
    **/
    public DayNumber _getMonthDelta(int deltaMo)
    {
        int YYYY = this.getYear();       // 20##
        int MM0  = this.getMonth0();     // 0..11
        int DD   = this.getDayOfMonth(); // 1..31
        MM0 += deltaMo;
        if (MM0 < 0) {
            // -- in the past
            YYYY -= ((Math.abs(MM0) - 1) / 12) + 1;  // (((|MM0| - 1) / 12) + 1)
            MM0   = 12 - ((Math.abs(MM0) - 1) % 12); // (12 - ((|MM0| - 1) % 12))
        } else
        if (MM0 > 11) {
            // -- in the future
            YYYY += MM0 / 12; // eg. (12/12)==>1
            MM0   = MM0 % 12; // eg. (12%12)==>0
        }
        return new DayNumber(YYYY, MM0+1, DD);
    }

    /**
    *** Returns the DayNumber representing the current DayNumber, plus the number of months offset.
    *** (ie. Same day-of-the-month on the delta month)
    **/
    public long getMonthDelta(int deltaMo)
    {
        return this._getMonthDelta(deltaMo).getDayNumber();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /** 
    *** Retuens a formatted Date
    *** @param fmt  The Date/Time format
    *** @return The formatted date
    **/
    public String format(String fmt)
    {
        String dtFMT = StringTools.blankDefault(fmt,DEFAULT_DATE_FORMAT);
        if (this.getDayNumber() <= 0L) {
            String zz = dtFMT;
            zz = zz.replace('y','0');
            zz = zz.replace('M','0');
            zz = zz.replace('d','0');
            return zz;
        } else {
            int year = this.getYear();
            int mon0 = this.getMonth() - 1;
            int day  = this.getDayOfMonth();
            GregorianCalendar gc = new GregorianCalendar(year, mon0, day);
            return DateTime.format(gc.getTime(), null, dtFMT);
        }
    }

    /**
    *** Returns a String representation of this instance
    *** @return A String representation of this instance
    **/
    public String toString()
    {
        return this.format(DEFAULT_DATE_FORMAT);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified DayNumber is <b>equal-to</b> this DayNumber instance
    *** @param obj the other DayNumber instance
    *** @return True if the specified DayNumber is <b>equal-to</b> this DayNumber instance
    **/
    public boolean equals(Object obj) 
    {
        if (obj instanceof DayNumber) {
            return (this.getDayNumber() == ((DayNumber)obj).getDayNumber());
        } else {
            return false;
        }
    }

    /**
    *** Returns a hash code value for the object. 
    **/
    public int hashCode()
    {
        return super.hashCode();
    }

    /**
    *** Compares another DayNumber instance to this instance.
    *** @param other  The other DayNumber instance.
    *** @return &lt;0 of the other DayNumber instance is before this instance, 0 if the other DayNumber
    ***         instance is equal to this instance, and &gt;0 if the other DayNumber instance is
    ***         after this instance.
    **/
    public int compareTo(Object other)
    {
        if (other instanceof DayNumber) {
            long otherDN = ((DayNumber)other).getDayNumber();
            long thisDN  = this.getDayNumber();
            if (thisDN < otherDN) { return -1; }
            if (thisDN > otherDN) { return  1; }
            return 0;
        } else {
            return -1;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns a clone of this DateTime instance
    *** @return A clone of this DateTime instance
    **/
    public Object clone()
    {
        return new DayNumber(this);
    }

    // ------------------------------------------------------------------------

}
