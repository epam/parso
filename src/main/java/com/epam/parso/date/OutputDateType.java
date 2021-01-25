package com.epam.parso.date;

/**
 * Option how Parso outputs dates.
 */
public enum OutputDateType {
    /**
     * Outputs date as java.util.Date for date and datetime formats.
     * Note that time will be represented as a number.
     */
    JAVA_DATE_LEGACY,
    /**
     * Outputs date as LocalDate and LocalDateTime for date and datetime formats.
     * Note that time will be represented as a number, because of SAS time
     * can't be represented as a LocalTime!
     */
    JAVA_TEMPORAL,
    /**
     * Output date as raw SAS value.
     * It is number of days (for date type) or seconds(for datetime type)
     * since 1 January 1960 as java double.
     */
    SAS_VALUE,
    /**
     * Output date as number of seconds since 1 January 1970 as java double.
     * Note that it may contain fractional part.
     */
    EPOCH_SECONDS
}
