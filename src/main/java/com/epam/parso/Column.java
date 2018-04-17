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

/**
 * A class to store column metadata.
 */
public class Column {
    /**
     * The column id.
     */
    private final int id;

    /**
     * The column name.
     */
    private String name;

    /**
     * The column label.
     */
    private String label;

    /**
     * The column format.
     */
    private ColumnFormat format;

    /**
     * The class of data stored in the cells of rows related to a column, can be Number.class or String.class.
     */
    private final Class<?> type;

    /**
     * The data length of the column.
     */
    private final int length;

    /**
     * The constructor that defines all parameters of the Column class.
     *
     * @param id     the column id
     * @param name   the column name.
     * @param label  the column label.
     * @param format the column format
     * @param type   the class of data stored in cells of rows related to the column, can be Number.class or
     *               String.class.
     * @param length the column length
     */
    public Column(int id, String name, String label, ColumnFormat format, Class<?> type, int length) {
        this.id = id;
        this.name = name;
        this.label = label;
        this.format = format;
        this.type = type;
        this.length = length;
    }

    /**
     * The function to get {@link Column#id}.
     *
     * @return the number that contains the column id.
     */
    public int getId() {
        return id;
    }

    /**
     * The function to get {@link Column#name}.
     *
     * @return the string that contains the column name.
     */
    public String getName() {
        return name;
    }

    /**
     * The function to get {@link Column#format}.
     *
     * @return the string that contains the column format.
     */
    public ColumnFormat getFormat() {
        return format;
    }

    /**
     * The function to get {@link Column#type}.
     *
     * @return the class of data stored in cells of rows related to the column.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * The function to get {@link Column#label}.
     *
     * @return the string that contains the column label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * The function to get {@link Column#length}.
     *
     * @return the number that contains the column length.
     */
    public int getLength() {
        return length;
    }

    /**
     * The function to set column name.
     * @param name the column name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The function to set column label.
     * @param label the column label.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * The function to set column format.
     * @param format the column format.
     */
    public void setFormat(ColumnFormat format) {
        this.format = format;
    }
}
