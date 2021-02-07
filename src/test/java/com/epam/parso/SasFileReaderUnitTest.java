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

import au.com.bytecode.opencsv.CSVReader;
import com.epam.parso.impl.CSVDataWriterImpl;
import com.epam.parso.impl.CSVMetadataWriterImpl;
import com.epam.parso.impl.SasFileReaderImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.epam.parso.TestUtils.compareResultWithControl;
import static com.epam.parso.TestUtils.getResourceAsStream;
import static org.assertj.core.api.Assertions.assertThat;

public class SasFileReaderUnitTest {
    private static final Logger logger = LoggerFactory.getLogger(SasFileReaderUnitTest.class);
    private static final String DEFAULT_FILE_NAME = "sas7bdat//all_rand_normal.sas7bdat";
    private static final List<Integer> COLON_COLUMN_IDS = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
    private static final List<String> COLON_COLUMN_NAMES = Arrays.asList("sex", "age", "stage", "mmdx", "yydx",
            "surv_mm", "surv_yy", "status", "subsite", "year8594", "agegrp", "dx", "exit");
    private static final List<String> COLON_COLUMN_FORMATS = Arrays.asList("", "", "", "", "", "", "", "", "", "", "",
            "MMDDYY10.", "MMDDYY10.");
    private static final List<String> COLON_COLUMN_LABELS = Arrays.asList("Sex", "Age at diagnosis",
            "Clinical stage at diagnosis", "Month of diagnosis", "Year of diagnosis", "Survival time in months",
            "Survival time in years", "Vital status at last contact", "Anatomical subsite of tumour",
            "Year of diagnosis 1985-94", "Age in 4 categories", "Date of diagnosis", "Date of exit");
    private static final List<Class<Number>> COLON_COLUMN_TYPES = new ArrayList<Class<Number>>() {{
        add(Number.class);
        add(Number.class);
        add(Number.class);
        add(Number.class);
        add(Number.class);
        add(Number.class);
        add(Number.class);
        add(Number.class);
        add(Number.class);
        add(Number.class);
        add(Number.class);
        add(Number.class);
        add(Number.class);
    }};
    private static final List<Integer> COLON_COLUMN_LENGTHS = Arrays.asList(8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8);
    private static final SasFileProperties COLON_SAS_FILE_PROPERTIES = new SasFileProperties();
    private static final int COMPARE_ROWS_COUNT = 300;
    public static final String COLON_SAS7BDAT_URL = "https://biostat3.net/download/sas/colon.sas7bdat";
    public static final Locale CSV_DATA_WRITER_LOCALE = Locale.US;

    static {
        COLON_SAS_FILE_PROPERTIES.setU64(false);
        COLON_SAS_FILE_PROPERTIES.setCompressionMethod(null);
        COLON_SAS_FILE_PROPERTIES.setEndianness(1);
        COLON_SAS_FILE_PROPERTIES.setEncoding(null);
        COLON_SAS_FILE_PROPERTIES.setName("colon");
        COLON_SAS_FILE_PROPERTIES.setFileType("DATA");
        COLON_SAS_FILE_PROPERTIES.setFileLabel("");
        COLON_SAS_FILE_PROPERTIES.setDateCreated(new Date(854409600000L));
        COLON_SAS_FILE_PROPERTIES.setDateModified(new Date(854409600000L));
        COLON_SAS_FILE_PROPERTIES.setSasRelease("7.00.00B");
        COLON_SAS_FILE_PROPERTIES.setServerType("WIN_95");
        COLON_SAS_FILE_PROPERTIES.setOsName("WIN");
        COLON_SAS_FILE_PROPERTIES.setOsType("");
        COLON_SAS_FILE_PROPERTIES.setHeaderLength(1024);
        COLON_SAS_FILE_PROPERTIES.setPageLength(262144);
        COLON_SAS_FILE_PROPERTIES.setPageCount(7);
        COLON_SAS_FILE_PROPERTIES.setRowCount(15564);
        COLON_SAS_FILE_PROPERTIES.setRowLength(104);
        COLON_SAS_FILE_PROPERTIES.setMixPageRowCount(2493);
        COLON_SAS_FILE_PROPERTIES.setColumnsCount(13);
    }

    private String fileName = DEFAULT_FILE_NAME;

    @Test
    public void testColumns() {

        long programStart = System.currentTimeMillis();
        List<Column> columns;
        try (InputStream is = new URL(COLON_SAS7BDAT_URL).openStream()) {

            SasFileReader sasFileReader = new SasFileReaderImpl(is);
            columns = sasFileReader.getColumns();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return;
        }

        logger.info("Processing file {}", fileName);
        for (int i = 0; i < columns.size(); i++) {
            assertThat(columns.get(i).getId()).isEqualTo(COLON_COLUMN_IDS.get(i));
            assertThat(columns.get(i).getName()).isEqualTo(COLON_COLUMN_NAMES.get(i));
            assertThat(columns.get(i).getLabel()).isEqualTo(COLON_COLUMN_LABELS.get(i));
            assertThat(columns.get(i).getFormat().toString()).isEqualTo(COLON_COLUMN_FORMATS.get(i));
            assertThat(columns.get(i).getType()).isEqualTo(COLON_COLUMN_TYPES.get(i));
            assertThat(columns.get(i).getLength()).isEqualTo(COLON_COLUMN_LENGTHS.get(i));
        }
        logger.info("Time passed: {} ms", System.currentTimeMillis() - programStart);
    }

    @Test
    public void testMetadata() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));

        long programStart = System.currentTimeMillis();
        logger.info("Processing file {}", fileName);
        CSVReader controlReader = null;

        try (InputStream fileInputStream = getResourceAsStream(fileName);
             Writer writer = new StringWriter()) {
            SasFileReader sasFileReader = new SasFileReaderImpl(fileInputStream);
            controlReader = new CSVReader(new InputStreamReader(getResourceAsStream(
                fileName.replace(".sas7bdat", "").replace("sas7bdat", "csv") + "_meta.csv")));
            CSVMetadataWriter csvMetadataWriter = new CSVMetadataWriterImpl(writer);
            csvMetadataWriter.writeMetadata(sasFileReader.getColumns());
            csvMetadataWriter.writeSasFileProperties(sasFileReader.getSasFileProperties());
            compareResultWithControl(controlReader, writer);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("Time passed: {} ms", System.currentTimeMillis() - programStart);
    }

    @Test
    public void testData() {
        long programStart = System.currentTimeMillis();

        logger.info("Processing file {}", fileName);

        try (InputStream fileInputStream = getResourceAsStream(fileName);
             StringWriter writer = new StringWriter();
             InputStreamReader inputStreamReader = new InputStreamReader(
                 getResourceAsStream(fileName.toLowerCase().replace("sas7bdat", "csv")))) {
            SasFileReader sasFileReader = new SasFileReaderImpl(fileInputStream);
            long rowCount = sasFileReader.getSasFileProperties().getRowCount();
            List<Column> columns = sasFileReader.getColumns();
            CSVReader controlReader = new CSVReader(inputStreamReader);
            CSVDataWriter csvDataWriter = new CSVDataWriterImpl(writer, ",", "\n", CSV_DATA_WRITER_LOCALE);
            controlReader.readNext();
            for (int i = 0; i < rowCount; i++) {
                csvDataWriter.writeRow(sasFileReader.getColumns(), sasFileReader.readNext());
                if (i != 0 && i % COMPARE_ROWS_COUNT == 0) {
                    compareResultWithControl(controlReader, writer, i - COMPARE_ROWS_COUNT, columns);
                    writer.getBuffer().setLength(0);
                }
            }
            compareResultWithControl(controlReader, writer, (int) (rowCount - rowCount % COMPARE_ROWS_COUNT), columns);
            assertThat(controlReader.readNext()).isNull();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("Time passed: {} ms", System.currentTimeMillis() - programStart);
    }

    @Test
    public void testRowsCount() {

        Object[][] data;
        try (InputStream is = new URL(COLON_SAS7BDAT_URL).openStream()) {
            SasFileReader reader = new SasFileReaderImpl(is);
            data = reader.readAll();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return;
        }
        assertThat(data).hasDimensions(15564, 13);
        assertThat(data[0][1]).isEqualTo(77L);
        assertThat(data[15563][1]).isEqualTo(84L);
    }

    @Test
    public void testStringValue() throws IOException {

        try (InputStream is = getResourceAsStream("sas7bdat/mixed_data_one.sas7bdat")) {
            SasFileReader reader = new SasFileReaderImpl(is);
            Object[] data = reader.readNext();
            assertThat(data[2]).isEqualTo("AAAAAAAA");
        }
    }

    @Test
    public void testSasFileProperties() throws IOException {

        long programStart = System.currentTimeMillis();

        SasFileProperties sasFileProperties;
        try (InputStream is = new URL(COLON_SAS7BDAT_URL).openStream()) {

            SasFileReader sasFileReader = new SasFileReaderImpl(is);

            sasFileProperties = sasFileReader.getSasFileProperties();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return;
        }

        assertThat(sasFileProperties.isU64()).isEqualTo(COLON_SAS_FILE_PROPERTIES.isU64());
        assertThat(sasFileProperties.isCompressed()).isEqualTo(COLON_SAS_FILE_PROPERTIES.isCompressed());
        assertThat(sasFileProperties.getEndianness()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getEndianness());
        assertThat(sasFileProperties.getEncoding()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getEncoding());
        assertThat(sasFileProperties.getName()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getName());
        assertThat(sasFileProperties.getFileType()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getFileType());
        assertThat(sasFileProperties.getFileLabel()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getFileLabel());
        assertThat(sasFileProperties.getDateCreated()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getDateCreated());
        assertThat(sasFileProperties.getDateModified()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getDateModified());
        assertThat(sasFileProperties.getSasRelease()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getSasRelease());
        assertThat(sasFileProperties.getServerType()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getServerType());
        assertThat(sasFileProperties.getOsName()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getOsName());
        assertThat(sasFileProperties.getOsType()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getOsType());
        assertThat(sasFileProperties.getHeaderLength()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getHeaderLength());
        assertThat(sasFileProperties.getPageLength()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getPageLength());
        assertThat(sasFileProperties.getPageCount()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getPageCount());
        assertThat(sasFileProperties.getRowLength()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getRowLength());
        assertThat(sasFileProperties.getRowCount()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getRowCount());
        assertThat(sasFileProperties.getMixPageRowCount()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getMixPageRowCount());
        assertThat(sasFileProperties.getColumnsCount()).isEqualTo(COLON_SAS_FILE_PROPERTIES.getColumnsCount());

        logger.info("Time passed: {} ms", System.currentTimeMillis() - programStart);
    }

    @Test
    public void testInputStream() throws IOException {
        String fileName = getClass().getClassLoader().getResource("sas7bdat/mixed_data_one.sas7bdat").getFile();
        try (ZeroAvailableBytesInputStream is = new ZeroAvailableBytesInputStream(fileName)) {
            SasFileReader reader = new SasFileReaderImpl(is);
            Object[][] data = reader.readAll();
            assertThat(data[0][2]).isEqualTo("AAAAAAAA");
            assertThat(data.length).isEqualTo(24);
        }
    }

    @Test
    public void testPartialReadingOfColumns() {
        long programStart = System.currentTimeMillis();
        logger.info("Processing file {}", fileName);

        List<String> columnNames = new ArrayList<String>() {{
            add("x1");
            add("x5");
            add("x8");
        }};

        try (InputStream fileInputStream = getResourceAsStream(fileName);
             Writer writer = new StringWriter();
             InputStreamReader inputStreamReader = new InputStreamReader(
                 getResourceAsStream(fileName.toLowerCase().replace("sas7bdat", "csv")))) {
            SasFileReader sasFileReader = new SasFileReaderImpl(fileInputStream);
            long rowCount = sasFileReader.getSasFileProperties().getRowCount();
            CSVReader controlReader = new CSVReader(inputStreamReader);
            CSVDataWriter csvDataWriter = new CSVDataWriterImpl(writer, ",", "\n", CSV_DATA_WRITER_LOCALE);
            controlReader.readNext();
            for (int i = 0; i < rowCount; i++) {
                csvDataWriter.writeRow(sasFileReader.getColumns(columnNames), sasFileReader.readNext(columnNames));
            }
            CSVReader resultReader = new CSVReader(new StringReader(writer.toString()));
            for (int i = 0; i < rowCount; i++) {
                String[] controlRow = controlReader.readNext();
                String[] resultRow = resultReader.readNext();
                assertThat(resultRow.length).isEqualTo(columnNames.size());
                assertThat(resultRow[0]).isEqualTo(controlRow[0]);
                assertThat(resultRow[1]).isEqualTo(controlRow[4]);
                assertThat(resultRow[2]).isEqualTo(controlRow[7]);
            }
            assertThat(controlReader.readNext()).isNull();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("Time passed: {} ms", System.currentTimeMillis() - programStart);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private static class ZeroAvailableBytesInputStream extends FileInputStream {

        public ZeroAvailableBytesInputStream(String name) throws FileNotFoundException {
            super(name);
        }

        @Override
        public int available() throws IOException {
            return 0;
        }
    }
}
