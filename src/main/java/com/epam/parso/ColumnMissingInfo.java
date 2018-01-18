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
 * A class to store info about column if it's on page type "amd".
 */
public class ColumnMissingInfo {
    /**
     * The column id.
     */
    private int columnId;

    /**
     * The text subheader index that stores offset and length of missing info.
     */
    private int textSubheaderIndex;

    /**
     * The missing information offset.
     */
    private int offset;

    /**
     * The missing information length.
     */
    private int length;

    /**
     * The missing information type.
     */
    private MissingInfoType missingInfoType;

    /**
     * The constructor that defines all parameters of the ColumnMissingInfo class.
     * @param columnId the column id.
     * @param textSubheaderIndex the text subheader index.
     * @param offset the missing information offset.
     * @param length the missing information length.
     * @param missingInfoType the missing information type.
     */
    public ColumnMissingInfo(int columnId, int textSubheaderIndex, int offset, int length,
                             MissingInfoType missingInfoType) {
        this.columnId = columnId;
        this.textSubheaderIndex = textSubheaderIndex;
        this.offset = offset;
        this.length = length;
        this.missingInfoType = missingInfoType;
    }

    /**
     * The function to get {@link ColumnMissingInfo#columnId}.
     *
     * @return the number that contains the columnId.
     */
    public int getColumnId() {
        return columnId;
    }

    /**
     * The function to get {@link ColumnMissingInfo#textSubheaderIndex}.
     *
     * @return the number that contains the textSubheaderIndex.
     */
    public int getTextSubheaderIndex() {
        return textSubheaderIndex;
    }

    /**
     * The function to get {@link ColumnMissingInfo#offset}.
     *
     * @return the number that contains the offset.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * The function to get {@link ColumnMissingInfo#length}.
     *
     * @return the number that contains the length.
     */
    public int getLength() {
        return length;
    }

    /**
     * The function to get {@link ColumnMissingInfo#missingInfoType}.
     *
     * @return the number that contains the missingInfoType.
     */
    public MissingInfoType getMissingInfoType() {
        return missingInfoType;
    }

    /**
     * Enumeration of missing information types.
     */
    public enum MissingInfoType { NAME, FORMAT, LABEL }
}
