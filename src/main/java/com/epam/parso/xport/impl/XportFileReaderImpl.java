package com.epam.parso.xport.impl;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.epam.parso.xport.XportDataset;
import com.epam.parso.xport.XportDatasetProperties;
import com.epam.parso.xport.XportFileProperties;
import com.epam.parso.xport.XportFileReader;
import com.epam.parso.xport.XportVersion;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * A class to read XPORT files and then to get metadata and file data.
 * This class is used as a wrapper for XportFileParser, it provides methods to read XPORT file and its properties.
 * Despite this, {@link XportFileParser} is publicly available, it can be instanced via a constructor
 * and used directly.
 */
@Slf4j
public class XportFileReaderImpl implements XportFileReader {

    /**
     * Object for parsing XPORT file.
     */
    private final XportFileParser parser;

    /**
     * Basic constructor to instantiate the reader.
     * @param xportFile source file.
     * @param version XPORT format version.
     */
    public XportFileReaderImpl(File xportFile, XportVersion version) {
        this.parser = new XportFileParser(xportFile, version);
    }

    /**
     * Constructor to instantiate the reader with default version (Version 5).
     * @param xportFile source file.
     */
    public XportFileReaderImpl(File xportFile) {
        this(xportFile, XportVersion.VERSION_5);
    }

    @Override
    public XportDatasetProperties getCurrentDatasetMetadata() {
        return parser.getCurrentDatasetProperties();
    }

    @Override
    public List<XportDataset> readAll() {
        return readAll(null);
    }

    @Override
    public List<XportDataset> readAllInDatasets(List<String> datasetNames) {
        return readAll(datasetNames.stream()
            .collect(toMap(name -> name, name -> Collections.emptyList())));
    }

    /**
     * Reads all data from the xport file. For each dataset specified, for each row, only the columns defined in the
     * list are read.
     * This method should not be used on the same reader instance in combination with any methods that
     * imply iterative reading (e.g. {@link XportFileReader#readNext()}, {@link XportFileReader#nextDataset()})
     *
     * @param datasetColumnNames map of dataset names to list of column names which should be processed.
     *                           If columns list is empty, all columns are read.
     *                           Note: empty list is used instead of null because not all Map implementations allow
     *                           null values.
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    @Override
    public List<XportDataset> readAll(Map<String, List<String>> datasetColumnNames) {
        // note that dataset properties are in the dame order as datasets appear in the file
        return parser.getXportFileProperties().getDatasetProperties().stream()
            .filter(dataset -> datasetColumnNames == null || datasetColumnNames.containsKey(dataset.getDatasetName()))
            .map(dataset -> {
                while (getCurrentDatasetMetadata().getDatasetIndex() < dataset.getDatasetIndex()) {
                    nextDataset();
                }
                // replace empty list with null for further processing
                List<String> columnNames = Optional.ofNullable(datasetColumnNames)
                    .map(o -> o.get(dataset.getDatasetName()))
                    .filter(list -> !list.isEmpty())
                    .orElse(null);
                Object[][] data = readAllInCurrentDataset(columnNames);
                return new XportDataset(dataset, data);
            })
            .collect(toList());
    }

    /**
     * Reads all rows from the xport file. For each row, only the columns defined in the list are read.
     *
     * @param columnNames list of column names which should be processed.
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    @Override
    public Object[][] readAllInCurrentDataset(List<String> columnNames) {

        int rowNum = (int) parser.getCurrentDatasetProperties().getRowCount();
        Object[][] result = new Object[rowNum][];
        for (int i = 0; i < rowNum; i++) {
            try {
                result[i] = readNext(columnNames);
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn("I/O exception, skipping the rest of the file. "
                        + "Rows read: " + i + ". Expected number of rows from metadata: " + rowNum, e);
                }
                break;
            }
        }
        return result;
    }

    /**
     * Reads all rows from the sas7bdat file.
     *
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    @Override
    public Object[][] readAllInCurrentDataset() {
        return readAllInCurrentDataset(null);
    }

    /**
     * Reads all rows from the sas7bdat file.
     *
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    @Override
    public Object[] readNext() {
        return parser.readNext(null);
    }

    /**
     * Reads all rows from the sas7bdat file. For each row, only the columns defined in the list are read.
     *
     * @param columnNames list of column names which should be processed.
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    @Override
    public Object[] readNext(List<String> columnNames) throws IOException {
        return parser.readNext(columnNames);
    }

    @Override
    public int nextDataset() {
        return parser.nextDataset();
    }


    @Override
    public XportFileProperties getXportFileProperties() {
        return parser.getXportFileProperties();
    }

    @Override
    public Integer getOffset() {
        return parser.getOffset();
    }

    @Override
    public void close() throws Exception {
        parser.close();
    }
}
