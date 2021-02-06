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

package com.epam.parso.xport;

import static com.epam.parso.TestUtils.compareResultWithControl;
import static com.epam.parso.TestUtils.getResourceAsFile;
import static com.epam.parso.TestUtils.getResourceAsStream;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import au.com.bytecode.opencsv.CSVReader;
import com.epam.parso.CSVDataWriter;
import com.epam.parso.CSVMetadataWriter;
import com.epam.parso.Column;
import com.epam.parso.impl.CSVDataWriterImpl;
import com.epam.parso.impl.CSVMetadataWriterImpl;
import com.epam.parso.xport.impl.XportFileReaderImpl;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import lombok.Setter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XportFileReaderTest {
    private static final Logger logger = LoggerFactory.getLogger(XportFileReaderTest.class);

    private static final XportDatasetProperties AE_XPORT_DATASET_PROPERTIES = XportDatasetProperties.builder()
        .datasetName("AE")
        .datasetLabel("")
        .datasetType("")
        .datasetIndex(0)
        .sasOs("R 3.4.0")
        .sasVersion("7.00")
        .dateCreated("06SEP17:20:23:53")
        .dateModified("06SEP17:20:23:53")
        .columnsCount(37)
        .dataOffset(5920)
        .rowLength(487)
        .rowCount(961)
        .namestrLength(140)
        .variableProperties(singletonList(XportVariableProperties.builder()
            .type(XportVariableProperties.VariableType.CHAR)
            .name("STUDYID")
            .longName("STUDYID")
            .label("Study Identifier")
            .longLabel("Study Identifier")
            .varnum(1)
            .variableLength(12)
            .formatName("")
            .inputFormatName("")
            .build()))
        .build();
    private static final XportFileProperties AE_XPORT_FILE_PROPERTIES = XportFileProperties.builder()
        .sasOs("R 3.4.0")
        .sasVersion("7.00")
        .dateCreated("06SEP17:20:23:53")
        .dateModified("06SEP17:20:23:53")
        .datasetProperties(singletonList(AE_XPORT_DATASET_PROPERTIES))
        .build();

    public static final Locale CSV_DATA_WRITER_LOCALE = Locale.US;
    private static final int COMPARE_ROWS_COUNT = 300;

    static {
        AE_XPORT_FILE_PROPERTIES.setSasOs("R 3.4.0");
        AE_XPORT_FILE_PROPERTIES.setSasVersion("7.00");
        AE_XPORT_FILE_PROPERTIES.setDateCreated("06SEP17:20:23:53");
        AE_XPORT_FILE_PROPERTIES.setDateModified("06SEP17:20:23:53");
    }

    @Setter
    private String fileName = "xport/v5_6/input/ae.xpt";
    
    private final String fileNameSeveralDatasets = "xport/v5_6/input/air_class.xpt";

    @Test
    public void testMetadata() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        File inputFile = getResourceAsFile(fileName);
        long programStart = System.currentTimeMillis();

        logger.info("Processing file {}", fileName);

        try (XportFileReader xportFileReader = new XportFileReaderImpl(inputFile);
             Writer writer = new StringWriter();
             CSVReader controlReader = new CSVReader(new InputStreamReader(getResourceAsStream(
                 fileName.replace(".xpt", "")
                     .replace("input", "output") + "_meta.csv")))) {

            CSVMetadataWriter csvMetadataWriter = new CSVMetadataWriterImpl(writer);
            csvMetadataWriter.writeMetadata(xportFileReader.getCurrentDatasetMetadata().getColumns());
            csvMetadataWriter.writeXportFileProperties(xportFileReader.getXportFileProperties());
            compareResultWithControl(controlReader, writer);
        }
        logger.info("Time passed: {} ms", System.currentTimeMillis() - programStart);
    }

    @Test
    public void testReadDatasetRowByRow() throws Exception {
        long programStart = System.currentTimeMillis();
        File inputFile = getResourceAsFile(fileName);
        logger.info("Processing file {}", fileName);

        try (XportFileReader xportFileReader = new XportFileReaderImpl(inputFile);
             StringWriter writer = new StringWriter();
             CSVReader controlReader = new CSVReader(new InputStreamReader(getResourceAsStream(
                 fileName.replace("xpt", "csv")
                     .replace("input", "output"))))) {

            long rowCount = xportFileReader.getCurrentDatasetMetadata().getRowCount();
            List<Column> columns = xportFileReader.getCurrentDatasetMetadata().getColumns();

            CSVDataWriter csvDataWriter = new CSVDataWriterImpl(writer, ",", "\n", CSV_DATA_WRITER_LOCALE);
            controlReader.readNext();
            for (int i = 0; i < rowCount; i++) {
                csvDataWriter.writeRow(columns, xportFileReader.readNext());
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
    public void testReadNext() {
        File inputFile = getResourceAsFile(fileName);

        try (XportFileReader xportFileReader = new XportFileReaderImpl(inputFile)) {
            Object[] firstRow = xportFileReader.readNext();
            assertThat(xportFileReader.getOffset()).isEqualTo(1);
            assertThat(firstRow).isEqualTo(new Object[] {"CDISCPILOT01", "AE", "01-701-1015", 1L, "E07",
                "APPLICATION SITE ERYTHEMA", "APPLICATION SITE REDNESS", null, "APPLICATION SITE ERYTHEMA", null,
                "HLT_0617", null, "HLGT_0152", null, "GENERAL DISORDERS AND ADMINISTRATION SITE CONDITIONS", null,
                "GENERAL DISORDERS AND ADMINISTRATION SITE CONDITIONS", null, "MILD", "N",
                "", "PROBABLE", "NOT RECOVERED/NOT RESOLVED", "N", "N", "N", "N", "N", "N", "N", "TREATMENT",
                "2014-01-16", "2014-01-03", "", 15L, 2L, null});
            Object[] secondRow = xportFileReader.readNext();
            assertThat(xportFileReader.getOffset()).isEqualTo(2);
            assertThat(secondRow).isEqualTo(
                new Object[] {"CDISCPILOT01", "AE", "01-701-1015", 2L, "E08", "APPLICATION SITE PRURITUS",
                    "APPLICATION SITE ITCHING", null, "APPLICATION SITE PRURITUS", null, "HLT_0317", null, "HLGT_0338",
                    null, "GENERAL DISORDERS AND ADMINISTRATION SITE CONDITIONS", null,
                    "GENERAL DISORDERS AND ADMINISTRATION SITE CONDITIONS", null, "MILD", "N", "", "PROBABLE",
                    "NOT RECOVERED/NOT RESOLVED", "N", "N", "N", "N", "N", "N", "N", "TREATMENT", "2014-01-16",
                    "2014-01-03", "", 15L, 2L, null});

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void testReadAll() throws Exception {
        File inputFile = getResourceAsFile(fileName);

        try (XportFileReader xportFileReader = new XportFileReaderImpl(inputFile)) {
            List<XportDataset> xportDatasets = xportFileReader.readAll();
            assertThat(xportDatasets).hasSize(1);
            XportDataset dataset = xportDatasets.get(0);
            assertThat(dataset.getData().length)
                .isEqualTo(dataset.getMetadata().getRowCount());
            assertThat(dataset.getData()[0].length)
                .isEqualTo(dataset.getMetadata().getColumnsCount());
        }
    }

    @Test
    public void testReadAllInDatasets() throws Exception {
        File inputFile = getResourceAsFile(fileNameSeveralDatasets);

        try (XportFileReader xportFileReader = new XportFileReaderImpl(inputFile)) {
            List<XportDataset> xportDatasets = xportFileReader.readAllInDatasets(Collections.singletonList("CLASS1"));
            assertThat(xportDatasets).hasSize(1);
            XportDataset dataset = xportDatasets.get(0);
            assertThat(dataset.getMetadata().getDatasetName()).isEqualTo("CLASS1");
            assertThat(dataset.getData().length)
                    .isEqualTo(dataset.getMetadata().getRowCount());
            assertThat(dataset.getData()[0].length)
                    .isEqualTo(dataset.getMetadata().getColumnsCount());
        } 
    }

    @Test
    public void testReadAll_twoDatasets() throws Exception {
        File inputFile = getResourceAsFile(fileNameSeveralDatasets);

        try (XportFileReader xportFileReader = new XportFileReaderImpl(inputFile)) {
            List<XportDataset> xportDatasets = xportFileReader.readAll();
            assertThat(xportDatasets).hasSize(2);
            XportDataset firstDataset = xportDatasets.get(0);
            // fixme: "SAS Universal Viewer" returns number of rows equal to 144. There are 144 rows with data in the
            //  input file, but there is one more empty row before the start of the next dataset. Consider whether it
            //  needs to be truncated by some extra logic.
            assertThat(firstDataset.getMetadata())
                .extracting(XportDatasetProperties::getDatasetIndex, XportDatasetProperties::getDatasetName,
                    XportDatasetProperties::getColumnsCount, XportDatasetProperties::getRowCount)
                .containsExactly(0, "AIR", 2, 145L);
            assertThat(firstDataset.getData().length)
                .isEqualTo(firstDataset.getMetadata().getRowCount());
            assertThat(firstDataset.getData()[0].length)
                .isEqualTo(firstDataset.getMetadata().getColumnsCount());
            XportDataset secondDataset = xportDatasets.get(1);
            assertThat(secondDataset.getMetadata())
                .extracting(XportDatasetProperties::getDatasetIndex, XportDatasetProperties::getDatasetName,
                    XportDatasetProperties::getColumnsCount, XportDatasetProperties::getRowCount)
                .containsExactly(1, "CLASS1", 5, 19L);
            assertThat(secondDataset.getData().length)
                .isEqualTo(secondDataset.getMetadata().getRowCount());
            assertThat(secondDataset.getData()[0].length)
                .isEqualTo(secondDataset.getMetadata().getColumnsCount());
        }
    }

    @Test
    public void testReadAll_partialReadingOfColumns() throws Exception {
        File inputFile = getResourceAsFile(fileNameSeveralDatasets);

        try (XportFileReader xportFileReader = new XportFileReaderImpl(inputFile)) {
            Map<String, List<String>> datasetNameToListOfColumns = new HashMap<>();
            datasetNameToListOfColumns.put("CLASS1", Arrays.asList("NAME", "AGE"));
            List<XportDataset> xportDatasets = xportFileReader.readAll(datasetNameToListOfColumns);
            assertThat(xportDatasets).hasSize(1);
            XportDataset dataset = xportDatasets.get(0);
            assertThat(dataset.getMetadata().getDatasetName()).isEqualTo("CLASS1");
            assertThat(dataset.getData().length)
                    .isEqualTo(dataset.getMetadata().getRowCount());
            Object[] firstRow = dataset.getData()[0];
            assertThat(firstRow.length).isEqualTo(2);
            assertThat(firstRow[0]).isEqualTo("Alfred");
            assertThat(firstRow[1]).isEqualTo(14L);
        }
    }

    @Test
    public void testXportFileProperties() {

        File inputFile = getResourceAsFile(fileName);

        long programStart = System.currentTimeMillis();
        try (XportFileReader xportFileReader = new XportFileReaderImpl(inputFile)) {

            XportFileProperties xportFileProperties = xportFileReader.getXportFileProperties();

            assertThat(xportFileProperties).usingRecursiveComparison()
                .ignoringFields("datasetProperties").isEqualTo(AE_XPORT_FILE_PROPERTIES);
            assertThat(xportFileProperties.getDatasetProperties()).hasSize(1);

            XportDatasetProperties datasetProperties = xportFileProperties.getDatasetProperties().get(0);
            assertThat(datasetProperties).usingRecursiveComparison()
                .ignoringFields("variableProperties").isEqualTo(AE_XPORT_DATASET_PROPERTIES);
            assertThat(datasetProperties.getVariableProperties()).hasSize(37);
            assertThat(datasetProperties.getVariableProperties().get(0)).usingRecursiveComparison()
                .isEqualTo(AE_XPORT_DATASET_PROPERTIES.getVariableProperties().get(0));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("Time passed: {} ms", System.currentTimeMillis() - programStart);
    }

    @Test
    public void testPartialReadingOfColumns() throws Exception {
        long programStart = System.currentTimeMillis();
        File inputFile = getResourceAsFile(fileName);
        logger.info("Processing file {}", fileName);

        List<String> columnNames = Arrays.asList("AETERM", "AEHLT", "AEDY");

        try (XportFileReader xportFileReader = new XportFileReaderImpl(inputFile);
             Writer writer = new StringWriter();
             CSVReader controlReader = new CSVReader(new InputStreamReader(getResourceAsStream(
                 fileName.replace("xpt", "csv")
                     .replace("input", "output"))))) {

            long rowCount = xportFileReader.getCurrentDatasetMetadata().getRowCount();
            CSVDataWriter csvDataWriter = new CSVDataWriterImpl(writer, ",", "\n", CSV_DATA_WRITER_LOCALE);
            controlReader.readNext();
            for (int i = 0; i < rowCount; i++) {
                csvDataWriter.writeRow(xportFileReader.getCurrentDatasetMetadata().getColumns(columnNames),
                    xportFileReader.readNext(columnNames));
            }
            CSVReader resultReader = new CSVReader(new StringReader(writer.toString()));
            for (int i = 0; i < rowCount; i++) {
                String[] controlRow = controlReader.readNext();
                String[] resultRow = resultReader.readNext();
                assertThat(resultRow.length).isEqualTo(columnNames.size());
                assertThat(resultRow[0]).isEqualTo(controlRow[5]);
                assertThat(resultRow[1]).isEqualTo(controlRow[10]);
                assertThat(resultRow[2]).isEqualTo(controlRow[34]);
            }
            assertThat(controlReader.readNext()).isNull();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("Time passed: {} ms", System.currentTimeMillis() - programStart);
    }
}
