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
     * Output date as String formatted according to
     * column format defined in the source SAS file.
     * <p>
     * Not all formats are yet supported and there is a slight difference
     * between SAS and Parso results (at least in rounding),
     * so this option is marked as experimental.
     */
    SAS_FORMAT_EXPERIMENTAL,
    /**
     * Same as SAS_FORMAT, but result doesn't have leading spaces
     * (trimmed, as the SAS Universal Viewer outputs by default).
     */
    SAS_FORMAT_TRIM_EXPERIMENTAL,
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
