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

package com.epam.parso.impl;

import com.epam.parso.CSVMetadataWriter;
import com.epam.parso.Column;
import com.epam.parso.SasFileProperties;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * This is a class to export the sas7bdat file metadata into the CSV format.
 */
public class CSVMetadataWriterImpl extends AbstractCSVWriter implements CSVMetadataWriter {
    /**
     * The id column header for metadata.
     */
    private static final String COLUMN_HEADING_ID = "Number";

    /**
     * The name column header for metadata.
     */
    private static final String COLUMN_HEADING_NAME = "Name";

    /**
     * The type column header for metadata.
     */
    private static final String COLUMN_HEADING_TYPE = "Type";

    /**
     * The data length column header for metadata.
     */
    private static final String COLUMN_HEADING_DATA_LENGTH = "Data length";

    /**
     * The format column header for metadata.
     */
    private static final String COLUMN_HEADING_FORMAT = "Format";

    /**
     * The label column header for metadata.
     */
    private static final String COLUMN_HEADING_LABEL = "Label";

    /**
     * Constant containing Number type name.
     */
    private static final String JAVA_NUMBER_CLASS_NAME = "java.lang.Number";

    /**
     * Constant containing String type name.
     */
    private static final String JAVA_STRING_CLASS_NAME = "java.lang.String";

    /**
     * Representation of Number type in metadata.
     */
    private static final String OUTPUT_NUMBER_TYPE_NAME = "Numeric";

    /**
     * Representation of String type in metadata.
     */
    private static final String OUTPUT_STRING_TYPE_NAME = "Character";

    /**
     * The constructor that defines writer variable to output result csv file.
     *
     * @param writer the writer which is used to output csv file.
     */
    public CSVMetadataWriterImpl(Writer writer) {
        super(writer);
    }

    /**
     * The constructor that defines writer variable to output result csv file with selected delimiter.
     *
     * @param writer    the writer which is used to output csv file.
     * @param delimiter separator used in csv file.
     */
    public CSVMetadataWriterImpl(Writer writer, String delimiter) {
        super(writer, delimiter);
    }

    /**
     * The constructor that defines writer variable to output result csv file with selected delimiter and endline.
     *
     * @param writer    the writer which is used to output csv file.
     * @param delimiter separator used in csv file.
     * @param endline   symbols used in csv file as endline.
     */
    public CSVMetadataWriterImpl(Writer writer, String delimiter, String endline) {
        super(writer, delimiter, endline);
    }

    /**
     * The method to export a parsed sas7bdat file metadata (stored as an object of the {@link SasFileReaderImpl} class)
     * using {@link CSVMetadataWriterImpl#writer}.
     *
     * @param columns the {@link Column} class variables list that stores columns description from the sas7bdat file.
     * @throws java.io.IOException appears if the output into writer is impossible.
     */
    @Override
    public void writeMetadata(List<Column> columns) throws IOException {
        Writer writer = getWriter();
        String delimiter = getDelimiter();
        String endline = getEndline();

        writer.write(COLUMN_HEADING_ID);
        writer.write(delimiter);
        writer.write(COLUMN_HEADING_NAME);
        writer.write(delimiter);
        writer.write(COLUMN_HEADING_TYPE);
        writer.write(delimiter);
        writer.write(COLUMN_HEADING_DATA_LENGTH);
        writer.write(delimiter);
        writer.write(COLUMN_HEADING_FORMAT);
        writer.write(delimiter);
        writer.write(COLUMN_HEADING_LABEL);
        writer.write(endline);
        for (Column column : columns) {
            writer.write(String.valueOf(column.getId()));
            writer.write(delimiter);
            checkSurroundByQuotesAndWrite(writer, delimiter, column.getName());
            writer.write(delimiter);
            writer.write(column.getType().getName().replace(JAVA_NUMBER_CLASS_NAME, OUTPUT_NUMBER_TYPE_NAME).replace(
                    JAVA_STRING_CLASS_NAME, OUTPUT_STRING_TYPE_NAME));
            writer.write(delimiter);
            writer.write(String.valueOf(column.getLength()));
            writer.write(delimiter);
            if (!column.getFormat().isEmpty()) {
                checkSurroundByQuotesAndWrite(writer, delimiter, column.getFormat().toString());
            }
            writer.write(delimiter);
            checkSurroundByQuotesAndWrite(writer, delimiter, column.getLabel());
            writer.write(endline);
        }
        writer.flush();
    }

    /**
     * The method to output the sas7bdat file properties.
     *
     * @param sasFileProperties the variable with sas file properties data.
     * @throws IOException appears if the output into writer is impossible.
     */
    @Override
    public void writeSasFileProperties(SasFileProperties sasFileProperties) throws IOException {
        constructPropertiesString("Bitness: ", sasFileProperties.isU64() ? "x64" : "x86");
        constructPropertiesString("Compressed: ", sasFileProperties.getCompressionMethod());
        constructPropertiesString("Endianness: ", sasFileProperties.getEndianness() == 1 ? "LITTLE_ENDIANNESS"
                : "BIG_ENDIANNESS");
        constructPropertiesString("Encoding: ", sasFileProperties.getEncoding());
        constructPropertiesString("Name: ", sasFileProperties.getName());
        constructPropertiesString("File type: ", sasFileProperties.getFileType());
        constructPropertiesString("File label: ", sasFileProperties.getFileLabel());
        constructPropertiesString("Date created: ", sasFileProperties.getDateCreated());
        constructPropertiesString("Date modified: ", sasFileProperties.getDateModified());
        constructPropertiesString("SAS release: ", sasFileProperties.getSasRelease());
        constructPropertiesString("SAS server type: ", sasFileProperties.getServerType());
        constructPropertiesString("OS name: ", sasFileProperties.getOsName());
        constructPropertiesString("OS type: ", sasFileProperties.getOsType());
        constructPropertiesString("Header Length: ", sasFileProperties.getHeaderLength());
        constructPropertiesString("Page Length: ", sasFileProperties.getPageLength());
        constructPropertiesString("Page Count: ", sasFileProperties.getPageCount());
        constructPropertiesString("Row Length: ", sasFileProperties.getRowLength());
        constructPropertiesString("Row Count: ", sasFileProperties.getRowCount());
        constructPropertiesString("Mix Page Row Count: ", sasFileProperties.getMixPageRowCount());
        constructPropertiesString("Columns Count: ", sasFileProperties.getColumnsCount());
        getWriter().flush();
    }

    /**
     * The method to output string containing information about passed property using writer.
     *
     * @param propertyName the string containing name of a property.
     * @param property     a property value.
     * @throws IOException appears if the output into writer is impossible.
     */
    private void constructPropertiesString(String propertyName, Object property) throws IOException {
        getWriter().write(propertyName + property + "\n");
    }
}
