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

import com.epam.parso.CSVDataWriter;
import com.epam.parso.Column;
import com.epam.parso.DataWriterUtil;

import java.io.IOException;
import java.io.Writer;
import java.text.Format;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This is a class to export the sas7bdat file data into the CSV format.
 */
public class CSVDataWriterImpl extends AbstractCSVWriter implements CSVDataWriter {

    /**
     * The map to store (@link Column#id) column identifier and the formatter
     * for converting locale-sensitive values stored in this column into string.
     */
    private Map<Integer, Format> columnFormatters = new HashMap<>();

    /**
     * The constructor that defines writer variable to output result csv file.
     *
     * @param writer the writer which is used to output csv file.
     */
    public CSVDataWriterImpl(Writer writer) {
        super(writer);
    }

    /**
     * The constructor that defines writer variable to output result csv file with selected delimiter.
     *
     * @param writer    the writer which is used to output csv file.
     * @param delimiter separator used in csv file.
     */
    public CSVDataWriterImpl(Writer writer, String delimiter) {
        super(writer, delimiter);
    }

    /**
     * The constructor that defines writer variable to output result csv file with selected delimiter and endline.
     *
     * @param writer    the writer which is used to output csv file.
     * @param delimiter separator used in csv file.
     * @param endline   symbols used in csv file as endline.
     */
    public CSVDataWriterImpl(Writer writer, String delimiter, String endline) {
        super(writer, delimiter, endline);
    }

    /**
     * The constructor that defines writer variable to output result csv file with selected delimiter,
     * endline and locale.
     *
     * @param writer    the writer which is used to output csv file.
     * @param delimiter separator used in csv file.
     * @param endline   symbols used in csv file as endline.
     * @param locale    locale used for dates in csv file.
     */
    public CSVDataWriterImpl(Writer writer, String delimiter, String endline, Locale locale) {
        super(writer, delimiter, endline, locale);
    }

    /**
     * The method to export a row from sas7bdat file (stored as an object of the {@link SasFileReaderImpl} class)
     * using {@link CSVDataWriterImpl#writer}.
     *
     * @param columns the {@link Column} class variables list that stores columns description from the sas7bdat file.
     * @param row     the Objects arrays that stores data from the sas7bdat file.
     * @throws java.io.IOException appears if the output into writer is impossible.
     */
    @Override
    public void writeRow(List<Column> columns, Object[] row) throws IOException {
        if (row == null) {
            return;
        }
        Writer writer = getWriter();
        List<String> valuesToPrint = DataWriterUtil.getRowValues(columns, row, getLocale(), columnFormatters);
        for (int currentColumnIndex = 0; currentColumnIndex < columns.size(); currentColumnIndex++) {
            writer.write(checkSurroundByQuotes(getDelimiter(), valuesToPrint.get(currentColumnIndex)));
            if (currentColumnIndex != columns.size() - 1) {
                writer.write(getDelimiter());
            }
        }
        writer.write(getEndline());
        writer.flush();
    }

    /**
     * The method to export a parsed sas7bdat file (stored as an object of the {@link SasFileReaderImpl} class)
     * using {@link CSVDataWriterImpl#writer}.
     *
     * @param columns the {@link Column} class variables list that stores columns description from the sas7bdat file.
     * @param rows    the Objects arrays array that stores data from the sas7bdat file.
     * @throws java.io.IOException appears if the output into writer is impossible.
     */
    @Override
    public void writeRowsArray(List<Column> columns, Object[][] rows) throws IOException {
        for (Object[] currentRow : rows) {
            if (currentRow != null) {
                writeRow(columns, currentRow);
            } else {
                break;
            }
        }
    }

    /**
     * The method to output the column names using the {@link CSVDataWriterImpl#delimiter} delimiter
     * using {@link CSVDataWriterImpl#writer}.
     *
     * @param columns the list of column names.
     * @throws IOException appears if the output into writer is impossible.
     */
    @Override
    public void writeColumnNames(List<Column> columns) throws IOException {
        Writer writer = getWriter();
        for (int i = 0; i < columns.size(); i++) {
            checkSurroundByQuotesAndWrite(writer, getDelimiter(), columns.get(i).getName());
            if (i != columns.size() - 1) {
                writer.write(getDelimiter());
            }
        }
        writer.write(getEndline());
        writer.flush();
    }

}
