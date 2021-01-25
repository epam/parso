package com.epam.parso.date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static com.epam.parso.date.OutputDateType.SAS_VALUE;


/**
 * SAS supports wide family of date formats.
 * This class allows to represent SAS date in various types.
 * <p>
 * This class is not thread-safe and it should be synchronised externally.
 * Actually, it is not a problem for the the Parso itself as it is a single-threaded
 * library, where each instance of SasFileParser has it's own instance of the formatter.
 */
public class SasTemporalFormatter {

    /**
     * Seconds in day: 86400.
     */
    private static final int SECONDS_IN_DAY = 60 * 60 * 24;

    /**
     * The difference in days between 01/01/1960 (the dates starting point in SAS)
     * and 01/01/1970 (the dates starting point in Java).
     */
    private static final int SAS_VS_EPOCH_DIFF_DAYS = 365 * 10 + 3;

    /**
     * The difference in seconds between 01/01/1960 (the dates starting point in SAS)
     * and 01/01/1970 (the dates starting point in Java).
     */
    private static final int SAS_VS_EPOCH_DIFF_SECONDS = SECONDS_IN_DAY * SAS_VS_EPOCH_DIFF_DAYS;

    /**
     * Milliseconds in one second.
     */
    private static final int MILLIS_IN_SECOND = 1000;

    /**
     * First time when a leap day is removed from the SAS calendar.
     * In seconds since 1960-01-01
     */
    private static final double SAS_SECONDS_29FEB4000 = 64381305600D;

    /**
     * Second time when a leap day is removed from the SAS calendar.
     * In seconds since 1960-01-01
     */
    private static final double SAS_SECONDS_29FEB8000 = 190609027200D;

    /**
     * SAS removes leap day every 4000 year.
     * It removes these days:
     * - 29FEB4000
     * - 29FEB8000
     * This guy proposed such approach many years ago: https://en.wikipedia.org/wiki/John_Herschel
     * <p>
     * Sometimes people discussed why SAS dates are so strange:
     * - https://blogs.sas.com/content/sasdummy/2010/04/05/in-the-year-9999/
     * - https://communities.sas.com/t5/SAS-Programming/Leap-Years-divisible-by-4000/td-p/663467
     * <p>
     * See the SAS program and its output:
     * ```shell
     * data test;
     * dtime = '28FEB4000:00:00:00'dt;
     * put dtime; *out: 64381219200
     * <p>
     * dtime = '29FEB4000:00:00:00'dt;
     * put dtime; *err: ERROR: Invalid date/time/datetime constant '29FEB4000:00:00:00'dt.
     * <p>
     * dtime = '01MAR4000:00:00:00'dt;
     * put dtime; *out: 64381305600
     * <p>
     * dtime = '31DEC4000:00:00:00'dt;
     * put dtime; *out: 64407657600
     * <p>
     * dtime = '28FEB8000:00:00:00'dt;
     * put dtime; *out: 190608940800
     * <p>
     * dtime = '29FEB8000:00:00:00'dt;
     * put dtime; *err: ERROR: Invalid date/time/datetime constant '29FEB8000:00:00:00'dt.
     * <p>
     * dtime = '01MAR8000:00:00:00'dt;
     * put dtime; * out: 190609027200
     * <p>
     * dtime = '31DEC8000:00:00:00'dt;
     * put dtime; *out: 190635379200
     * <p>
     * dtime = '31DEC9999:00:00:00'dt;
     * put dtime; *out: 253717660800
     * run;
     * ```
     * As you can see SAS doesn't accept leap days for 4000 and 8000 years
     * and removes these days at all from the SAS calendar.
     * <p>
     * At the same time these leap days are ok for:
     * - Java: `LocalDateTime.of(4000, 2, 29, 0, 0).toEpochSecond(ZoneOffset.UTC)`
     * outputs 64065686400
     * - JavaScript: `Date.parse('4000-02-29')`
     * outputs 64065686400000
     * - GNU/date: `date --utc --date '4000-02-29' +%s`
     * outputs 64065686400
     * and so on.
     * <p>
     * So, in order to parse SAS dates correctly,
     * we need to restore removed leap days
     *
     * @param sasSeconds SAS date representation in seconds since 1960-01-01
     * @return seconds with restored leap days
     */
    private static double sasLeapDaysFix(double sasSeconds) {
        if (sasSeconds >= SAS_SECONDS_29FEB4000) {
            if (sasSeconds >= SAS_SECONDS_29FEB8000) {
                sasSeconds += SECONDS_IN_DAY; //restore Y8K leap day
            }
            sasSeconds += SECONDS_IN_DAY; //restore Y4K leap day
        }
        return sasSeconds;
    }

    /**
     * Format SAS seconds explicitly into the java Date.
     *
     * @param sasSeconds seconds since 1960-01-01
     * @return date
     */
    public Date formatSasSecondsAsJavaDate(double sasSeconds) {
        double epochSeconds = sasLeapDaysFix(sasSeconds) - SAS_VS_EPOCH_DIFF_SECONDS;
        return new Date((long) (epochSeconds * MILLIS_IN_SECOND));
    }

    /**
     * Format SAS date in SAS days to one of the specified form.
     *
     * @param sasDays        days since 1960-01-01
     * @param dateFormatType type of output date
     * @param sasFormatName  date column format name
     * @param width          date column format width
     * @param precision      date column format precision
     * @return date representation
     */
    public Object formatSasDate(Double sasDays, OutputDateType dateFormatType,
                                String sasFormatName, int width, int precision) {

        if (dateFormatType == SAS_VALUE) {
            return sasDays;
        } else if (sasDays == null || Double.isNaN(sasDays)) {
            return null;
        }

        double epochSeconds = sasLeapDaysFix(sasDays * SECONDS_IN_DAY) - SAS_VS_EPOCH_DIFF_SECONDS;

        switch (dateFormatType) {
            case EPOCH_SECONDS:
                return epochSeconds;
            case JAVA_TEMPORAL:
                return LocalDate.ofEpochDay((long) (epochSeconds / SECONDS_IN_DAY));
            case JAVA_DATE_LEGACY:
            default:
                return new Date((long) (epochSeconds * MILLIS_IN_SECOND));
        }
    }

    /**
     * Format SAS time in SAS seconds to one of the specified form.
     * For the compatibility with Parso this formatter returns number
     * (long or double) instead of date case of JAVA_DATE output type.
     *
     * @param sasSeconds     days since 1960-01-01
     * @param dateFormatType type of output date
     * @param sasFormatName  date column format name
     * @param width          date column format width
     * @param precision      date column format precision
     * @return date representation
     */
    public Object formatSasTime(Double sasSeconds, OutputDateType dateFormatType,
                                String sasFormatName, int width, int precision) {
        if (dateFormatType == SAS_VALUE) {
            return sasSeconds;
        } else if (sasSeconds == null || Double.isNaN(sasSeconds)) {
            return null;
        }

        switch (dateFormatType) {
            case JAVA_DATE_LEGACY:
            case JAVA_TEMPORAL:
            default:
                // These lines below for compatibility with existing Parso result.
                // Number of seconds in Parso is represented in some cases as long
                // or as double using the SasFileParser.convertByteArrayToNumber function.
                long longSeconds = Math.round(sasSeconds);
                if (Math.abs(sasSeconds - longSeconds) > 0) {
                    return sasSeconds;
                } else {
                    return longSeconds;
                }
        }
    }

    /**
     * Format SAS date-time in SAS seconds to one of the specified form.
     *
     * @param sasSeconds     seconds since midnight
     * @param dateFormatType type of output date
     * @param sasFormatName  date column format name
     * @param width          date column format width
     * @param precision      date column format precision
     * @return date representation
     */
    public Object formatSasDateTime(Double sasSeconds, OutputDateType dateFormatType,
                                    String sasFormatName, int width, int precision) {
        if (dateFormatType == SAS_VALUE) {
            return sasSeconds;
        } else if (sasSeconds == null || Double.isNaN(sasSeconds)) {
            return null;
        }

        double epochSeconds = sasLeapDaysFix(sasSeconds) - SAS_VS_EPOCH_DIFF_SECONDS;

        switch (dateFormatType) {
            case EPOCH_SECONDS:
                return epochSeconds;
            case JAVA_TEMPORAL:
                String f = String.format("%.9f", epochSeconds);
                int nanos = Integer.parseInt(f.substring(f.indexOf('.') + 1));
                return LocalDateTime.ofEpochSecond((long) epochSeconds, nanos, ZoneOffset.UTC);
            case JAVA_DATE_LEGACY:
            default:
                return new Date((long) (epochSeconds * MILLIS_IN_SECOND));
        }
    }

    /**
     * Check if the specified SAS format is type of date.
     *
     * @param sasFormatName SAS format name
     * @return true if matched
     */
    public static boolean isDateFormat(String sasFormatName) {
        for (SasDateFormat s : SasDateFormat.values()) {
            if (s.name().equals(sasFormatName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the specified SAS format is type of time.
     *
     * @param sasFormatName SAS format name
     * @return true if matched
     */
    public static boolean isTimeFormat(String sasFormatName) {
        for (SasTimeFormat s : SasTimeFormat.values()) {
            if (s.name().equals(sasFormatName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the specified SAS format is type of date-time.
     *
     * @param sasFormatName SAS format name
     * @return true if matched
     */
    public static boolean isDateTimeFormat(String sasFormatName) {
        for (SasDateTimeFormat s : SasDateTimeFormat.values()) {
            if (s.name().equals(sasFormatName)) {
                return true;
            }
        }
        return false;
    }
}
