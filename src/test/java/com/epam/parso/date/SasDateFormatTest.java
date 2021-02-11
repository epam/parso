package com.epam.parso.date;

import com.epam.parso.Column;
import com.epam.parso.SasFileReader;
import com.epam.parso.impl.SasFileReaderImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


public class SasDateFormatTest {
    private static final Logger logger = LoggerFactory.getLogger(SasDateFormatTest.class);

    /**
     * Validate the "dataset.sas7bdat" file against the "dataset.tsv" file
     * checking both trimmed and not-trimmed dates.
     */
    private static void assertSasDateFormat(String dataset) throws Exception {
        assertSasDateFormat(dataset, true, false);
        assertSasDateFormat(dataset, false, false);
    }

    private static void assertSasDateFormatIgnoreSasRoundingBugs(String dataset) throws Exception {
        assertSasDateFormat(dataset, true, true);
        assertSasDateFormat(dataset, false, true);
    }

    private static void assertSasDateFormat(String dataset, boolean trim, boolean ignoreSasRoundingBugs) throws Exception {
        try (InputStream sasInputStream = new FileInputStream(
                "src/test/resources/dates/sas7bdat/" + dataset + ".sas7bdat");
             BufferedReader tsvReader = new BufferedReader(new FileReader(
                     "src/test/resources/dates/tsv/" + dataset + ".tsv"))
        ) {
            SasFileReader sasReader = new SasFileReaderImpl(sasInputStream, null,
                    trim ? OutputDateType.SAS_FORMAT_TRIM_EXPERIMENTAL : OutputDateType.SAS_FORMAT_EXPERIMENTAL);
            List<Column> columns = sasReader.getColumns();
            int row = 0;
            String line;
            int sasRoundingBugsCount = 0;
            while ((line = tsvReader.readLine()) != null) {
                row++;
                Object[] sasRow = sasReader.readNext();
                String[] tsvRow = line.split("\t", -1);
                assertThat(sasRow).withFailMessage("Same amount of columns").hasSameSizeAs(tsvRow);
                for (int col = 0; col < tsvRow.length; col++) {
                    final int row1 = row;
                    String expected = trim ? tsvRow[col].trim() : tsvRow[col];
                    String actual = Objects.toString(sasRow[col]);
                    Column column = columns.get(col);

                    if (!expected.equals(actual)) {
                        if (ignoreSasRoundingBugs) {
                            int expectedIntegralIndex = expected.lastIndexOf(':');
                            int expectedFractionIndex = expected.lastIndexOf('.');
                            String expectedIntegral = expectedIntegralIndex > 0 ?
                                    expected.substring(0, expectedIntegralIndex) : "0";
                            double expectedFraction = Double.parseDouble(
                                    expected.substring(expectedIntegralIndex + 1));

                            int actualIntegralIndex = actual.lastIndexOf(':');
                            int actualFractionIndex = actual.lastIndexOf('.');
                            String actualIntegral = actualIntegralIndex > 0 ?
                                    actual.substring(0, actualIntegralIndex) : "0";
                            double actualFraction = Double.parseDouble(
                                    actual.substring(actualIntegralIndex + 1));

                            int bugPosition = Math.max(
                                    expectedFractionIndex == -1 ? 0 : expected.length() - expectedFractionIndex - 1,
                                    actualFractionIndex == -1 ? 0 : actual.length() - actualFractionIndex - 1
                            );
                            if (expected.length() == actual.length()
                                    && expectedIntegral.equals(actualIntegral)
                                    && BigDecimal.valueOf(expectedFraction)
                                    .subtract(BigDecimal.valueOf(actualFraction))
                                    .abs()
                                    .movePointRight(bugPosition)
                                    .compareTo(BigDecimal.ONE) == 0) {

                                sasRoundingBugsCount += 1;
                                logger.debug("Rounding bug ignored (fraction {} vs. {})\n"
                                                + "Row: {}, column: {}, format: {}\nexpected: {}\nactual: {}",
                                        expectedFraction, actualFraction,
                                        row1, column.getName(), column.getFormat(),
                                        expected, actual);
                                continue;
                            }
                        }

                        assertThat(actual).withFailMessage(() -> String.format(
                                "Row: #%d, column: [%s], format: [%s]\nexpected: [%s]\nactual: [%s]\n",
                                row1, column.getName(), column.getFormat(),
                                expected, actual)
                        ).isEqualTo(expected);
                    }
                }
            }
            if (sasRoundingBugsCount > 0) {
                logger.warn("Ignored [{}] ([{}%]) SAS rounding bugs for [{}]",
                        sasRoundingBugsCount,
                        Math.round(10000.0 * sasRoundingBugsCount / row / columns.size()) / 100.0,
                        dataset);
            }
            assertThat(sasReader.readNext()).withFailMessage("All rows are read").isNull();
        }
    }

    // Date formats
    @Test
    public void testDATE() throws Exception {
        assertSasDateFormat("date_format_date");
    }

    @Test
    public void testDDMMYY() throws Exception {
        assertSasDateFormat("date_format_ddmmyy");
    }

    @Test
    public void testMMDDYY() throws Exception {
        assertSasDateFormat("date_format_mmddyy");
    }

    @Test
    public void testYYMMDD() throws Exception {
        assertSasDateFormat("date_format_yymmdd");
    }

    @Test
    public void testMMYY() throws Exception {
        assertSasDateFormat("date_format_mmyy");
    }

    @Test
    public void testYYMM() throws Exception {
        assertSasDateFormat("date_format_yymm");
    }

    @Test
    public void testDAY() throws Exception {
        assertSasDateFormat("date_format_day");
    }

    @Test
    public void testJULDAY() throws Exception {
        assertSasDateFormat("date_format_julday");
    }

    @Test
    public void testJULIAN() throws Exception {
        assertSasDateFormat("date_format_julian");
    }

    @Test
    public void testMONTH() throws Exception {
        assertSasDateFormat("date_format_month");
    }

    @Test
    public void testYEAR() throws Exception {
        assertSasDateFormat("date_format_year");
    }

    @Test
    public void testMONYY() throws Exception {
        assertSasDateFormat("date_format_monyy");
    }

    @Test
    public void testYYMON() throws Exception {
        assertSasDateFormat("date_format_yymon");
    }

    @Test
    public void testB8601DA() throws Exception {
        assertSasDateFormat("date_format_b8601da");
    }

    @Test
    public void testE8601DA() throws Exception {
        assertSasDateFormat("date_format_e8601da");
    }

    @Test
    public void testMONNAME() throws Exception {
        assertSasDateFormat("date_format_monname");
    }

    @Test
    public void testWEEKDAY() throws Exception {
        assertSasDateFormat("date_format_weekday");
    }

    @Test
    public void testDOWNAME() throws Exception {
        assertSasDateFormat("date_format_downame");
    }

    @Test
    public void testWORDDATE() throws Exception {
        assertSasDateFormat("date_format_worddate");
    }

    @Test
    public void testWORDDATX() throws Exception {
        assertSasDateFormat("date_format_worddatx");
    }

    @Test
    public void testWEEKDATE() throws Exception {
        assertSasDateFormat("date_format_weekdate");
    }

    @Test
    public void testWEEKDATX() throws Exception {
        assertSasDateFormat("date_format_weekdatx");
    }

    @Test
    public void testDTDATE() throws Exception {
        assertSasDateFormat("date_format_dtdate");
    }

    @Test
    public void testDTMONYY() throws Exception {
        assertSasDateFormat("date_format_dtmonyy");
    }

    @Test
    public void testDTYEAR() throws Exception {
        assertSasDateFormat("date_format_dtyear");
    }

    @Test
    public void testE8601DN() throws Exception {
        assertSasDateFormat("date_format_e8601dn");
    }

    @Test
    public void testB8601DN() throws Exception {
        assertSasDateFormat("date_format_b8601dn");
    }

    @Test
    public void testQTR() {
        assertThat(SasDateFormat.QTR.getDatePattern(1, 0)).isNull();
        assertThat(SasDateFormat.QTR.getFormatFunction(1, 0, true).apply(0D))
                .isEqualTo("01JAN60");
    }

    // Date-time formats
    @Test
    public void testDATETIME() throws Exception {
        assertSasDateFormatIgnoreSasRoundingBugs("date_format_datetime");
        assertSasDateFormatIgnoreSasRoundingBugs("date_format_datetime_loop");
    }

    @Test
    public void testB8601DT() {
        assertThat(SasDateTimeFormat.B8601DT.getDatePattern(1, 0)).isNull();
        assertThat(SasDateTimeFormat.B8601DT.getFormatFunction(1, 0, true).apply(1.1))
                .isEqualTo("01JAN60:00:00:01");
    }

    @Test
    public void testB8601DX() {
        assertThat(SasDateTimeFormat.B8601DX.getDatePattern(1, 0)).isNull();
        assertThat(SasDateTimeFormat.B8601DX.getFormatFunction(1, 0, true).apply(1.1))
                .isEqualTo("01JAN60:00:00:01");
    }

    @Test
    public void testB8601DZ() {
        assertThat(SasDateTimeFormat.B8601DZ.getDatePattern(1, 0)).isNull();
        assertThat(SasDateTimeFormat.B8601DZ.getFormatFunction(1, 0, true).apply(1.1))
                .isEqualTo("01JAN60:00:00:01");
    }

    @Test
    public void testB8601LX() {
        assertThat(SasDateTimeFormat.B8601LX.getDatePattern(1, 0)).isNull();
        assertThat(SasDateTimeFormat.B8601LX.getFormatFunction(1, 0, true).apply(1.1))
                .isEqualTo("01JAN60:00:00:01");
    }

    @Test
    public void testE8601DT() {
        assertThat(SasDateTimeFormat.E8601DT.getDatePattern(1, 0)).isNull();
        assertThat(SasDateTimeFormat.E8601DT.getFormatFunction(1, 0, true).apply(1.1))
                .isEqualTo("01JAN60:00:00:01");
    }

    @Test
    public void testE8601DX() {
        assertThat(SasDateTimeFormat.E8601DX.getDatePattern(1, 0)).isNull();
        assertThat(SasDateTimeFormat.E8601DX.getFormatFunction(1, 0, true).apply(1.1))
                .isEqualTo("01JAN60:00:00:01");
    }

    @Test
    public void testE8601DZ() {
        assertThat(SasDateTimeFormat.E8601DZ.getDatePattern(1, 0)).isNull();
        assertThat(SasDateTimeFormat.E8601DZ.getFormatFunction(1, 0, true).apply(1.1))
                .isEqualTo("01JAN60:00:00:01");
    }

    @Test
    public void testE8601LX() {
        assertThat(SasDateTimeFormat.E8601LX.getDatePattern(1, 0)).isNull();
        assertThat(SasDateTimeFormat.E8601LX.getFormatFunction(1, 0, true).apply(1.1))
                .isEqualTo("01JAN60:00:00:01");
    }

    @Test
    public void testDATEAMPM() {
        assertThat(SasDateTimeFormat.DATEAMPM.getDatePattern(1, 0)).isNull();
        assertThat(SasDateTimeFormat.DATEAMPM.getFormatFunction(1, 0, true).apply(1.1))
                .isEqualTo("01JAN60:00:00:01");
    }

    @Test
    public void testDTWKDATX() {
        assertThat(SasDateTimeFormat.DTWKDATX.getDatePattern(1, 0)).isNull();
        assertThat(SasDateTimeFormat.DTWKDATX.getFormatFunction(1, 0, true).apply(1.1))
                .isEqualTo("01JAN60:00:00:01");
    }

    @Test
    public void testMDYAMPM() {
        assertThat(SasDateTimeFormat.MDYAMPM.getDatePattern(1, 0)).isNull();
        assertThat(SasDateTimeFormat.MDYAMPM.getFormatFunction(1, 0, true).apply(1.1))
                .isEqualTo("01JAN60:00:00:01");
    }

    @Test
    public void testTOD() throws Exception {
        assertThat(SasDateTimeFormat.TOD.getDatePattern(1, 0)).isNull();
        assertSasDateFormatIgnoreSasRoundingBugs("date_format_tod");
        assertSasDateFormatIgnoreSasRoundingBugs("date_format_tod_loop");
    }

    // Time formats
    @Test
    public void testE8601TM() throws Exception {
        assertSasDateFormatIgnoreSasRoundingBugs("date_format_e8601tm");
        assertSasDateFormatIgnoreSasRoundingBugs("date_format_e8601tm_loop");
    }

    @Test
    public void testTIME() throws Exception {
        assertSasDateFormatIgnoreSasRoundingBugs("date_format_time");
        assertSasDateFormatIgnoreSasRoundingBugs("date_format_time_loop");
    }

    @Test
    public void testHHMM() throws Exception {
        assertSasDateFormatIgnoreSasRoundingBugs("date_format_hhmm");
        assertSasDateFormatIgnoreSasRoundingBugs("date_format_hhmm_loop");
    }

    @Test
    public void testMMSS() throws Exception {
        assertSasDateFormatIgnoreSasRoundingBugs("date_format_mmss");
        assertSasDateFormatIgnoreSasRoundingBugs("date_format_mmss_loop");
    }

    @Test
    public void testHOUR() throws Exception {
        assertSasDateFormat("date_format_hour");
        assertSasDateFormatIgnoreSasRoundingBugs("date_format_hour_loop");
    }

    @Test
    public void testTIMEAMPM() {
        assertThat(SasTimeFormat.TIMEAMPM.getFormatFunction(1, 0, true).apply(0D))
                .isEqualTo("0:00:00");
    }

    @Test
    public void testE8601LZ() {
        assertThat(SasTimeFormat.E8601LZ.getFormatFunction(1, 0, true).apply(0D))
                .isEqualTo("0:00:00");
    }

    // Other tests
    @Test
    public void testLeapDays() throws Exception {
        assertSasDateFormat("date_leap_days");
    }

    /**
     * QTRR format not declared in date-time enums,
     * but there is a file with a such formatted value.
     * It is just used to check how app handles unknown format.
     */
    @Test(expected = AssertionError.class)
    public void testNotDeclaresFormat() throws Exception {
        assertSasDateFormat("date_format_qtrr");
    }

    @Test
    public void testNullDates() {
        assertThat(SasDateFormat.DATE.getFormatFunction(1, 0, true).apply(null)).isEqualTo(".");
        assertThat(SasDateTimeFormat.DATETIME.getFormatFunction(1, 0, true).apply(null)).isEqualTo(".");
        assertThat(SasTimeFormat.TIME.getFormatFunction(1, 0, true).apply(null)).isEqualTo(".");
    }
}
