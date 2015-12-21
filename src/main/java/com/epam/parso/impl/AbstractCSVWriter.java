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

import java.io.IOException;
import java.io.Writer;

/**
 * This is a class to store functions which are used in classes {@link CSVMetadataWriterImpl} and
 * {@link CSVDataWriterImpl}
 */
abstract class AbstractCSVWriter {
    /**
     * The delimiter to use in the CSV format.
     */
    private static final String DEFAULT_DELIMITER = ",";

    /**
     * The default endline for csv file
     */
    private static final String DEFAULT_ENDLINE = "\n";

    /**
     * The variable to output csv file.
     */
    final Writer writer;

    /**
     * The delimiter for csv file
     */
    String delimiter = DEFAULT_DELIMITER;

    /**
     * The endline for csv file
     */
    String endline = DEFAULT_ENDLINE;

    /**
     * The constructor that defines writer variable to output result csv file.
     *
     * @param writer the writer which is used to output csv file.
     */
    AbstractCSVWriter(Writer writer) {
        this.writer = writer;
    }


    /**
     * The constructor that defines writer variable to output result csv file with selected delimiter.
     *
     * @param writer    the writer which is used to output csv file.
     * @param delimiter separator used in csv file.
     */
    AbstractCSVWriter(Writer writer, String delimiter) {
        this.writer = writer;
        this.delimiter = delimiter;
    }

    /**
     * The constructor that defines writer variable to output result csv file with selected delimiter and endline.
     *
     * @param writer    the writer which is used to output csv file.
     * @param delimiter separator used in csv file.
     * @param endline   symbols used in csv file as endline.
     */
    AbstractCSVWriter(Writer writer, String delimiter, String endline) {
        this.writer = writer;
        this.delimiter = delimiter;
        this.endline = endline;
    }

    /**
     * The method to output a text represented by an array of bytes using writer.
     * If the text contains the delimiter, line breaks, tabulation characters, and double quotes, the text is stropped.
     *
     * @param writer      the variable to output data.
     * @param delimiter   if trimmedText contains this delimiter it will be stropped.
     * @param trimmedText the array of bytes that contains the text to output.
     * @throws java.io.IOException appears if the output into writer is impossible.
     */
    static void checkSurroundByQuotesAndWrite(Writer writer, String delimiter, String trimmedText) throws IOException {
        boolean containsDelimiter = stringContainsItemFromList(trimmedText, delimiter, "\n", "\t", "\r", "\"");
        String trimmedTextWithoutQuotesDuplicates = trimmedText.replace("\"", "\"\"");
        if (containsDelimiter && trimmedTextWithoutQuotesDuplicates.length() != 0) {
            writer.write("\"");
        }
        writer.write(trimmedTextWithoutQuotesDuplicates);
        if (containsDelimiter && trimmedTextWithoutQuotesDuplicates.length() != 0) {
            writer.write("\"");
        }
    }

    private static boolean stringContainsItemFromList(String inputString, String... items) {
        for (String item : items) {
            if (inputString.contains(item)) {
                return true;
            }
        }
        return false;
    }
}
