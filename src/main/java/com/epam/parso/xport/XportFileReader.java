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

package com.epam.parso.xport;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Main interface for working with XPORT files.
 */
// todo: implement handling missing variables
// todo: implement handling columns format (e.g. datetime format - see air_class.csv - first column should
//  be parsed as a date according to the field format (open air_class.xpt in SAS Universal Viewer for reference))
// todo: test V8 implementation
public interface XportFileReader extends AutoCloseable {

    /**
     * The function to get the {@link XportDatasetProperties} from {@link XportFileReader}.
     *
     * @return current dataset properties.
     */
    XportDatasetProperties getCurrentDatasetMetadata();

    /**
     * Reads all data from the XPORT file.
     * This method should not be used on the same reader instance in combination with any methods that
     * imply iterative reading (e.g. {@link XportFileReader#readNext()}, {@link XportFileReader#nextDataset()})
     *
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    List<XportDataset> readAll();

    /**
     * Reads all data from the XPORT file. Only the datasets defined in the list are read.
     * This method should not be used on the same reader instance in combination with any methods that
     * imply iterative reading (e.g. {@link XportFileReader#readNext()}, {@link XportFileReader#nextDataset()})
     *
     * @param datasetNames list of dataset names which should be processed.
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    List<XportDataset> readAllInDatasets(List<String> datasetNames);

    /**
     * Reads all data from the XPORT file. For each dataset specified, for each row, only the columns defined in the
     * list are read.
     * This method should not be used on the same reader instance in combination with any methods that
     * imply iterative reading (e.g. {@link XportFileReader#readNext()}, {@link XportFileReader#nextDataset()})
     *
     * @param datasetColumnNames map of dataset names to list of column names which should be processed.
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    List<XportDataset> readAll(Map<String, List<String>> datasetColumnNames);

    /**
     * Reads all rows from the XPORT file in current dataset.
     * For each row, only the columns defined in the list are read.
     *
     * @param columnNames list of column names which should be processed.
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    Object[][] readAllInCurrentDataset(List<String> columnNames);

    /**
     * Reads all rows from the XPORT file in current dataset.
     *
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    Object[][] readAllInCurrentDataset();

    /**
     * Reads rows in current dataset one by one from the XPORT file.
     *
     * @return an array of objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     * @throws IOException if reading input stream is impossible.
     */
    Object[] readNext() throws IOException;

    /**
     * Reads rows in current dataset one by one from the XPORT file. For each row, only the columns defined in the
     * list are read.
     *
     * @param columnNames list of column names which should be processed.
     * @return an array of objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     * @throws IOException if reading input stream is impossible.
     */
    Object[] readNext(List<String> columnNames) throws IOException;

    /**
     * Switch to the next XPORT dataset in the same file.
     *
     * @return next (i.e. 'new current') dataset index, -1 if there are no more datasets
     */
    int nextDataset();

    /**
     * The function to get XPORT file properties.
     *
     * @return the object of the {@link XportFileProperties} class that stores file metadata.
     */
    XportFileProperties getXportFileProperties();

    /**
     * The function to return the index of the current row in current dataset when reading the XPORT file.
     *
     * @return current row index
     */
    Integer getOffset();
}
