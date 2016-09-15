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

package com.epam.parso;

import java.io.IOException;
import java.util.List;

/**
 * Interface for exporting data from sas7bdat file to csv.
 * @since 2.1
 */
public interface CSVDataWriter {
    /**
     * The method to export a row from sas7bdat file (stored as an object of the
     * {@link com.epam.parso.impl.SasFileReaderImpl} class) using writer.
     *
     * @param columns the {@link Column} class variables list that stores columns description from the sas7bdat file.
     * @param row     the Objects arrays that stores data from the sas7bdat file.
     * @throws java.io.IOException appears if the output into writer is impossible.
     */
    void writeRow(List<Column> columns, Object[] row) throws IOException;

    /**
     * The method to export a parsed sas7bdat file (stored as an object of the
     * {@link com.epam.parso.impl.SasFileReaderImpl} class) using writer.
     *
     * @param columns the {@link Column} class variables list that stores columns description from the sas7bdat file.
     * @param rows    the Objects arrays array that stores data from the sas7bdat file.
     * @throws java.io.IOException appears if the output into writer is impossible.
     */
    void writeRowsArray(List<Column> columns, Object[][] rows) throws IOException;

    /**
     * The method to output the column names using the delimiter using writer.
     *
     * @param columns the list of column names.
     * @throws IOException appears if the output into writer is impossible.
     */
    void writeColumnNames(List<Column> columns) throws IOException;
}
