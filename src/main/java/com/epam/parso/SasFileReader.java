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
 * Main interface for working with library.
 * @since 2.1
 */
public interface SasFileReader {
    /**
     * The function to get the {@link Column} list from {@link SasFileReader}.
     *
     * @return a list of columns.
     */
    List<Column> getColumns();

    /**
     * Reads all rows from the sas7bdat file.
     *
     * @return an array of array objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     */
    Object[][] readAll();

    /**
     * Reads rows one by one from the sas7bdat file.
     *
     * @return an array of objects whose elements can be objects of the following classes: double, long,
     * int, byte[], Date depending on the column they are in.
     *
     * @throws IOException if reading input stream is impossible.
     */
    Object[] readNext() throws IOException;

    /**
     * The function to get sas file properties.
     *
     * @return the object of the {@link SasFileProperties} class that stores file metadata.
     */
    SasFileProperties getSasFileProperties();
}
