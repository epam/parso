package com.epam.parso.date;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import static com.epam.parso.date.SasTemporalConstants.*;
import static java.math.RoundingMode.*;

/**
 * Utility methods for dates.
 */
final class SasTemporalUtils {
    /**
     * Disable creation of utility instances.
     */
    private SasTemporalUtils() {
    }

    /**
     * Repeat character n times to create String.
     *
     * @param character char
     * @param nTimes    number of times
     * @return String
     */
    static String nChars(char character, int nTimes) {
        if (nTimes == 1) {
            return String.valueOf(character);
        }
        if (nTimes <= 30) {
            switch (character) {
                case ' ':
                    return "                              ".substring(0, nTimes);
                case '*':
                    return "******************************".substring(0, nTimes);
                case '0':
                    return "000000000000000000000000000000".substring(0, nTimes);
                case 'S':
                    return "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSS".substring(0, nTimes);
                default:
                    break;
            }
        }
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < nTimes; i++) {
            str.append(character);
        }
        return str.toString();
    }

    /**
     * Convert SAS days to unix epoch seconds.
     *
     * @param sasDays SAS days
     * @return UNIX seconds
     */
    static double sasDaysToEpochSeconds(double sasDays) {
        return sasDays * SECONDS_IN_DAY - SAS_VS_EPOCH_DIFF_SECONDS;
    }

    /**
     * Convert SAS seconds to unix epoch seconds.
     *
     * @param sasSeconds SAS seconds
     * @return UNIX seconds
     */
    static double sasSecondsToEpochSeconds(double sasSeconds) {
        return sasSeconds - SAS_VS_EPOCH_DIFF_SECONDS;
    }

    /**
     * Convert SAS days to Java Date.
     *
     * @param sasDays SAS days
     * @return date
     */
    static Date sasDaysToDate(double sasDays) {
        return new Date((long) (sasDaysToEpochSeconds(sasDays) * MILLIS_IN_SECOND));
    }

    /**
     * Convert SAS seconds to Java date.
     *
     * @param sasSeconds SAS seconds
     * @return date
     */
    static Date sasSecondsToDate(double sasSeconds) {
        return new Date((long) (sasSecondsToEpochSeconds(sasSeconds) * MILLIS_IN_SECOND));
    }

    /**
     * Convert SAS days to Java LocalDate.
     *
     * @param sasDays SAS days
     * @return date
     */
    static LocalDate sasDaysToLocalDate(double sasDays) {
        return LocalDate.ofEpochDay((long) Math.floor(sasDays) - SAS_VS_EPOCH_DIFF_DAYS);
    }

    /**
     * Round SAS seconds. It is not a regular rounding.
     * SAS rounds negative (dates before 1960 year) and positive dates (after 1960 year)
     * in different ways.
     * See examples
     * <p>
     * SAS value        SAS format DATE22.2        SAS format DATE22.3
     * ----------------|------------------------|-----------------------
     * negative dates rounding
     * -395163560.356     24JUN1947:08:20:39.64  24JUN1947:08:20:39.644
     * -394053550.355     07JUL1947:04:40:49.65  07JUL1947:04:40:49.645
     * -392943540.354     20JUL1947:01:00:59.65  20JUL1947:01:00:59.646
     * <p>
     * positive dates rounding
     * 392943540.354     13JUN1972:22:59:00.35   13JUN1972:22:59:00.354
     * 394053550.355     26JUN1972:19:19:10.35   26JUN1972:19:19:10.355
     * 395163560.356     09JUL1972:15:39:20.36   09JUL1972:15:39:20.356
     * <p>
     * rounding near to 24h
     * -11896934400.001  31DEC1582:23:59:59.99   31DEC1582:23:59:59.999
     * 1679183999.999    17MAR2013:23:59:59.99   17MAR2013:23:59:59.999
     * <p>
     * Rounding UP may increase seconds, minutes or hours, but it never
     * increase day. So the biggest rounded time is always less than 24.
     *
     * @param sasSeconds SAS seconds
     * @param precision  column format precision
     * @return rounded seconds with fraction
     */
    static BigDecimal roundSeconds(Double sasSeconds, int precision) {
        BigDecimal seconds = new BigDecimal(sasSeconds)
                .setScale(precision, sasSeconds < 0 ? HALF_DOWN : HALF_UP);

        if (seconds.remainder(BIG_SECONDS_IN_DAY).compareTo(BigDecimal.ZERO) == 0) {
            if (seconds.doubleValue() > sasSeconds) {
                seconds = seconds.subtract(BIG_NANOSECOND_FRACTION).setScale(precision, FLOOR);
            }
        }
        return seconds;
    }


    /**
     * Convert SAS seconds to Java LocalDateTime.
     * Internally it applies SAS-specific rounding for negative dates.
     *
     * @param sasSeconds SAS seconds
     * @param precision  column format precision
     * @return date
     */
    static LocalDateTime sasSecondsToLocalDateTime(double sasSeconds, int precision) {
        BigDecimal bigSeconds = roundSeconds(sasSeconds, precision);
        BigDecimal nanosFraction = bigSeconds.remainder(BigDecimal.ONE);
        if (nanosFraction.compareTo(BigDecimal.ZERO) < 0) {
            nanosFraction = nanosFraction.add(BigDecimal.ONE);
        }
        return LocalDateTime.ofEpochSecond(
                bigSeconds.setScale(0, FLOOR).longValue() - SAS_VS_EPOCH_DIFF_SECONDS,
                nanosFraction.multiply(BIG_NANOS_IN_SECOND).intValue(),
                ZoneOffset.UTC);
    }

    /**
     * Create DateTimeFormatter instance based on pattern in UTC timezone for US locale.
     *
     * @param datePattern date pattern
     * @return formatter
     */
    static DateTimeFormatter createDateTimeFormatterFromPattern(String datePattern) {
        return DateTimeFormatter.ofPattern(datePattern)
                .withZone(ZoneOffset.UTC)
                .withLocale(Locale.US);
    }

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
    static double sasLeapSecondsFix(double sasSeconds) {
        if (sasSeconds >= SAS_SECONDS_29FEB4000) {
            if (sasSeconds >= SAS_SECONDS_29FEB8000) {
                sasSeconds += SECONDS_IN_DAY; //restore Y8K leap day
            }
            sasSeconds += SECONDS_IN_DAY; //restore Y4K leap day
        }
        return sasSeconds;
    }

    /**
     * The same as sasLeapSecondsFix but for days.
     *
     * @param sasDays SAS days
     * @return fixed days
     */
    static double sasLeapDaysFix(double sasDays) {
        if (sasDays >= SAS_DAYS_29FEB4000) {
            if (sasDays >= SAS_DAYS_29FEB8000) {
                sasDays += 1; //restore Y8K leap day
            }
            sasDays += 1; //restore Y4K leap day
        }
        return sasDays;
    }
}
