package com.epam.parso.impl;

import static com.epam.parso.impl.SasFileConstants.SECONDS_IN_DAY;

/**
 * SAS supports wide family of date formats.
 * It is reasonable to keep all SAS date related features separately.
 * See more about SAS dates:
 * - https://v8doc.sas.com/sashtml/lrcon/zenid-63.htm
 * - https://v8doc.sas.com/sashtml/lgref/z0197923.htm
 * - https://v8doc.sas.com/sashtml/ets/chap2/sect7.htm
 */
final class SasDateFormat {
    /**
     * Private constructor for utility class.
     */
    private SasDateFormat() {
    }

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
    public static double sasLeapDaysFix(double sasSeconds) {
        if (sasSeconds >= SAS_SECONDS_29FEB4000) {
            if (sasSeconds >= SAS_SECONDS_29FEB8000) {
                sasSeconds += SECONDS_IN_DAY; //restore Y8K leap day
            }
            sasSeconds += SECONDS_IN_DAY; //restore Y4K leap day
        }
        return sasSeconds;
    }
}
