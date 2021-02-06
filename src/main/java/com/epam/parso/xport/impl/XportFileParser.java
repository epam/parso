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

package com.epam.parso.xport.impl;

import com.epam.parso.xport.XportDatasetProperties;
import com.epam.parso.xport.XportFileProperties;
import com.epam.parso.xport.XportVariableProperties;
import com.epam.parso.xport.XportVersion;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.epam.parso.common.BytesHelper.*;

/**
 * This is a class that parses SAS XPORT (.xpt) files. When parsing an XPORT file, to interact with the library
 * use {@link XportFileReaderImpl} which is a wrapper for XportFileParser. Despite this, XportFileParser
 * is publicly available, it can be instanced a constructor and used directly.
 */
@Slf4j
public final class XportFileParser implements AutoCloseable {

    /**
     * SAS transport file (XPORT) format version.
     */
    @Getter
    private final XportVersion version;

    /**
     * Byte buffer used for skip operations.
     * Actually the data containing in this buffer is ignored,
     * because it only used for dummy reads.
     */
    private static final byte[] SKIP_BYTE_BUFFER = new byte[4096];
    /**
     * The variable to store all the properties from the XPORT file.
     */
    @Getter
    private final XportFileProperties xportFileProperties = new XportFileProperties();
    /**
     * The index of the current byte when reading the file.
     */
    private long currentFilePosition;

    /**
     * The input stream through which the XPORT is read.
     */
    private final BufferedInputStream xportFileStream;

    /**
     * The index of the current dataset when reading the file.
     */
    private int currentDatasetIndex;

    /**
     * The index of the current row in the dataset when reading the file.
     */
    private int currentRowInDatasetIndex;
    /**
     * Last read row from XPORT file.
     */
    private Object[] currentRow;

    /**
     * The constructor that reads metadata from the XPORT, parses it and puts the results in
     * {@link XportFileParser#xportFileProperties}.
     *
     * @param xportFile XPORT file
     * @param version XPORT format version (5 or 8)
     */
    @SneakyThrows
    public XportFileParser(File xportFile, XportVersion version) {

        this.xportFileStream = new BufferedInputStream(new FileInputStream(xportFile));
        this.version = version;
        XportMetadataParser metadataParser = XportMetadataParser.getInstance(xportFile, xportFileProperties, version);
        try {
            getMetadataFromXportFile(metadataParser);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Constructor to instantiate the parser with default version (Version 5).
     * @param xportFile source file.
     */
    public XportFileParser(File xportFile) {
        this(xportFile, XportVersion.VERSION_5);
    }

    /**
     * Get dataset (member) metadata.
     * @param datasetName dataset name.
     * @return metadata of specified dataset.
     */
    public XportDatasetProperties getDatasetMetadata(String datasetName) {
        return xportFileProperties.getDatasetProperties().stream()
            .filter(ds -> ds.getDatasetName().equals(datasetName))
            .findFirst().orElse(null);
    }

    /**
     * The method that reads and parses metadata from the XPORT and puts the results in
     * {@link XportFileParser#xportFileProperties}.
     *
     * @throws IOException - appears if reading from the source file is impossible.
     * @param metadataParser XPORT metadata parser.
     */
    private void getMetadataFromXportFile(XportMetadataParser metadataParser) throws IOException {

        currentFilePosition = metadataParser.populateMetadataFromXportFile();
        skipBytes(xportFileStream, SKIP_BYTE_BUFFER, currentFilePosition);

        if (this.xportFileProperties.getDatasetProperties().size() > 0) {
            resetPositionToStartOfDataset(0);
        }
    }

    /**
     * Reset input stream position to the start of the data rows by specified dataset index.
     * @param datasetIndex sequential index of dataset in the file, starting from 0. Dataset index must be not less
     *                     than current dataset index.
     */
    @SneakyThrows
    private void resetPositionToStartOfDataset(int datasetIndex) {
        long dataOffset = xportFileProperties.getDatasetProperties().get(datasetIndex).getDataOffset();
        if (currentDatasetIndex > datasetIndex || dataOffset < currentFilePosition) {
            throw new IllegalArgumentException("Cannot scroll input stream back");
        }
        skipBytes(xportFileStream, SKIP_BYTE_BUFFER, dataOffset - currentFilePosition);
        currentFilePosition = dataOffset;
        currentDatasetIndex = datasetIndex;
        currentRowInDatasetIndex = 0;
    }

     /**
     * Switch to the next XPORT dataset in the same file.
     *
     * @return next (i.e. 'new current') dataset index, -1 if there are no more datasets
     */
    public int nextDataset() {
        int nextDatasetIndex = currentDatasetIndex + 1;
        if (nextDatasetIndex >= xportFileProperties.getDatasetProperties().size()) {
            return -1;
        }
        resetPositionToStartOfDataset(nextDatasetIndex);
        return currentDatasetIndex;
    }

    /**
     * Read next data row in current dataset.
     * @param columnNames - list of column names which should be processed. If null, all columns are returned.
     * @return array of row data.
     */
    @SneakyThrows
    public Object[] readNext(List<String> columnNames) {
        if (currentRowInDatasetIndex++ >= getCurrentDatasetProperties().getRowCount()) {
            return null;
        }

        byte[] source = new byte[getCurrentDatasetProperties().getRowLength()];
        xportFileStream.read(source);
        currentFilePosition += source.length;
        currentRow = processByteArrayWithData(source, columnNames);

        return Arrays.copyOf(currentRow, currentRow.length);
    }

    /**
     * The function to convert the array of bytes that stores the data of a row into an array of objects.
     * Each object corresponds to a table cell.
     *
     * @param source   - the row byte array.
     * @param columnNames - list of column names which should be processed. If null, all columns are returned.
     * @return the array of objects storing the data of the row.
     */
    @SneakyThrows
    private Object[] processByteArrayWithData(byte[] source, List<String> columnNames) {
        Object[] rowElements;
        if (columnNames != null) {
            rowElements = new Object[columnNames.size()];
        } else {
            rowElements = new Object[getCurrentDatasetProperties().getColumnsCount()];
        }

        List<XportVariableProperties> variableProperties = getCurrentDatasetProperties().getVariableProperties();

        for (int currentColumnIndex = 0; currentColumnIndex < getCurrentDatasetProperties().getColumnsCount()
            && variableProperties.get(currentColumnIndex).getVariableLength() != 0;
             currentColumnIndex++) {
            if (columnNames == null) {
                rowElements[currentColumnIndex] = processElement(source, currentColumnIndex);
            } else {
                String name = variableProperties.get(currentColumnIndex).getName();
                if (columnNames.contains(name)) {
                    rowElements[columnNames.indexOf(name)] = processElement(source, currentColumnIndex);
                }
            }
        }
        return rowElements;
    }

    /**
     * The function to process element of row.
     *
     * @param source             an array of bytes containing required data.
     * @param currentColumnIndex index of the current element.
     * @return object storing the data of the element.
     */
    private Object processElement(byte[] source, int currentColumnIndex) {
        XportVariableProperties variableProperties =
            getCurrentDatasetProperties().getVariableProperties().get(currentColumnIndex);
        int length = variableProperties.getVariableLength();

        byte[] temp = Arrays.copyOfRange(source, variableProperties.getVariableOffset(),
            variableProperties.getVariableOffset() + length);

        if (variableProperties.getType() == XportVariableProperties.VariableType.NUMERIC) {
            return convertIbmByteArrayToNumber(temp);
        } else {
            return bytesToString(temp).trim();
        }
    }


    /**
     * The function to get the {@link XportDatasetProperties} from {@link XportFileParser}.
     *
     * @return current dataset properties.
     */
    public XportDatasetProperties getCurrentDatasetProperties() {
        return xportFileProperties.getDatasetProperties().get(currentDatasetIndex);
    }

    /**
     * The function to return the index of the current row in the current dataset when reading the XPORT file.
     *
     * @return current row index (starting from 1)
     */
    public int getOffset() {
        return currentRowInDatasetIndex;
    }

    @Override
    public void close() throws Exception {
        xportFileStream.close();
    }
}
