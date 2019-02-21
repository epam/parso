/**
 * *************************************************************************
 * Copyright (C) 2015 EPAM
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * *************************************************************************
 */

package com.epam.parso;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A helper class to allow re-use formatted values from sas7bdat file.
 */
public final class DataWriterUtil {

    /**
     * The number of digits starting from the first non-zero value, used to round doubles.
     */
    private static final int ACCURACY = 15;

    /**
     * The class name of array of byte.
     */
    private static final String BYTE_ARRAY_CLASS_NAME = (new byte[0]).getClass().getName();

    /**
     * Encoding used to convert byte arrays to string.
     */
    private static final String ENCODING = "CP1252";

    /**
     * If the number of digits in a double value exceeds a given constant, it rounds off.
     */
    private static final int ROUNDING_LENGTH = 13;

    /**
     * The constant to check whether or not a string containing double stores infinity.
     */
    private static final String DOUBLE_INFINITY_STRING = "Infinity";

    /**
     * The format to output hours in the CSV format.
     */
    private static final String HOURS_OUTPUT_FORMAT = "%02d";

    /**
     * The format to output minutes in the CSV format.
     */
    private static final String MINUTES_OUTPUT_FORMAT = "%02d";

    /**
     * The format to output seconds in the CSV format.
     */
    private static final String SECONDS_OUTPUT_FORMAT = "%02d";

    /**
     * The delimiter between hours and minutes, minutes and seconds in the CSV file.
     */
    private static final String TIME_DELIMETER = ":";

    /**
     * The date formats to store the hour, minutes, seconds, and milliseconds. Appear in the data of
     * the {@link com.epam.parso.impl.SasFileParser.FormatAndLabelSubheader} subheader
     * and are stored in {@link Column#format}.
     */
    private static final List<String> TIME_FORMAT_STRINGS = Arrays.asList("TIME", "HHMM");

    /**
     * The format to store the percentage values. Appear in the data of
     * the {@link com.epam.parso.impl.SasFileParser.FormatAndLabelSubheader} subheader
     * and are stored in {@link Column#format}.
     */
    private static final String PERCENT_FORMAT = "PERCENT";

    /**
     * The number of seconds in a minute.
     */
    private static final int SECONDS_IN_MINUTE = 60;

    /**
     * The number of minutes in an hour.
     */
    private static final int MINUTES_IN_HOUR = 60;

    /**
     * The locale for dates in output row.
     */
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    /**
     * Error string if column format is unknown.
     */
    private static final String UNKNOWN_DATE_FORMAT_EXCEPTION = "Unknown date format";

    /**
     * These are sas7bdat format references to {@link java.text.SimpleDateFormat} date formats.
     * <p>
     * UNSUPPORTED FORMATS:
     * DTYYQC, PDJULG, PDJULI, QTR, QTRR, WEEKU, WEEKV, WEEKW,
     * YYQ, YYQC, YYQD, YYQN, YYQP, YYQS, YYQR, YYQRC, YYQRD, YYQRN, YYQRP, YYQRS
     */
    private static final Map<String, String>
            DATE_OUTPUT_FORMAT_STRINGS = Collections.unmodifiableMap(new HashMap<String, String>() {
        {
            /* date formats */
            put("B8601DA", "yyyyMMdd");
            put("E8601DA", "yyyy-MM-dd");
            put("DATE", "ddMMMyyyy");
            put("DAY", "dd");
            put("DDMMYY", "dd/MM/yyyy");
            put("DDMMYYB", "dd MM yyyy");
            put("DDMMYYC", "dd:MM:yyyy");
            put("DDMMYYD", "dd-MM-yyyy");
            put("DDMMYYN", "ddMMyyyy");
            put("DDMMYYP", "dd.MM.yyyy");
            put("DDMMYYS", "dd/MM/yyyy");
            put("JULDAY", "D");
            put("JULIAN", "yyyyD");
            put("MMDDYY", "MM/dd/yyyy");
            put("MMDDYYB", "MM dd yyyy");
            put("MMDDYYC", "MM:dd:yyyy");
            put("MMDDYYD", "MM-dd-yyyy");
            put("MMDDYYN", "MMddyyyy");
            put("MMDDYYP", "MM.dd.yyyy");
            put("MMDDYYS", "MM/dd/yyyy");
            put("MMYY", "MM'M'yyyy");
            put("MMYYC", "MM:yyyy");
            put("MMYYD", "MM-yyyy");
            put("MMYYN", "MMyyyy");
            put("MMYYP", "MM.yyyy");
            put("MMYYS", "MM/yyyy");
            put("MONNAME", "MMMM");
            put("MONTH", "M");
            put("MONYY", "MMMyyyy");
            put("WEEKDATE", "EEEE, MMMM dd, yyyy");
            put("WEEKDATX", "EEEE, dd MMMM, yyyy");
            put("WEEKDAY", "u");
            put("DOWNAME", "EEEE");
            put("WORDDATE", "MMMM d, yyyy");
            put("WORDDATX", "d MMMM yyyy");
            put("YYMM", "yyyy'M'MM");
            put("YYMMC", "yyyy:MM");
            put("YYMMD", "yyyy-MM");
            put("YYMMN", "yyyyMM");
            put("YYMMP", "yyyy.MM");
            put("YYMMS", "yyyy/MM");
            put("YYMMDD", "yyyy-MM-dd");
            put("YYMMDDB", "yyyy MM dd");
            put("YYMMDDC", "yyyy:MM:dd");
            put("YYMMDDD", "yyyy-MM-dd");
            put("YYMMDDN", "yyyyMMdd");
            put("YYMMDDP", "yyyy.MM.dd");
            put("YYMMDDS", "yyyy/MM/dd");
            put("YYMON", "yyyyMMM");
            put("YEAR", "yyyy");

            /* datetime formats */
            put("B8601DN", "yyyyMMdd");
            put("B8601DT", "yyyyMMdd'T'HHmmssSSS");
            put("B8601DX", "yyyyMMdd'T'HHmmssZ");
            put("B8601DZ", "yyyyMMdd'T'HHmmssZ");
            put("B8601LX", "yyyyMMdd'T'HHmmssZ");
            put("E8601DN", "yyyy-MM-dd");
            put("E8601DT", "yyyy-MM-dd'T'HH:mm:ss.SSS");
            put("E8601DX", "yyyy-MM-dd'T'HH:mm:ssZ");
            put("E8601DZ", "yyyy-MM-dd'T'HH:mm:ssZ");
            put("E8601LX", "yyyy-MM-dd'T'HH:mm:ssZ");
            put("DATEAMPM", "ddMMMyyyy:HH:mm:ss.SS a");
            put("DATETIME", "ddMMMyyyy:HH:mm:ss.SS");
            put("DTDATE", "ddMMMyyyy");
            put("DTMONYY", "MMMyyyy");
            put("DTWKDATX", "EEEE, dd MMMM, yyyy");
            put("DTYEAR", "yyyy");
            put("MDYAMPM", "MM/dd/yyyy H:mm a");
            put("TOD", "HH:mm:ss.SS");
        }
    });

    /**
     * Empty private constructor for preventing instances.
     */
    private DataWriterUtil() {

    }

    /**
     * Checks current entry type and returns its string representation.
     *
     * @param column           current processing column.
     * @param entry            current processing entry.
     * @param locale           the locale for parsing date and percent elements.
     * @param columnFormatters the map that stores (@link Column#id) column identifier and the formatter
     *                         for converting locale-sensitive values stored in this column into string.
     * @return a string representation of current processing entry.
     * @throws IOException appears if the output into writer is impossible.
     */
    private static String processEntry(Column column, Object entry, Locale locale,
                                       Map<Integer, Format> columnFormatters) throws IOException {
        if (!String.valueOf(entry).contains(DOUBLE_INFINITY_STRING)) {
            String valueToPrint;
            if (entry.getClass() == Date.class) {
                valueToPrint = convertDateElementToString((Date) entry,
                        (SimpleDateFormat) columnFormatters.computeIfAbsent(column.getId(),
                                e -> getDateFormatProcessor(column.getFormat(), locale)));
            } else {
                if (TIME_FORMAT_STRINGS.contains(column.getFormat().getName())) {
                    valueToPrint = convertTimeElementToString((Long) entry);
                } else if (PERCENT_FORMAT.equals(column.getFormat().getName())) {
                    valueToPrint = convertPercentElementToString(entry,
                            (DecimalFormat) columnFormatters.computeIfAbsent(column.getId(),
                                    e -> getPercentFormatProcessor(column.getFormat(), locale)));
                } else {
                    valueToPrint = String.valueOf(entry);
                    if (entry.getClass() == Double.class) {
                        valueToPrint = convertDoubleElementToString((Double) entry);
                    }
                }
            }

            return valueToPrint;
        }
        return "";
    }

    /**
     * The function to convert a date into a string according to the format used.
     *
     * @param currentDate the date to convert.
     * @param dateFormat  the formatter to convert date element into string.
     * @return the string that corresponds to the date in the format used.
     */
    private static String convertDateElementToString(Date currentDate, SimpleDateFormat dateFormat) {
        return currentDate.getTime() != 0 ? dateFormat.format(currentDate.getTime()) : "";
    }

    /**
     * The function to convert time without a date (hour, minute, second) from the sas7bdat file format
     * (which is the number of seconds elapsed from the midnight) into a string of the format set by the constants:
     * {@link DataWriterUtil#HOURS_OUTPUT_FORMAT}, {@link DataWriterUtil#MINUTES_OUTPUT_FORMAT},
     * {@link DataWriterUtil#SECONDS_OUTPUT_FORMAT}, and {@link DataWriterUtil#TIME_DELIMETER}.
     *
     * @param secondsFromMidnight the number of seconds elapsed from the midnight.
     * @return the string of time in the format set by constants.
     */
    private static String convertTimeElementToString(Long secondsFromMidnight) {
        return String.format(HOURS_OUTPUT_FORMAT, secondsFromMidnight / SECONDS_IN_MINUTE / MINUTES_IN_HOUR)
                + TIME_DELIMETER
                + String.format(MINUTES_OUTPUT_FORMAT, secondsFromMidnight / SECONDS_IN_MINUTE % MINUTES_IN_HOUR)
                + TIME_DELIMETER
                + String.format(SECONDS_OUTPUT_FORMAT, secondsFromMidnight % SECONDS_IN_MINUTE);
    }

    /**
     * The function to convert a double value into a string. If the text presentation of the double is longer
     * than {@link DataWriterUtil#ROUNDING_LENGTH}, the rounded off value of the double includes
     * the {@link DataWriterUtil#ACCURACY} number of digits from the first non-zero value.
     *
     * @param value the input numeric value to convert.
     * @return the string with the text presentation of the input numeric value.
     */
    private static String convertDoubleElementToString(Double value) {
        String valueToPrint = String.valueOf(value);
        if (valueToPrint.length() > ROUNDING_LENGTH) {
            int lengthBeforeDot = (int) Math.ceil(Math.log10(Math.abs(value)));
            BigDecimal bigDecimal = new BigDecimal(value);
            bigDecimal = bigDecimal.setScale(ACCURACY - lengthBeforeDot, BigDecimal.ROUND_HALF_UP);
            valueToPrint = String.valueOf(bigDecimal.doubleValue());
        }
        valueToPrint = trimZerosFromEnd(valueToPrint);
        return valueToPrint;
    }

    /**
     * The function to convert a percent element into a string.
     *
     * @param value         the input numeric value to convert.
     * @param decimalFormat the formatter to convert percentage element into string.
     * @return the string with the text presentation of the input numeric value.
     */
    private static String convertPercentElementToString(Object value, DecimalFormat decimalFormat) {
        Double doubleValue = value instanceof Long ? ((Long) value).doubleValue() : (Double) value;
        return decimalFormat.format(doubleValue);
    }

    /**
     * The function to remove trailing zeros from the decimal part of the numerals represented by a string.
     * If there are no digits after the point, the point is deleted as well.
     *
     * @param string the input string trailing zeros.
     * @return the string without trailing zeros.
     */
    private static String trimZerosFromEnd(String string) {
        return string.contains(".") ? string.replaceAll("0*$", "").replaceAll("\\.$", "") : string;
    }

    /**
     * The method to convert the Objects array that stores data from the sas7bdat file to list of string.
     *
     * @param columns          the {@link Column} class variables list that stores columns
     *                         description from the sas7bdat file.
     * @param row              the Objects arrays that stores data from the sas7bdat file.
     * @param locale           the locale for parsing date elements.
     * @param columnFormatters the map that stores (@link Column#id) column identifier and the formatter
     *                         for converting locale-sensitive values stored in this column into string.
     * @return list of String objects that represent data from sas7bdat file.
     * @throws java.io.IOException appears if the output into writer is impossible.
     */
    public static List<String> getRowValues(List<Column> columns, Object[] row, Locale locale,
                                            Map<Integer, Format> columnFormatters) throws IOException {
        List<String> values = new ArrayList<String>();
        for (int currentColumnIndex = 0; currentColumnIndex < columns.size(); currentColumnIndex++) {
            values.add(getValue(columns.get(currentColumnIndex), row[currentColumnIndex], locale, columnFormatters));
        }
        return values;
    }

    /**
     * The method to convert the Objects array that stores data from the sas7bdat file to list of string.
     *
     * @param columns          the {@link Column} class variables list that stores columns
     *                         description from the sas7bdat file.
     * @param row              the Objects arrays that stores data from the sas7bdat file.
     * @param columnFormatters the map that stores (@link Column#id) column identifier and the formatter
     *                         for converting locale-sensitive values stored in this column into string.
     * @return list of String objects that represent data from sas7bdat file.
     * @throws java.io.IOException appears if the output into writer is impossible.
     */
    public static List<String> getRowValues(List<Column> columns, Object[] row,
                                            Map<Integer, Format> columnFormatters) throws IOException {
        return getRowValues(columns, row, DEFAULT_LOCALE, columnFormatters);
    }

    /**
     * The method to convert the Object that stores data from the sas7bdat file cell to string.
     *
     * @param column           the {@link Column} class variable that stores current processing column.
     * @param entry            the Object that stores data from the cell of sas7bdat file.
     * @param locale           the locale for parsing date elements.
     * @param columnFormatters the map that stores (@link Column#id) column identifier and the formatter
     *                         for converting locale-sensitive values stored in this column into string.
     * @return a string representation of current processing entry.
     * @throws IOException appears if the output into writer is impossible.
     */
    public static String getValue(Column column, Object entry, Locale locale,
                                  Map<Integer, Format> columnFormatters) throws IOException {
        String value = "";
        if (entry != null) {
            if (entry.getClass().getName().compareTo(BYTE_ARRAY_CLASS_NAME) == 0) {
                value = new String((byte[]) entry, ENCODING);
            } else {
                value = processEntry(column, entry, locale, columnFormatters);
            }
        }
        return value;
    }

    /**
     * The function to get a formatter to convert percentage elements into a string.
     *
     * @param columnFormat the (@link ColumnFormat) class variable that stores the precision of rounding
     *                     the converted value.
     * @param locale       locale for parsing date elements.
     * @return a formatter to convert percentage elements into a string.
     */
    private static Format getPercentFormatProcessor(ColumnFormat columnFormat, Locale locale) {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
        if (columnFormat.getPrecision() == 0) {
            return new DecimalFormat("0%", dfs);
        }
        String pattern = "0%." + new String(new char[columnFormat.getPrecision()]).replace("\0", "0");
        return new DecimalFormat(pattern, dfs);
    }

    /**
     * The function to get a formatter to convert date elements into a string.
     *
     * @param columnFormat the (@link ColumnFormat) class variable that stores the format name that must belong to
     *                     the set of {@link DataWriterUtil#DATE_OUTPUT_FORMAT_STRINGS} mapping keys.
     * @param locale       locale for parsing date elements.
     * @return a formatter to convert date elements into a string.
     */
    private static Format getDateFormatProcessor(ColumnFormat columnFormat, Locale locale) {
        if (!DATE_OUTPUT_FORMAT_STRINGS.containsKey(columnFormat.getName())) {
            throw new NoSuchElementException(UNKNOWN_DATE_FORMAT_EXCEPTION);
        }
        SimpleDateFormat dateFormat =
                new SimpleDateFormat(DATE_OUTPUT_FORMAT_STRINGS.get(columnFormat.getName()), locale);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat;
    }
}
