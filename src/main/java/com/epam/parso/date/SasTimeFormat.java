package com.epam.parso.date;

/**
 * Collection of SAS time formats.
 */
public enum SasTimeFormat {
    /**
     * Writes time values as hours, minutes, and seconds in the form hh:mm:ss.ss.
     * See: https://v8doc.sas.com/sashtml/lgref/z0197928.htm
     */
    TIME,
    /**
     * Writes time values as the number of minutes and seconds since midnight.
     * See: https://v8doc.sas.com/sashtml/lgref/z0198053.htm
     */
    MMSS,
    /**
     * Writes time values as hours and minutes in the form hh:mm.
     * See: https://v8doc.sas.com/sashtml/lgref/z0198049.htm
     */
    HHMM,
    /**
     * Writes time values as hours and decimal fractions of hours.
     * See: https://v8doc.sas.com/sashtml/lgref/z0198051.htm
     */
    HOUR,
    /**
     * Writes time values as hours, minutes, and seconds in the form hh:mm:ss.ss with AM or PM.
     * See: https://v8doc.sas.com/sashtml/lgref/z0201272.htm
     */
    TIMEAMPM,
    /**
     * Writes time values as local time, appending the Coordinated Universal Time (UTC) offset
     * for the local SAS session, using the ISO 8601 extended time notation hh:mm:ss+|–hh:mm.
     */
    E8601LZ,
    /**
     * Writes time values by using the ISO 8601 extended notation hh:mm:ss.ffffff.
     */
    E8601TM
}
