/**
 * *************************************************************************
 * Copyright (C) 2015 EPAM

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * *************************************************************************
 */

package com.epam.parso.impl;

import com.epam.parso.Column;
import com.epam.parso.SasFileProperties;
import com.epam.parso.SasFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.epam.parso.impl.ParserMessageConstants.UNKNOWN_COLUMN_NAME;

/**
 * A class to read sas7bdat files transferred to the input stream and then to get metadata and file data.
 * This class is used as a wrapper for SasFileParser.
 */
public class SasFileReaderImpl implements SasFileReader {
    /**
     * Object for writing logs.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SasFileReaderImpl.class);

    /**
     * Object for parsing sas7bdat file.
     */
    private final SasFileParser sasFileParser;

    /**
     * List of columns obtained by names.
     */
    private final List<Column> columnsByName = new ArrayList<Column>();

    /**
     * Builds an object of the SasFileReaderImpl class from the file contained in the input stream.
     * Reads only metadata (properties and column information) of the sas7bdat file.
     *
     * @param inputStream - an input stream which should contain a correct sas7bdat file.
     */
    public SasFileReaderImpl(InputStream inputStream) {
        sasFileParser = new SasFileParser.Builder().sasFileStream(inputStream).build();
    }

    /**
     * Builds an object of the SasFileReaderImpl class from the file contained in the input stream with the encoding
     * defined in the 'encoding' variable.
     * Reads only metadata (properties and column information) of the sas7bdat file.
     *
     * @param inputStream - an input stream which should contain a correct sas7bdat file.
     * @param encoding    - the string containing the encoding to use in strings output
     */
    public SasFileReaderImpl(InputStream inputStream, String encoding) {
        sasFileParser = new SasFileParser.Builder().sasFileStream(inputStream).encoding(encoding).build();
    }

    /**
     * Builds an object of the SasFileReaderImpl class from the file contained in the input stream with a flag of
     * the binary or string format of the data output.
     * Reads only metadata (properties and column information) of the sas7bdat file.
     *
     * @param inputStream - an input stream which should contain a correct sas7bdat file.
     * @param byteOutput  - the flag of data output in binary or string format
     */
    public SasFileReaderImpl(InputStream inputStream, Boolean byteOutput) {
        sasFileParser = new SasFileParser.Builder().sasFileStream(inputStream).byteOutput(byteOutput).build();
    }

    /**
     * The function to get the {@link Column} list from {@link SasFileParser}.
     *
     * @return a list of columns.
     */
    @Override
    public List<Column> getColumns() {
        return sasFileParser.getColumns();
    }

    /**
     * The function to get the {@link Column} list from {@link SasFileReader}
     * according to the columnNames.
     *
     * @param columnNames - list of column names that should be returned.
     * @return a list of columns.
     */
    @Override
    public List<Column> getColumns(List<String> columnNames) {
        if (columnsByName.isEmpty()) {
            Map<String, Column> columnsMap = new HashMap<String, Column>();
            List<Column> allColumns = sasFileParser.getColumns();
            for (Column column : allColumns) {
                columnsMap.put(column.getName(), column);
            }
            for (String name : columnNames) {
                if (columnsMap.containsKey(name)) {
                    columnsByName.add(columnsMap.get(name));
                } else {
                    throw new NoSuchElementException(UNKNOWN_COLUMN_NAME);
                }
            }
        }
        return columnsByName;
    }

    /**
     * Reads all rows from the sas7bdat file. For each row, only the columns defined in the list are read.
     *
     * @param columnNames list of column names which should be processed.
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    @Override
    public Object[][] readAll(List<String> columnNames) {
        int rowNum = (int) getSasFileProperties().getRowCount();
        Object[][] result = new Object[rowNum][];
        for (int i = 0; i < rowNum; i++) {
            try {
                result[i] = readNext(columnNames);
            } catch (IOException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("I/O exception, skipping the rest of the file. "
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
    public Object[][] readAll() {
        return readAll(null);
    }

    /**
     * Reads all rows from the sas7bdat file.
     *
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    @Override
    public Object[] readNext() throws IOException {
        return sasFileParser.readNext(null);
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
        return sasFileParser.readNext(columnNames);
    }

    /**
     * The function to get sas file properties.
     *
     * @return the object of the {@link SasFileProperties} class that stores file metadata.
     */
    @Override
    public SasFileProperties getSasFileProperties() {
        return sasFileParser.getSasFileProperties();
    }
}
