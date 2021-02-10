package com.epam.parso.date;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.epam.parso.date.OutputDateType.*;
import static com.epam.parso.date.SasTemporalUtils.*;


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
     * Cached date format functions.
     */
    private final Map<String, Function<Double, String>> dateFormatFunctions = new HashMap<>();

    /**
     * Cached time format functions.
     */
    private final Map<String, Function<Double, String>> timeFormatFunctions = new HashMap<>();

    /**
     * Cached date-time format functions.
     */
    private final Map<String, Function<Double, String>> dateTimeFormatFunctions = new HashMap<>();

    /**
     * Format SAS seconds explicitly into the java Date.
     *
     * @param sasSeconds seconds since 1960-01-01
     * @return date
     */
    public Date formatSasSecondsAsJavaDate(double sasSeconds) {
        sasSeconds = sasLeapSecondsFix(sasSeconds);
        return sasSecondsToDate(sasSeconds);
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
            if (dateFormatType == SAS_FORMAT_EXPERIMENTAL || dateFormatType == SAS_FORMAT_TRIM_EXPERIMENTAL) {
                return ".";
            } else {
                return null;
            }
        }

        sasDays = sasLeapDaysFix(sasDays);

        switch (dateFormatType) {
            case EPOCH_SECONDS:
                return sasDaysToEpochSeconds(sasDays);
            case JAVA_TEMPORAL:
                return sasDaysToLocalDate(sasDays);
            case SAS_FORMAT_EXPERIMENTAL:
            case SAS_FORMAT_TRIM_EXPERIMENTAL:
                boolean trim = dateFormatType == SAS_FORMAT_TRIM_EXPERIMENTAL;
                return dateFormatFunctions.computeIfAbsent(sasFormatName + width + "." + precision,
                        k -> SasDateFormat.valueOf(sasFormatName).getFormatFunction(width, precision,
                                trim)
                ).apply(sasDays);
            case JAVA_DATE_LEGACY:
            default:
                return sasDaysToDate(sasDays);
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
            if (dateFormatType == SAS_FORMAT_EXPERIMENTAL || dateFormatType == SAS_FORMAT_TRIM_EXPERIMENTAL) {
                return ".";
            } else {
                return null;
            }
        }

        switch (dateFormatType) {
            case SAS_FORMAT_EXPERIMENTAL:
            case SAS_FORMAT_TRIM_EXPERIMENTAL:
                boolean trim = dateFormatType == SAS_FORMAT_TRIM_EXPERIMENTAL;
                return timeFormatFunctions.computeIfAbsent(sasFormatName + width + "." + precision,
                        k -> SasTimeFormat.valueOf(sasFormatName).getFormatFunction(width, precision, trim)
                ).apply(sasSeconds);
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
            if (dateFormatType == SAS_FORMAT_EXPERIMENTAL || dateFormatType == SAS_FORMAT_TRIM_EXPERIMENTAL) {
                return ".";
            } else {
                return null;
            }
        }

        sasSeconds = sasLeapSecondsFix(sasSeconds);

        switch (dateFormatType) {
            case EPOCH_SECONDS:
                return sasSecondsToEpochSeconds(sasSeconds);
            case JAVA_TEMPORAL:
                return sasSecondsToLocalDateTime(sasSeconds, 9);
            case SAS_FORMAT_EXPERIMENTAL:
            case SAS_FORMAT_TRIM_EXPERIMENTAL:
                boolean trim = dateFormatType == SAS_FORMAT_TRIM_EXPERIMENTAL;
                return dateTimeFormatFunctions.computeIfAbsent(sasFormatName + width + "." + precision,
                        k -> SasDateTimeFormat.valueOf(sasFormatName)
                                .getFormatFunction(width, precision, trim)
                ).apply(sasSeconds);
            case JAVA_DATE_LEGACY:
            default:
                return sasSecondsToDate(sasSeconds);
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
