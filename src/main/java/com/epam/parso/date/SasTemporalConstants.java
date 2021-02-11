package com.epam.parso.date;

import java.math.BigDecimal;

/**
 * Date and time related constants.
 */
interface SasTemporalConstants {

    /**
     * Milliseconds in one second.
     */
    int MILLIS_IN_SECOND = 1000;

    /**
     * Seconds in one minute.
     */
    int SECONDS_IN_MINUTE = 60;

    /**
     * Seconds in one minute as BigDecimal.
     */
    BigDecimal BIG_SECONDS_IN_MINUTE = BigDecimal.valueOf(SECONDS_IN_MINUTE);

    /**
     * Minutes in one hour.
     */
    int MINUTES_IN_HOUR = 60;

    /**
     * Minutes in one minute as BigDecimal.
     */
    BigDecimal BIG_MINUTES_IN_HOUR = BigDecimal.valueOf(MINUTES_IN_HOUR);

    /**
     * Seconds in one hour.
     */
    int SECONDS_IN_HOUR = SECONDS_IN_MINUTE * MINUTES_IN_HOUR;

    /**
     * Seconds in one hour as BigDecimal.
     */
    BigDecimal BIG_SECONDS_IN_HOUR = BigDecimal.valueOf(SECONDS_IN_HOUR);

    /**
     * Seconds in one day.
     */
    int SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;

    /**
     * Seconds in one day as BigDecimal.
     */
    BigDecimal BIG_SECONDS_IN_DAY = BigDecimal.valueOf(SECONDS_IN_DAY);

    /**
     * Nanoseconds in a second.
     */
    int NANOS_IN_SECOND = 1_000_000_000;

    /**
     * Nanoseconds in a second as BigDecimal.
     */
    BigDecimal BIG_NANOS_IN_SECOND = BigDecimal.valueOf(NANOS_IN_SECOND);

    /**
     * Single nanosecond in scope of second.
     */
    BigDecimal BIG_NANOSECOND_FRACTION = BigDecimal.valueOf(0.000_000_001);

    /**
     * The difference in days between 01/01/1960 (the dates starting point in SAS)
     * and 01/01/1970 (the dates starting point in Java).
     */
    int SAS_VS_EPOCH_DIFF_DAYS = 365 * 10 + 3;

    /**
     * The difference in seconds between 01/01/1960 (the dates starting point in SAS)
     * and 01/01/1970 (the dates starting point in Java).
     */
    int SAS_VS_EPOCH_DIFF_SECONDS = SAS_VS_EPOCH_DIFF_DAYS * SECONDS_IN_DAY;

    /**
     * First time when a leap day is removed from the SAS calendar.
     * In days since 1960-01-01
     */
    double SAS_DAYS_29FEB4000 = 745_154D;

    /**
     * First time when a leap day is removed from the SAS calendar.
     * In seconds since 1960-01-01
     */
    double SAS_SECONDS_29FEB4000 = SAS_DAYS_29FEB4000 * SECONDS_IN_DAY;

    /**
     * Second time when a leap day is removed from the SAS calendar.
     * In days since 1960-01-01
     */
    double SAS_DAYS_29FEB8000 = 2_206_123;

    /**
     * Second time when a leap day is removed from the SAS calendar.
     * In seconds since 1960-01-01
     */
    double SAS_SECONDS_29FEB8000 = SAS_DAYS_29FEB8000 * SECONDS_IN_DAY;
}
