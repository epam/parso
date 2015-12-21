/**
 * *************************************************************************
 * Copyright (C) 2015 EPAM
 * <p>
 * This file is part of Parso.
 * <p>
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * <p>
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
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
import java.util.List;

/**
 * A class to read sas7bdat files transferred to the input stream and then to get metadata and file data.
 * This class is used as a wrapper for SasFileParser.
 */

public class SasFileReaderImpl implements SasFileReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SasFileReaderImpl.class);

    private final SasFileParser sasFileParser;

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
     * Builds an object of the SasFileReaderImpl class from the file contained in the input stream with the encoding defined
     * in the 'encoding' variable.
     * Reads only metadata (properties and column information) of the sas7bdat file.
     *
     * @param inputStream - an input stream which should contain a correct sas7bdat file.
     * @param encoding    - the string containing the encoding to use in strings output
     */
    public SasFileReaderImpl(InputStream inputStream, String encoding) {
        sasFileParser = new SasFileParser.Builder().sasFileStream(inputStream).encoding(encoding).build();
    }

    /**
     * Builds an object of the SasFileReaderImpl class from the file contained in the input stream with a flag of the binary
     * or string format of the data output.
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
     * Reads all rows from the sas7bdat file.
     *
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    @Override
    public Object[][] readAll() {
        int rowNum = (int) getSasFileProperties().getRowCount();
        Object[][] result = new Object[rowNum][];
        for (int i = 0; i < rowNum; i++) {
            try {
                result[i] = readNext();
            } catch (IOException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("I/O exception, skipping the rest of the file. " +
                            "Rows read: " + i + ". Expected number of rows from metadata: " + rowNum, e);
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
    public Object[] readNext() throws IOException {
        return sasFileParser.readNext();
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
