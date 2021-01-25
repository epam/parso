package com.epam.parso.date;

/**
 * Collection of SAS datetime formats.
 */
enum SasDateTimeFormat {
    /**
     * Writes datetime values in the form ddmmmyy:hh:mm:ss.ss.
     * See: https://v8doc.sas.com/sashtml/lgref/z0197923.htm
     */
    DATETIME,
    /**
     * Writes dates from datetime values by using the ISO 8601 basic notation yyyymmdd.
     */
    B8601DN,
    /**
     * Writes datetime values by using the ISO 8601 basic notation yyyymmddThhmmss<ffffff>.
     */
    B8601DT,
    /**
     * Adjusts a Coordinated Universal Time (UTC) datetime value to the user local date and time.
     * Then, writes the local date and time by using the ISO 8601 datetime
     * and time zone basic notation yyyymmddThhmmss+hhmm.
     */
    B8601DX,
    /**
     * Reads Coordinated Universal Time (UTC) datetime values that are specified using the
     * ISO 8601 datetime basic notation yyyymmddThhmmss+|–hhmm or yyyymmddThhmmss<ffffff>Z.
     */
    B8601DZ,
    /**
     * Writes datetime values as local time by appending a time zone offset difference between the local time and UTC,
     * using the ISO 8601 basic notation yyyymmddThhmmss+|–hhmm.
     */
    B8601LX,
    /**
     * Writes dates from SAS datetime values by using the ISO 8601 extended notation yyyy-mm-dd.
     */
    E8601DN,
    /**
     * Reads datetime values that are specified using the
     * ISO 8601 extended notation yyyy-mm-ddThh:mm:ss.<ffffff>.
     */
    E8601DT,
    /**
     * Adjusts a Coordinated Universal Time (UTC) datetime value to the user local date and time.
     * Then, writes the local date and time by using the ISO 8601 datetime
     * and time zone extended notation yyyy-mm-ddThh:mm:ss+hh:mm.
     */
    E8601DX,
    /**
     * Reads Coordinated Universal Time (UTC) datetime values that are specified using the ISO 8601
     * datetime extended notation yyyy-mm-ddThh:mm:ss+|–hh:mm.<fffff> or yyyy-mm-ddThh:mm:ss.<fffff>Z.
     */
    E8601DZ,
    /**
     * Writes datetime values as local time by appending a time zone offset difference between the local time and UTC,
     * using the ISO 8601 extended notation yyyy-mm-ddThh:mm:ss+|–hh:mm.
     */
    E8601LX,
    /**
     * Writes datetime values in the form ddmmmyy:hh:mm:ss.ss with AM or PM.
     * See: https://v8doc.sas.com/sashtml/lgref/z0196050.htm
     */
    DATEAMPM,
    /**
     * Expects a datetime value as input and writes date values in the form ddmmmyy or ddmmmyyyy.
     */
    DTDATE,
    /**
     * Writes the date part of a datetime value as the month and year in the form mmmyy or mmmyyyy.
     */
    DTMONYY,
    /**
     * Writes the date part of a SAS datetime value as the day of the week and the date in the form
     * day-of-week, dd month-name yy (or yyyy).
     */
    DTWKDATX,
    /**
     * Writes the date part of a SAS datetime value as the year in the form yy or yyyy.
     */
    DTYEAR,
    /**
     * Writes datetime values in the form mm/dd/yy<yy> hh:mm AM|PM. The year can be either two or four digits.
     */
    MDYAMPM,
    /**
     * Writes the time portion of datetime values in the form hh:mm:ss.ss.
     * See: https://v8doc.sas.com/sashtml/lgref/z0201157.htm
     */
    TOD
}
