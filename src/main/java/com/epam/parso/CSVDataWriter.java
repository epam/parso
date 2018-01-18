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

package com.epam.parso;

import java.io.IOException;
import java.util.List;

/**
 * Interface for exporting data from sas7bdat file to csv.
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
