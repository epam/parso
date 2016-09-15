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

/**
 * A class to store column metadata.
 * @since 2.1
 */
public class Column {
    /**
     * The column id.
     */
    private final int id;

    /**
     * The column name.
     */
    private final String name;

    /**
     * The column label.
     */
    private final String label;

    /**
     * The column format.
     */
    private final String format;

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
    public Column(int id, String name, String label, String format, Class<?> type, int length) {
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
    public String getFormat() {
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
}
