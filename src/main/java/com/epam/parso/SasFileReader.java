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
 * Main interface for working with library.
 */
public interface SasFileReader {
    /**
     * The function to get the {@link Column} list from {@link SasFileReader}.
     *
     * @return a list of columns.
     */
    List<Column> getColumns();

    /**
     * The function to get the {@link Column} list from {@link SasFileReader}
     * according to the columnNames.
     *
     * @param columnNames - list of column names which should be returned.
     * @return a list of columns.
     */
    List<Column> getColumns(List<String> columnNames);

    /**
     * Reads all rows from the sas7bdat file.
     *
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    Object[][] readAll();

    /**
     * Reads all rows from the sas7bdat file. For each row, only the columns defined in the list are read.
     *
     * @param columnNames list of column names which should be processed.
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    Object[][] readAll(List<String> columnNames);

    /**
     * Reads rows one by one from the sas7bdat file.
     *
     * @return an array of objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     * @throws IOException if reading input stream is impossible.
     */
    Object[] readNext() throws IOException;

    /**
     * Reads rows one by one from the sas7bdat file. For each row, only the columns defined in the list are read.
     *
     * @param columnNames list of column names which should be processed.
     * @return an array of objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     * @throws IOException if reading input stream is impossible.
     */
    Object[] readNext(List<String> columnNames) throws IOException;

    /**
     * The function to get sas file properties.
     *
     * @return the object of the {@link SasFileProperties} class that stores file metadata.
     */
    SasFileProperties getSasFileProperties();
}
