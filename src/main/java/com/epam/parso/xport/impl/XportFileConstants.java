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

package com.epam.parso.xport.impl;

/**
 * This is an class to store constants for parsing the XPORT file (byte offsets, column formats, accuracy) as well as
 * the standard constants of time and the sizes of standard data types.
 */
public interface XportFileConstants {
    /**
     * The size of all transport data set records in bytes.
     */
    int RECORD_LENGTH = 80;

    /**
     * The size of XPORT variable in bytes.
     */
    int BYTES_IN_VARIABLE = 8;

    /**
     * The size of XPORT timestamp in bytes.
     */
    int BYTES_IN_TIMESTAMP = 16;

    /**
     * Member (dataset) header constant.
     */
    String MEMBER_HEADER = "HEADER RECORD*******MEMBER  HEADER RECORD!!!!!!!";

    /**
     * Number of records in member (dataset) header.
     */
    int MEMBER_HEADER_RECORDS_COUNT = 5;

    /**
     * Length in bytes of member (dataset) header.
     */
    int MEMBER_HEADERS_OFFSET = RECORD_LENGTH * MEMBER_HEADER_RECORDS_COUNT;

    /**
     * Number of records in file header.
     */
    int HEADER_RECORDS_COUNT = 3;

    /**
     * Header offset value in bytes.
     */
    long HEADER_SIZE_OFFSET = 240L;

    /**
     * Length in bytes of label field in NAMESTR (variable descriptor) record.
     */
    int NAMESTR_LABEL_LENGTH = 40;

    /**
     * Length in bytes of long name field in NAMESTR (variable/column descriptor) record.
     */
    int NAMESTR_LONG_NAME_LENGTH = 32;

    /**
     * The sequence of bytes; 0 means big-endian.
     */
    int DEFAULT_ENDIANNESS = 0;
}
