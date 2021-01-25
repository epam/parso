package com.epam.parso;

import com.epam.parso.impl.SasFileReaderImpl;
import org.junit.Test;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static com.epam.parso.date.OutputDateType.*;
import static org.junit.Assert.assertEquals;

/**
 * These tests cover JAVA_DATE, JAVA_TEMPORAL, EPOCH_SECONDS and SAS_VALUE.
 * <p>
 * These tests don't test SAS_FORMAT or SAS_FORMAT_TRIM, all SAS_FORMAT-related
 * cases are covered by separate Sas[FormatName]Test classes.
 */
public class SasDateTypeTest {

    private static Date utcDateOf(int year, int month, int day) {
        return Date.from(LocalDateTime.of(year, month, day, 0, 0).toInstant(ZoneOffset.UTC));
    }

    private static Date utcDateOf(int year, int month, int day, int hour, int minute, int second, int nanos) {
        return Date.from(LocalDateTime.of(year, month, day, hour, minute, second, nanos).toInstant(ZoneOffset.UTC));
    }

    private static double utcEpochSecondsOf(int year, int month, int day) {
        return LocalDateTime.of(year, month, day, 0, 0).toEpochSecond(ZoneOffset.UTC);
    }

    @Test
    public void testJavaDate() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_format_date.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, JAVA_DATE_LEGACY);
            Object[][] result = sasFileReader.readAll();
            assertEquals(utcDateOf(2013, 3, 17), result[0][0]);
        }
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_format_datetime.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, JAVA_DATE_LEGACY);
            Object[][] result = sasFileReader.readAll();
            assertEquals(utcDateOf(2013, 3, 17, 19, 53, 1, 321000000), result[0][0]);
        }
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_format_time.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, JAVA_DATE_LEGACY);
            Object[][] result = sasFileReader.readAll();
            assertEquals(71581.321, result[0][0]);
        }
    }

    @Test
    public void testJavaTemporal() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_format_date.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, JAVA_TEMPORAL);
            Object[][] result = sasFileReader.readAll();
            assertEquals(result[0][0], LocalDate.of(2013, 3, 17));
        }
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_format_datetime.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, JAVA_TEMPORAL);
            Object[][] result = sasFileReader.readAll();
            assertEquals(LocalDateTime.of(2013, 3, 17, 19, 53, 1, 321000000), result[0][0]);
        }
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_format_time.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, JAVA_TEMPORAL);
            Object[][] result = sasFileReader.readAll();
            assertEquals(71581.321, result[0][0]);
        }
    }

    @Test
    public void testSasValue() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_format_date.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, SAS_VALUE);
            Object[][] result = sasFileReader.readAll();
            assertEquals(19434.0, result[0][0]);
        }
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_format_datetime.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, SAS_VALUE);
            Object[][] result = sasFileReader.readAll();
            assertEquals(1679169181.321, result[0][0]);
        }
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_format_time.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, SAS_VALUE);
            Object[][] result = sasFileReader.readAll();
            assertEquals(71581.321, result[0][0]);
        }
    }

    @Test
    public void testEpochSeconds() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_format_date.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, EPOCH_SECONDS);
            Object[][] result = sasFileReader.readAll();
            assertEquals(1363478400.0, result[0][0]);
        }
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_format_datetime.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, EPOCH_SECONDS);
            Object[][] result = sasFileReader.readAll();
            assertEquals(1363549981.321, result[0][0]);
        }
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_format_time.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, EPOCH_SECONDS);
            Object[][] result = sasFileReader.readAll();
            assertEquals(71581.321, result[0][0]);
        }
    }

    @Test
    public void testLeapJavaDate() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_leap_days.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, JAVA_DATE_LEGACY);

            Object[][] result = sasFileReader.readAll();
            assertEquals(16, result.length);

            Date[] dates = {
                    utcDateOf(2000, 2, 28),
                    utcDateOf(2000, 2, 29),
                    utcDateOf(2000, 3, 1),
                    utcDateOf(2000, 12, 31),
                    utcDateOf(4000, 2, 28),
                    utcDateOf(4000, 3, 1),
                    utcDateOf(4000, 12, 31),
                    utcDateOf(6000, 2, 28),
                    utcDateOf(6000, 2, 29),
                    utcDateOf(6000, 3, 1),
                    utcDateOf(6000, 12, 31),
                    utcDateOf(8000, 2, 28),
                    utcDateOf(8000, 3, 1),
                    utcDateOf(8000, 12, 31),
                    null,
                    utcDateOf(9999, 12, 31),
            };
            for (int i = 0; i < dates.length; i++) {
                assertEquals(dates[i], result[i][0]);
                assertEquals(dates[i], result[i][1]);
            }
        }
    }

    @Test
    public void testLeapEpochSeconds() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_leap_days.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, EPOCH_SECONDS);

            Object[][] result = sasFileReader.readAll();
            assertEquals(16, result.length);

            Double[] dates = {
                    utcEpochSecondsOf(2000, 2, 28),
                    utcEpochSecondsOf(2000, 2, 29),
                    utcEpochSecondsOf(2000, 3, 1),
                    utcEpochSecondsOf(2000, 12, 31),
                    utcEpochSecondsOf(4000, 2, 28),
                    utcEpochSecondsOf(4000, 3, 1),
                    utcEpochSecondsOf(4000, 12, 31),
                    utcEpochSecondsOf(6000, 2, 28),
                    utcEpochSecondsOf(6000, 2, 29),
                    utcEpochSecondsOf(6000, 3, 1),
                    utcEpochSecondsOf(6000, 12, 31),
                    utcEpochSecondsOf(8000, 2, 28),
                    utcEpochSecondsOf(8000, 3, 1),
                    utcEpochSecondsOf(8000, 12, 31),
                    null,
                    utcEpochSecondsOf(9999, 12, 31),
            };
            for (int i = 0; i < dates.length; i++) {
                assertEquals(dates[i], result[i][0]);
                assertEquals(dates[i], result[i][1]);
            }
        }
    }

    @Test
    public void testLeapSasValue() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/dates/sas7bdat/date_leap_days.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is, null, SAS_VALUE);

            Object[][] result = sasFileReader.readAll();
            assertEquals(16, result.length);

            Double[][] dates = {
                    {14668D, 1267315200D},
                    {14669D, 1267401600D},
                    {14670D, 1267488000D},
                    {14975D, 1293840000D},
                    {745153D, 64381219200D},
                    {745154D, 64381305600D},
                    {745459D, 64407657600D},
                    {1475637D, 127495036800D},
                    {1475638D, 127495123200D},
                    {1475639D, 127495209600D},
                    {1475944D, 127521561600D},
                    {2206122D, 190608940800D},
                    {2206123D, 190609027200D},
                    {2206428D, 190635379200D},
                    {Double.NaN, Double.NaN},
                    {2936547D, 253717660800D},
            };
            for (int i = 0; i < dates.length; i++) {
                assertEquals(dates[i][0], result[i][0]);
                assertEquals(dates[i][1], result[i][1]);
            }
        }
    }
}
