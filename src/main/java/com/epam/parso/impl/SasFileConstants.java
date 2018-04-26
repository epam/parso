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

package com.epam.parso.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * This is an class to store constants for parsing the sas7bdat file (byte offsets, column formats, accuracy) as well as
 * the standard constants of time and the sizes of standard data types.
 */
interface SasFileConstants {
    /**
     * The size of the long value type in bytes.
     */
    int BYTES_IN_LONG = 8;

    /**
     * The size of the double value type in bytes.
     */
    int BYTES_IN_DOUBLE = 8;

    /**
     * The size of the int value type in bytes.
     */
    int BYTES_IN_INT = 4;

    /**
     * If a value with the length of {@link SasFileConstants#ALIGN_1_LENGTH} bytes stored in the sas7bdat file with
     * a {@link SasFileConstants#ALIGN_1_OFFSET} bytes offset equals to ALIGN_1_CHECKER_VALUE, then starting from
     * the {@link SasFileConstants#DATE_CREATED_OFFSET} bytes offset every offset should increase by
     * {@link SasFileConstants#ALIGN_1_VALUE} bytes.
     */
    int ALIGN_1_CHECKER_VALUE = 51;

    /**
     * If a value with the length of {@link SasFileConstants#ALIGN_1_LENGTH} bytes stored in the sas7bdat file with
     * a ALIGN_1_OFFSET bytes offset equals to {@link SasFileConstants#ALIGN_1_CHECKER_VALUE}, then starting from
     * the {@link SasFileConstants#DATE_CREATED_OFFSET} bytes offset every offset should increase by
     * {@link SasFileConstants#ALIGN_1_VALUE} bytes.
     */
    long ALIGN_1_OFFSET = 32L;

    /**
     * If a value with the length of ALIGN_1_LENGTH bytes stored in the sas7bdat file with
     * a {@link SasFileConstants#ALIGN_1_OFFSET} bytes offset equals to {@link SasFileConstants#ALIGN_1_CHECKER_VALUE},
     * then starting from the {@link SasFileConstants#DATE_CREATED_OFFSET} bytes offset every offset should increase
     * by {@link SasFileConstants#ALIGN_1_VALUE} bytes.
     */
    int ALIGN_1_LENGTH = 1;

    /**
     * If a value with the length of {@link SasFileConstants#ALIGN_1_LENGTH} bytes stored in the sas7bdat file with
     * a {@link SasFileConstants#ALIGN_1_OFFSET} bytes offset equals to {@link SasFileConstants#ALIGN_1_CHECKER_VALUE},
     * then starting from the {@link SasFileConstants#DATE_CREATED_OFFSET} bytes offset every offset should increase by
     * ALIGN_1_VALUE bytes.
     */
    int ALIGN_1_VALUE = 4;

    /**
     * If a value with the length of {@link SasFileConstants#ALIGN_2_LENGTH} bytes stored in the sas7bdat file with
     * a {@link SasFileConstants#ALIGN_2_OFFSET} bytes offset equals to U64_BYTE_CHECKER_VALUE, then:
     * - this sas7bdat file was created in the 64-bit version of SAS,
     * - starting from the {@link SasFileConstants#SAS_RELEASE_OFFSET} bytes offset every offset should increase by
     * {@link SasFileConstants#ALIGN_2_VALUE} bytes (in addition to {@link SasFileConstants#ALIGN_1_VALUE} bytes, if
     * those are added),
     * - the {@link SasFileConstants#PAGE_COUNT_LENGTH} value should increase by {@link SasFileConstants#ALIGN_2_VALUE}
     * bytes and the number of pages stored at the {@link SasFileConstants#PAGE_COUNT_OFFSET} bytes offset should read
     * as long.
     */
    int U64_BYTE_CHECKER_VALUE = 51;

    /**
     * If a value with the length of {@link SasFileConstants#ALIGN_2_LENGTH} bytes stored in the sas7bdat file with
     * a ALIGN_2_OFFSET bytes offset equals to {@link SasFileConstants#U64_BYTE_CHECKER_VALUE}, then:
     * - this sas7bdat file was created in the 64-bit version of SAS,
     * - starting from the {@link SasFileConstants#SAS_RELEASE_OFFSET} bytes offset every offset should increase
     * by {@link SasFileConstants#ALIGN_2_VALUE} bytes, (in addition to {@link SasFileConstants#ALIGN_1_VALUE}
     * bytes if those are added) and the number of pages stored at the {@link SasFileConstants#PAGE_COUNT_OFFSET} bytes
     * offset should read as long.
     */
    long ALIGN_2_OFFSET = 35L;

    /**
     * If a value with the length of ALIGN_2_LENGTH bytes stored in the sas7bdat file at
     * a {@link SasFileConstants#ALIGN_2_OFFSET} bytes offset equals to
     * {@link SasFileConstants#U64_BYTE_CHECKER_VALUE}, then:
     * - this sas7bdat file was created in the 64-bit version of SAS,
     * - starting from the {@link SasFileConstants#SAS_RELEASE_OFFSET} bytes offset every offset should increase by
     * {@link SasFileConstants#ALIGN_2_VALUE} bytes (in addition to {@link SasFileConstants#ALIGN_1_VALUE} bytes if
     * those are added) and the number of pages stored at the {@link SasFileConstants#PAGE_COUNT_OFFSET} bytes offset
     * should read as long.
     */
    int ALIGN_2_LENGTH = 1;

    /**
     * If a value with the length of {@link SasFileConstants#ALIGN_2_LENGTH} bytes stored in the sas7bdat file with
     * a {@link SasFileConstants#ALIGN_2_OFFSET} bytes offset equals to
     * {@link SasFileConstants#U64_BYTE_CHECKER_VALUE}, then:
     * - this sas7bdat file was created in the 64-bit version of SAS,
     * - starting from the {@link SasFileConstants#SAS_RELEASE_OFFSET} bytes offset every offset should increase by
     * ALIGN_2_VALUE bytes (in addition to {@link SasFileConstants#ALIGN_1_VALUE} bytes if those are added) and
     * the number of pages stored at the {@link SasFileConstants#PAGE_COUNT_OFFSET} bytes offset should read as long.
     */
    int ALIGN_2_VALUE = 4;

    /**
     * If a value with the length of {@link SasFileConstants#ENDIANNESS_LENGTH} bytes stored in the sas7bdat file with
     * a ENDIANNESS_OFFSET bytes offset equals to 1 then the bytes order is little-endian (Intel),
     * if the value equals to 0 then the bytes order is big-endian.
     */
    long ENDIANNESS_OFFSET = 37L;

    /**
     * If a value with the length of ENDIANNESS_LENGTH bytes stored in the sas7bdat file with
     * a {@link SasFileConstants#ENDIANNESS_OFFSET} bytes offset equals to 1 then the bytes order is
     * little-endian (Intel), if the value equals to 0 then the bytes order is big-endian.
     */
    int ENDIANNESS_LENGTH = 1;

    /**
     * If a value with the length of ENDIANNESS_LENGTH bytes stored in the sas7bdat file with
     * a {@link SasFileConstants#ENDIANNESS_OFFSET} bytes offset equals to LITTLE_ENDIAN_CHECKER
     * then the bytes order is little-endian (Intel), if the value equals to
     * {@link SasFileConstants#BIG_ENDIAN_CHECKER} then the bytes order is big-endian.
     */
    int LITTLE_ENDIAN_CHECKER = 1;

    /**
     * If a value with the length of ENDIANNESS_LENGTH bytes stored in the sas7bdat file with
     * a {@link SasFileConstants#ENDIANNESS_OFFSET} bytes offset equals to
     * {@link SasFileConstants#LITTLE_ENDIAN_CHECKER} then the bytes order is little-endian (Intel),
     * if the value equals to BIG_ENDIAN_CHECKER then the bytes order is big-endian.
     */
    int BIG_ENDIAN_CHECKER = 0;

    /**
     * The sas7bdat file stores its character encoding with the length of {@link SasFileConstants#ENCODING_LENGTH} bytes
     * and a ENCODING_OFFSET bytes offset.
     */
    long ENCODING_OFFSET = 70L;

    /**
     * The sas7bdat files its character encoding with the length of ENCODING_LENGTH bytes and
     * a {@link SasFileConstants#ENCODING_OFFSET} bytes offset.
     */
    int ENCODING_LENGTH = 1;

    /**
     * The integer (one or two bytes) at the {@link SasFileConstants#ENCODING_OFFSET} indicates the character encoding
     * of string data.  The SAS_CHARACTER_ENCODINGS map links the values that are known to occur and the associated
     * encoding.  This list excludes encodings present in SAS but missing support in {@link java.nio.charset}
     */
    Map<Byte, String> SAS_CHARACTER_ENCODINGS = Collections.unmodifiableMap(new HashMap<Byte, String>() {
        {
            put((byte) 0x46, "x-MacArabic");
            put((byte) 0xF5, "x-MacCroatian");
            put((byte) 0xF6, "x-MacCyrillic");
            put((byte) 0x48, "x-MacGreek");
            put((byte) 0x47, "x-MacHebrew");
            put((byte) 0xA3, "x-MacIceland");
            put((byte) 0x22, "ISO-8859-6");
            put((byte) 0x45, "x-MacRoman");
            put((byte) 0xF7, "x-MacRomania");
            put((byte) 0x49, "x-MacThai");
            put((byte) 0x4B, "x-MacTurkish");
            put((byte) 0x4C, "x-MacUkraine");
            put((byte) 0x7B, "Big5");
            put((byte) 0x21, "ISO-8859-5");
            put((byte) 0x4E, "IBM037");
            put((byte) 0x5F, "x-IBM1025");
            put((byte) 0xCF, "x-IBM1097");
            put((byte) 0x62, "x-IBM1112");
            put((byte) 0x63, "x-IBM1122");
            put((byte) 0xB7, "IBM01140");
            put((byte) 0xB8, "IBM01141");
            put((byte) 0xB9, "IBM01142");
            put((byte) 0xBA, "IBM01143");
            put((byte) 0xBB, "IBM01144");
            put((byte) 0xBC, "IBM01145");
            put((byte) 0xBD, "IBM01146");
            put((byte) 0xBE, "IBM01147");
            put((byte) 0xBF, "IBM01148");
            put((byte) 0xD3, "IBM01149");
            put((byte) 0x57, "IBM424");
            put((byte) 0x58, "IBM500");
            put((byte) 0x59, "IBM-Thai");
            put((byte) 0x5A, "IBM870");
            put((byte) 0x5B, "x-IBM875");
            put((byte) 0x7D, "GBK");
            put((byte) 0x86, "EUC-JP");
            put((byte) 0x8C, "EUC-KR");
            put((byte) 0x77, "x-EUC-TW");
            put((byte) 0xCD, "GB18030");
            put((byte) 0x23, "ISO-8859-7");
            put((byte) 0x24, "ISO-8859-8");
            put((byte) 0x80, "x-IBM1381");
            put((byte) 0x82, "x-IBM930");
            put((byte) 0x8B, "x-IBM933");
            put((byte) 0x7C, "x-IBM935");
            put((byte) 0x75, "x-IBM937");
            put((byte) 0x81, "x-IBM939");
            put((byte) 0x89, "x-IBM942");
            put((byte) 0x8E, "x-IBM949");
            put((byte) 0xAC, "x-ISO2022-CN-CNS");
            put((byte) 0xA9, "x-ISO2022-CN-GB");
            put((byte) 0xA7, "ISO-2022-JP");
            put((byte) 0xA8, "ISO-2022-KR");
            put((byte) 0x1D, "ISO-8859-1");
            put((byte) 0x1E, "ISO-8859-2");
            put((byte) 0x1F, "ISO-8859-3");
            put((byte) 0x20, "ISO-8859-4");
            put((byte) 0x25, "ISO-8859-9");
            put((byte) 0xF2, "ISO-8859-13");
            put((byte) 0x28, "ISO-8859-15");
            put((byte) 0x88, "x-windows-iso2022jp");
            put((byte) 0x7E, "x-mswin-936");
            put((byte) 0x8D, "x-windows-949");
            put((byte) 0x76, "x-windows-950");
            put((byte) 0xAD, "IBM037");
            put((byte) 0x6C, "x-IBM1025");
            put((byte) 0x6D, "IBM1026");
            put((byte) 0x6E, "IBM1047");
            put((byte) 0xD0, "x-IBM1097");
            put((byte) 0x6F, "x-IBM1112");
            put((byte) 0x70, "x-IBM1122");
            put((byte) 0xC0, "IBM01140");
            put((byte) 0xC1, "IBM01141");
            put((byte) 0xC2, "IBM01142");
            put((byte) 0xC3, "IBM01143");
            put((byte) 0xC4, "IBM01144");
            put((byte) 0xC5, "IBM01145");
            put((byte) 0xC6, "IBM01146");
            put((byte) 0xC7, "IBM01147");
            put((byte) 0xC8, "IBM01148");
            put((byte) 0xD4, "IBM01149");
            put((byte) 0x66, "IBM424");
            put((byte) 0x67, "IBM-Thai");
            put((byte) 0x68, "IBM870");
            put((byte) 0x69, "x-IBM875");
            put((byte) 0xEA, "x-IBM930");
            put((byte) 0xEB, "x-IBM933");
            put((byte) 0xEC, "x-IBM935");
            put((byte) 0xED, "x-IBM937");
            put((byte) 0xEE, "x-IBM939");
            put((byte) 0x2B, "IBM437");
            put((byte) 0x2C, "IBM850");
            put((byte) 0x2D, "IBM852");
            put((byte) 0x3A, "IBM857");
            put((byte) 0x2E, "IBM00858");
            put((byte) 0x2F, "IBM862");
            put((byte) 0x33, "IBM866");
            put((byte) 0x8A, "Shift_JIS");
            put((byte) 0xF8, "JIS_X0201");
            put((byte) 0x27, "x-iso-8859-11");
            put((byte) 0x1C, "US-ASCII");
            put((byte) 0x14, "UTF-8");
            put((byte) 0x42, "windows-1256");
            put((byte) 0x43, "windows-1257");
            put((byte) 0x3D, "windows-1251");
            put((byte) 0x3F, "windows-1253");
            put((byte) 0x41, "windows-1255");
            put((byte) 0x3E, "windows-1252");
            put((byte) 0x3C, "windows-1250");
            put((byte) 0x40, "windows-1254");
            put((byte) 0x44, "windows-1258");
        }
    });

    /**
     * The sas7bdat file stores the table name with the length of {@link SasFileConstants#DATASET_LENGTH} bytes and
     * a DATASET_OFFSET bytes offset.
     */
    long DATASET_OFFSET = 92L;

    /**
     * The sas7bdat file stores the table name with the length of DATASET_LENGTH bytes and
     * a {@link SasFileConstants#DATASET_OFFSET} bytes offset.
     */
    int DATASET_LENGTH = 64;

    /**
     * The sas7bdat file stores its file type with the length of {@link SasFileConstants#FILE_TYPE_LENGTH} bytes
     * and a FILE_TYPE_OFFSET bytes offset.
     */
    long FILE_TYPE_OFFSET = 156L;

    /**
     * The sas7bdat file stores its file type with the length of FILE_TYPE_LENGTH bytes and
     * a {@link SasFileConstants#FILE_TYPE_OFFSET} bytes offset.
     */
    int FILE_TYPE_LENGTH = 8;

    /**
     * The sas7bdat file stores its creation date with the length of {@link SasFileConstants#DATE_CREATED_LENGTH} bytes
     * and a DATE_CREATED_OFFSET bytes offset (with possible addition of {@link SasFileConstants#ALIGN_1_VALUE}).
     * The date is a double value denoting the number of seconds elapsed from 01/01/1960 to the date stored.
     */
    long DATE_CREATED_OFFSET = 164L;

    /**
     * The sas7bdat file stores its creation date with the length of DATE_CREATED_LENGTH bytes and
     * a {@link SasFileConstants#DATE_CREATED_OFFSET} bytes offset (with possible addition of
     * {@link SasFileConstants#ALIGN_1_VALUE}). The date is a double value denoting the number of seconds elapsed
     * from 01/01/1960 to the date stored.
     */
    int DATE_CREATED_LENGTH = 8;

    /**
     * The sas7bdat file stores its last modification date with the length of
     * {@link SasFileConstants#DATE_MODIFIED_LENGTH} bytes and a DATE_MODIFIED_OFFSET bytes offset (with possible
     * addition of {@link SasFileConstants#ALIGN_1_VALUE}). The date is a double value denoting the number of seconds
     * elapsed from 01/01/1960 to the date stored.
     */
    long DATE_MODIFIED_OFFSET = 172L;

    /**
     * The sas7bdat file stores its last modification date with the length of DATE_MODIFIED_LENGTH bytes and
     * a {@link SasFileConstants#DATE_MODIFIED_OFFSET} bytes offset (with possible addition of
     * {@link SasFileConstants#ALIGN_1_VALUE}). The date is a value of double format denoting the number of seconds
     * elapsed from 01/01/1960 to the date stored.
     */
    int DATE_MODIFIED_LENGTH = 8;

    /**
     * The sas7bdat file stores the length of its metadata (can be 1024 and 8192) as an int value with the length of
     * {@link SasFileConstants#HEADER_SIZE_LENGTH} bytes and a HEADER_SIZE_OFFSET bytes offset (with possible addition
     * of {@link SasFileConstants#ALIGN_1_VALUE}).
     */
    long HEADER_SIZE_OFFSET = 196L;

    /**
     * The sas7bdat file stores the length of its metadata (can be 1024 and 8192) as an int value with the length of
     * HEADER_SIZE_LENGTH bytes and a  {@link SasFileConstants#HEADER_SIZE_OFFSET} bytes offset (with possible addition
     * of {@link SasFileConstants#ALIGN_1_VALUE}).
     */
    int HEADER_SIZE_LENGTH = 4;

    /**
     * The sas7bdat file stores the length of its pages as an int value with the length of
     * {@link SasFileConstants#PAGE_SIZE_LENGTH} bytes and a PAGE_SIZE_OFFSET bytes offset (with possible addition of
     * {@link SasFileConstants#ALIGN_1_VALUE}).
     */
    long PAGE_SIZE_OFFSET = 200L;

    /**
     * The sas7bdat file stores the length of its pages as an int value with the length of PAGE_SIZE_LENGTH bytes and
     * a {@link SasFileConstants#PAGE_SIZE_OFFSET} bytes offset (with possible addition of
     * {@link SasFileConstants#ALIGN_1_VALUE}).
     */
    int PAGE_SIZE_LENGTH = 4;

    /**
     * The sas7bdat file stores the number of its pages as an int or long value (depending on
     * {@link SasFileConstants#ALIGN_2_VALUE}) with the length of {@link SasFileConstants#PAGE_COUNT_LENGTH} bytes
     * (with possible addition of {@link SasFileConstants#ALIGN_2_VALUE}) and a PAGE_COUNT_OFFSET bytes offset
     * (with possible addition of {@link SasFileConstants#ALIGN_1_VALUE}).
     */
    long PAGE_COUNT_OFFSET = 204L;

    /**
     * The sas7bdat file stores the number of its pages as an int or long value (depending on
     * {@link SasFileConstants#ALIGN_2_VALUE}) with the length of PAGE_COUNT_LENGTH bytes (with possible addition of
     * {@link SasFileConstants#ALIGN_2_VALUE}) and a {@link SasFileConstants#PAGE_COUNT_OFFSET} bytes offset
     * (with possible addition of {@link SasFileConstants#ALIGN_1_VALUE}).
     */
    int PAGE_COUNT_LENGTH = 4;

    /**
     * The sas7bdat file stores the name of SAS version in which the sas7bdat was created with the length of
     * {@link SasFileConstants#SAS_RELEASE_LENGTH} bytes and a SAS_RELEASE_OFFSET bytes offset (with possible addition
     * of {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}).
     */
    long SAS_RELEASE_OFFSET = 216L;

    /**
     * The sas7bdat file stores the name of SAS version in which the sas7bdat was created with the length of
     * SAS_RELEASE_LENGTH bytes and a {@link SasFileConstants#SAS_RELEASE_OFFSET} bytes offset (with possible addition
     * of {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}).
     */
    int SAS_RELEASE_LENGTH = 8;

    /**
     * The sas7bdat file stores the name of the server version on which the sas7bdat was created with the length of
     * {@link SasFileConstants#SAS_SERVER_TYPE_LENGTH} bytes and a SAS_SERVER_TYPE_OFFSET bytes offset (with possible
     * addition of {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}).
     */
    long SAS_SERVER_TYPE_OFFSET = 224L;

    /**
     * The sas7bdat file stores the name of the server version on which the sas7bdat was created with the length of
     * SAS_SERVER_TYPE_LENGTH bytes and a {@link SasFileConstants#SAS_SERVER_TYPE_OFFSET} bytes offset (with possible
     * addition of {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}).
     */
    int SAS_SERVER_TYPE_LENGTH = 16;

    /**
     * The sas7bdat file stores the version of the OS in which the sas7bdat was created with the length of
     * {@link SasFileConstants#OS_VERSION_NUMBER_LENGTH} bytes and a OS_VERSION_NUMBER_OFFSET bytes offset
     * (with possible addition of {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}).
     */
    long OS_VERSION_NUMBER_OFFSET = 240L;

    /**
     * The sas7bdat file stores the version of the OS in which the sas7bdat was created with the length of
     * OS_VERSION_NUMBER_LENGTH bytes and a {@link SasFileConstants#OS_VERSION_NUMBER_OFFSET} bytes offset (with
     * possible addition of {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}).
     */
    int OS_VERSION_NUMBER_LENGTH = 16;

    /**
     * The sas7bdat file stores the name of the OS in which the sas7bdat was created with the length of
     * {@link SasFileConstants#OS_MAKER_LENGTH} bytes and a OS_MAKER_OFFSET bytes offset (with possible addition of
     * {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}). If the OS name is
     * an empty string, then the file stores the OS name with the length of {@link SasFileConstants#OS_NAME_LENGTH}
     * bytes and a {@link SasFileConstants#OS_NAME_OFFSET} bytes offset (with possible addition of
     * {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}).
     */
    long OS_MAKER_OFFSET = 256L;

    /**
     * The sas7bdat file stores the name of the OS in which the sas7bdat was created with the length of OS_MAKER_LENGTH
     * bytes and a {@link SasFileConstants#OS_MAKER_OFFSET} bytes offset (with possible addition of
     * {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}). If the OS name is
     * an empty string, then the file stores the OS name with the length of {@link SasFileConstants#OS_NAME_LENGTH}
     * bytes and a {@link SasFileConstants#OS_NAME_OFFSET} bytes offset (with possible addition of
     * {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}).
     */
    int OS_MAKER_LENGTH = 16;

    /**
     * The sas7bdat file stores the name of the OS in which the sas7bdat was created with the length of
     * {@link SasFileConstants#OS_NAME_LENGTH} bytes and a OS_NAME_OFFSET bytes offset (with possible addition of
     * {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}). If the OS name is
     * an empty string, then the file stores the OS name with the length of {@link SasFileConstants#OS_MAKER_LENGTH}
     * bytes and a {@link SasFileConstants#OS_MAKER_OFFSET} bytes offset (with possible addition of
     * {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}).
     */
    long OS_NAME_OFFSET = 272L;

    /**
     * The sas7bdat file stores the name of the OS in which the sas7bdat was created with the length of OS_NAME_LENGTH
     * bytes and a {@link SasFileConstants#OS_NAME_OFFSET} bytes offset (with possible addition of
     * {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}). If the OS name is
     * an empty string, then the file stores the OS name with the length of  {@link SasFileConstants#OS_MAKER_LENGTH}
     * bytes and a {@link SasFileConstants#OS_MAKER_OFFSET} bytes offset (with possible addition of
     * {@link SasFileConstants#ALIGN_1_VALUE} and {@link SasFileConstants#ALIGN_2_VALUE}).
     */
    int OS_NAME_LENGTH = 16;

    /**
     * An offset in bytes from the start of the page - for sas7bdat files created in the 32-bit version of SAS
     * (see {@link SasFileConstants#ALIGN_2_VALUE}). Added to all offsets within a page.
     */
    int PAGE_BIT_OFFSET_X86 = 16;

    /**
     * An offset in bytes from the start of the page - for sas7bdat files created in the 64-bit version of SAS
     * (see {@link SasFileConstants#ALIGN_2_VALUE}). Added to all offsets within a page.
     */
    int PAGE_BIT_OFFSET_X64 = 32;

    /**
     * The length in bytes of one subheader pointer ({@link SasFileParser.SubheaderPointer}) of a sas7bdat file
     * created in the 32-bit version of SAS (see {@link SasFileConstants#ALIGN_2_VALUE}).
     */
    int SUBHEADER_POINTER_LENGTH_X86 = 12;

    /**
     * The length in bytes of one subheader pointer ({@link SasFileParser.SubheaderPointer}) of a sas7bdat file
     * created in the 64-bit version of SAS (see {@link SasFileConstants#ALIGN_2_VALUE}).
     */
    int SUBHEADER_POINTER_LENGTH_X64 = 24;

    /**
     * The sas7bdat file stores the type of page as a short value with the length of
     * {@link SasFileConstants#PAGE_TYPE_LENGTH} bytes and a PAGE_TYPE_OFFSET bytes offset (with addition of
     * {@link SasFileConstants#PAGE_BIT_OFFSET_X86} or {@link SasFileConstants#PAGE_BIT_OFFSET_X64}).
     * There can be {@link SasFileConstants#PAGE_META_TYPE_1}, {@link SasFileConstants#PAGE_META_TYPE_2},
     * {@link SasFileConstants#PAGE_DATA_TYPE}, or {@link SasFileConstants#PAGE_MIX_TYPE} page types.
     */
    long PAGE_TYPE_OFFSET = 0L;

    /**
     * The sas7bdat file stores the type of page as a short value with the length of PAGE_TYPE_LENGTH bytes
     * and a {@link SasFileConstants#PAGE_TYPE_OFFSET} bytes offset (with addition of
     * {@link SasFileConstants#PAGE_BIT_OFFSET_X86} or {@link SasFileConstants#PAGE_BIT_OFFSET_X64}).
     * There can be {@link SasFileConstants#PAGE_META_TYPE_1}, {@link SasFileConstants#PAGE_META_TYPE_2},
     * {@link SasFileConstants#PAGE_DATA_TYPE}, or {@link SasFileConstants#PAGE_MIX_TYPE} page types.
     */
    int PAGE_TYPE_LENGTH = 2;

    /**
     * For pages of the {@link SasFileConstants#PAGE_DATA_TYPE} type, the sas7bdat file stores the number of rows
     * in the table on the current page as a short value - with the length of
     * {@link SasFileConstants#BLOCK_COUNT_LENGTH} bytes and a BLOCK_COUNT_OFFSET bytes offset (with addition of
     * {@link SasFileConstants#PAGE_BIT_OFFSET_X86} or {@link SasFileConstants#PAGE_BIT_OFFSET_X64}).
     */
    long BLOCK_COUNT_OFFSET = 2L;

    /**
     * For pages of the {@link SasFileConstants#PAGE_DATA_TYPE} type, the sas7bdat file stores the number of rows
     * in the table on the current page as a short value - with the length of BLOCK_COUNT_LENGTH bytes
     * and a {@link SasFileConstants#BLOCK_COUNT_OFFSET} bytes offset (with addition of
     * {@link SasFileConstants#PAGE_BIT_OFFSET_X86} or {@link SasFileConstants#PAGE_BIT_OFFSET_X64}).
     */
    int BLOCK_COUNT_LENGTH = 2;

    /**
     * For pages of the {@link SasFileConstants#PAGE_META_TYPE_1}, {@link SasFileConstants#PAGE_META_TYPE_2}
     * and {@link SasFileConstants#PAGE_MIX_TYPE} types, the sas7bdat file stores the number of subheaders on
     * the current page as a short value - with the length of {@link SasFileConstants#SUBHEADER_COUNT_LENGTH}
     * bytes and a SUBHEADER_COUNT_OFFSET bytes offset from the beginning of the page (with addition of
     * {@link SasFileConstants#PAGE_BIT_OFFSET_X86} or {@link SasFileConstants#PAGE_BIT_OFFSET_X64}).
     */
    long SUBHEADER_COUNT_OFFSET = 4L;

    /**
     * For pages of the {@link SasFileConstants#PAGE_META_TYPE_1}, {@link SasFileConstants#PAGE_META_TYPE_2}
     * and {@link SasFileConstants#PAGE_MIX_TYPE} types, the sas7bdat file stores the number of subheaders on
     * the current page as a short value - with the length of SUBHEADER_COUNT_LENGTH bytes and a
     * {@link SasFileConstants#SUBHEADER_COUNT_OFFSET} bytes offset from the beginning of the page
     * (with addition of {@link SasFileConstants#PAGE_BIT_OFFSET_X86} or {@link SasFileConstants#PAGE_BIT_OFFSET_X64}).
     */
    int SUBHEADER_COUNT_LENGTH = 2;

    /**
     * The page type storing metadata as a set of subheaders. It can also store compressed row data in subheaders.
     * The sas7bdat format has two values that correspond to the page type 'meta'.
     */
    int PAGE_META_TYPE_1 = 0;

    /**
     * The page type storing metadata as a set of subheaders. It can also store compressed row data in subheaders.
     * The sas7bdat format has two values that correspond to the page type 'meta'.
     */
    int PAGE_META_TYPE_2 = 16384;

    /**
     * The page type storing only data as a number of table rows.
     */
    int PAGE_DATA_TYPE = 256;

    /**
     * The page type storing metadata as a set of subheaders and data as a number of table rows.
     */
    int PAGE_MIX_TYPE = 512;

    /**
     * The page type amd.
     */
    int PAGE_AMD_TYPE = 1024;

    /**
     * The sas7bdat file stores the array of subheader pointers ({@link SasFileParser.SubheaderPointer}) at this
     * offset (adding {@link SasFileConstants#PAGE_BIT_OFFSET_X86} or {@link SasFileConstants#PAGE_BIT_OFFSET_X64})
     * from the beginning of the page.
     */
    int SUBHEADER_POINTERS_OFFSET = 8;

    /**
     * If the {@link SasFileParser.SubheaderPointer#compression} value of a subheader equals to TRUNCATED_SUBHEADER_ID
     * then it does not contain useful information.
     */
    int TRUNCATED_SUBHEADER_ID = 1;

    /**
     * A subheader with compressed data has two parameters:
     * its {@link SasFileParser.SubheaderPointer#compression} should equal to COMPRESSED_SUBHEADER_ID and its
     * {@link SasFileParser.SubheaderPointer#type} should equal to {@link SasFileConstants#COMPRESSED_SUBHEADER_TYPE}.
     */
    int COMPRESSED_SUBHEADER_ID = 4;

    /**
     * A Subheader with compressed data has two parameters:
     * its {@link SasFileParser.SubheaderPointer#compression} should equal to
     * {@link SasFileConstants#COMPRESSED_SUBHEADER_ID} and its {@link SasFileParser.SubheaderPointer#type}
     * should equal to COMPRESSED_SUBHEADER_TYPE.
     */
    int COMPRESSED_SUBHEADER_TYPE = 1;

    /**
     * The number of bits in a byte.
     */
    int BITS_IN_BYTE = 8;

    /**
     * The multiplier whose product with the length of the variable type (that can be int or long depending on the
     * {@link SasFileConstants#ALIGN_2_VALUE} value) is the offset from the subheader beginning
     * {@link SasFileParser.RowSizeSubheader} at which the row length is stored.
     */
    int ROW_LENGTH_OFFSET_MULTIPLIER = 5;

    /**
     * The multiplier whose product with the length of the variable type (that can be int or long depending on the
     * {@link SasFileConstants#ALIGN_2_VALUE} value) is the offset from the subheader beginning
     * {@link SasFileParser.RowSizeSubheader} at which the number of rows in the table is stored.
     */
    int ROW_COUNT_OFFSET_MULTIPLIER = 6;

    /**
     * The multiplier whose product with the length of the variable type (that can be int or long depending on the
     * {@link SasFileConstants#ALIGN_2_VALUE} value) is the offset from the subheader beginning
     * {@link SasFileParser.RowSizeSubheader} at which the file stores the number of rows in the table
     * on the last page of the {@link SasFileConstants#PAGE_MIX_TYPE} type.
     */
    int ROW_COUNT_ON_MIX_PAGE_OFFSET_MULTIPLIER = 15;

    /**
     * The number of bytes taken by the value denoting the length of the text block with information about
     * file compression and table rows (name, label, format).
     */
    int TEXT_BLOCK_SIZE_LENGTH = 2;

    /**
     * A substring that appears in the text block with information about file compression and table rows
     * (name, label, format) if CHAR compression is used.
     */
    String COMPRESS_CHAR_IDENTIFYING_STRING = "SASYZCRL";

    /**
     * A substring that appears in the text block with information about file compression and table rows
     * (name, label, format) if BIN compression is used.
     */
    String COMPRESS_BIN_IDENTIFYING_STRING = "SASYZCR2";

    /**
     * The length of the column name pointer in bytes.
     */
    int COLUMN_NAME_POINTER_LENGTH = 8;

    /**
     * For each table column, the sas7bdat file stores the index of the
     * {@link SasFileParser.ColumnTextSubheader} subheader whose text block contains the name
     * of the column - with the length of {@link SasFileConstants#COLUMN_NAME_TEXT_SUBHEADER_LENGTH} bytes and an offset
     * measured from the beginning of the {@link SasFileParser.ColumnNameSubheader} subheader
     * and calculated by the following formula: COLUMN_NAME_TEXT_SUBHEADER_OFFSET +
     * + column number * {@link SasFileConstants#COLUMN_NAME_POINTER_LENGTH} + size of the value type (int or long
     * depending on the {@link SasFileConstants#ALIGN_2_VALUE} value).
     */
    long COLUMN_NAME_TEXT_SUBHEADER_OFFSET = 0L;

    /**
     * For each table column, the sas7bdat file stores the index of the
     * {@link SasFileParser.ColumnTextSubheader} subheader whose text block contains the name
     * of the column - with the length of COLUMN_NAME_TEXT_SUBHEADER_LENGTH bytes and an offset measured from
     * the beginning of the {@link SasFileParser.ColumnNameSubheader} subheader
     * and calculated by the following formula: {@link SasFileConstants#COLUMN_NAME_TEXT_SUBHEADER_OFFSET} +
     * + column number * {@link SasFileConstants#COLUMN_NAME_POINTER_LENGTH} + size of the value type (int or long
     * depending on the {@link SasFileConstants#ALIGN_2_VALUE} value).
     */
    int COLUMN_NAME_TEXT_SUBHEADER_LENGTH = 2;

    /**
     * For each table column, the sas7bdat file stores the offset (in symbols) of the column name from the beginning
     * of the text block of the {@link SasFileParser.ColumnTextSubheader} subheader (see
     * {@link SasFileConstants#COLUMN_NAME_TEXT_SUBHEADER_OFFSET})- with the length of
     * {@link SasFileConstants#COLUMN_NAME_OFFSET_LENGTH} bytes and an offset measured from the beginning
     * of the {@link SasFileParser.ColumnNameSubheader} subheader and calculated by
     * the following formula: COLUMN_NAME_OFFSET_OFFSET +
     * + column number * {@link SasFileConstants#COLUMN_NAME_POINTER_LENGTH} + size of the value type (int or long
     * depending on the {@link SasFileConstants#ALIGN_2_VALUE} value).
     */
    long COLUMN_NAME_OFFSET_OFFSET = 2L;

    /**
     * For each table column, the sas7bdat file stores the offset (in symbols) of the column name from the beginning
     * of the text block of the {@link SasFileParser.ColumnTextSubheader} subheader (see
     * {@link SasFileConstants#COLUMN_NAME_TEXT_SUBHEADER_OFFSET})- with the length of COLUMN_NAME_OFFSET_LENGTH bytes
     * and an offset measured from the beginning of the {@link SasFileParser.ColumnNameSubheader}
     * subheader and calculated by the following formula: {@link SasFileConstants#COLUMN_NAME_OFFSET_OFFSET} +
     * + column number * {@link SasFileConstants#COLUMN_NAME_POINTER_LENGTH} + size of the value type (int or long
     * depending on the {@link SasFileConstants#ALIGN_2_VALUE} value).
     */
    int COLUMN_NAME_OFFSET_LENGTH = 2;

    /**
     * For each table column, the sas7bdat file stores column name length (in symbols):
     * - with the length of {@link SasFileConstants#COLUMN_NAME_LENGTH_LENGTH} bytes,
     * - at an offset  measured from the beginning of the
     * {@link SasFileParser.ColumnNameSubheader} subheader
     * and calculated by the following formula: COLUMN_NAME_LENGTH_OFFSET +
     * + column number * {@link SasFileConstants#COLUMN_NAME_POINTER_LENGTH} + size of the value type (int or long
     * depending on the {@link SasFileConstants#ALIGN_2_VALUE} value).
     */
    long COLUMN_NAME_LENGTH_OFFSET = 4L;

    /**
     * For each table column, the sas7bdat file stores column name length (in symbols):
     * - with the length of COLUMN_NAME_LENGTH_LENGTH bytes.
     * - at an offset measured from the beginning of the
     * {@link SasFileParser.ColumnNameSubheader} subheader and calculated
     * by the following formula: {@link SasFileConstants#COLUMN_NAME_LENGTH_OFFSET} +
     * + column number * {@link SasFileConstants#COLUMN_NAME_POINTER_LENGTH} + size of the value type (int or long
     * depending on the {@link SasFileConstants#ALIGN_2_VALUE} value).
     */
    int COLUMN_NAME_LENGTH_LENGTH = 2;

    /**
     * For every table column, the sas7bdat file stores the value (int or long depending on
     * {@link SasFileConstants#ALIGN_2_VALUE}) that defines the offset of data in the current column
     * from the beginning of the row with data in bytes:
     * - at an offset measured from the beginning of the {@link SasFileParser.ColumnAttributesSubheader} subheader
     * and calculated by the following formula: COLUMN_DATA_OFFSET_OFFSET +
     * + column index * (8 + the size of value type (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE})) +
     * + the size of value type (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE})
     * from the beginning of the {@link SasFileParser.ColumnAttributesSubheader} subheader.
     */
    long COLUMN_DATA_OFFSET_OFFSET = 8L;

    /**
     * For every table column, the sas7bdat file stores the denotation (in bytes) of data length in a column:
     * - at an offset measured from the beginning of the {@link SasFileParser.ColumnAttributesSubheader} subheader
     * and calculated by the following formula: COLUMN_DATA_LENGTH_OFFSET +
     * + column index * (8 + the size of value type (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE})) +
     * + the size of value type (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE})
     * from the beginning of the {@link SasFileParser.ColumnAttributesSubheader} subheader,
     * - with the length of {@link SasFileConstants#COLUMN_DATA_LENGTH_LENGTH} bytes.
     */
    long COLUMN_DATA_LENGTH_OFFSET = 8L;

    /**
     * For every table column, the sas7bdat file stores the denotation (in bytes) of data length in a column:
     * - at an offset measured from the beginning of the {@link SasFileParser.ColumnAttributesSubheader} subheader
     * and calculated by the following formula: {@link SasFileConstants#COLUMN_DATA_LENGTH_OFFSET} +
     * + column index * (8 + the size of value type (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE})) +
     * + the size of value type (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE})
     * from the beginning of the {@link SasFileParser.ColumnAttributesSubheader} subheader,
     * - with the length of COLUMN_DATA_LENGTH_LENGTH bytes.
     */
    int COLUMN_DATA_LENGTH_LENGTH = 4;

    /**
     * For every table column, the sas7bdat file stores the data type of a column:
     * - with the length of {@link SasFileConstants#COLUMN_TYPE_LENGTH} bytes.
     * - at an offset measured from the beginning of the {@link SasFileParser.ColumnAttributesSubheader} subheader
     * and calculated by the following formula: COLUMN_TYPE_OFFSET +
     * + column index * (8 + the size of value type (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE})) +
     * + the size of value type (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE})
     * from the beginning of the {@link SasFileParser.ColumnAttributesSubheader} subheader,
     * If type=1, then the column stores numeric values, if type=0, the column stores text.
     */
    long COLUMN_TYPE_OFFSET = 14L;

    /**
     * For every table column, the sas7bdat file stores the data type of a column:
     * - with the length of COLUMN_TYPE_LENGTH bytes.
     * - at an offset measured from the beginning of the {@link SasFileParser.ColumnAttributesSubheader} subheader
     * and calculated by the following formula: {@link SasFileConstants#COLUMN_TYPE_OFFSET} +
     * + column index * (8 + the size of value type (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE})) +
     * + the size of value type (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE})
     * from the beginning of the {@link SasFileParser.ColumnAttributesSubheader} subheader,
     * If type=1, then the column stores numeric values, if type=0, the column stores text.
     */
    int COLUMN_TYPE_LENGTH = 1;

    /**
     * For some table column, the sas7bdat file stores width of format:
     * - with the length of {@link SasFileConstants#COLUMN_FORMAT_WIDTH_OFFSET_LENGTH} bytes,
     * - at an offset calculated as COLUMN_FORMAT_WIDTH_OFFSET bytes + the size of value types
     * (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE}) from
     * the beginning of the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    long COLUMN_FORMAT_WIDTH_OFFSET = 8L;

    /**
     * For some table column, the sas7bdat file stores width of format:
     * - with the length of COLUMN_FORMAT_WIDTH_OFFSET_LENGTH bytes,
     * - at an offset calculated as {@link SasFileConstants#COLUMN_FORMAT_WIDTH_OFFSET bytes} + the size of value types
     * (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE}) from
     * the beginning of the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    int COLUMN_FORMAT_WIDTH_OFFSET_LENGTH = 2;

    /**
     * For some table column, the sas7bdat file stores precision of format:
     * - with the length of {@link SasFileConstants#COLUMN_FORMAT_PRECISION_OFFSET_LENGTH} bytes,
     * - at an offset calculated as COLUMN_FORMAT_PRECISION_OFFSET bytes + the size of value types
     * (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE}) from
     * the beginning of the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    long COLUMN_FORMAT_PRECISION_OFFSET = 10L;

    /**
     * For some table column, the sas7bdat file stores width of format:
     * - with the length of COLUMN_FORMAT_PRECISION_OFFSET_LENGTH bytes,
     * - at an offset calculated as {@link SasFileConstants#COLUMN_FORMAT_PRECISION_OFFSET bytes} +
     * + the size of value type (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE}) from
     * the beginning of the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    int COLUMN_FORMAT_PRECISION_OFFSET_LENGTH = 2;

    /**
     * For every table column, the sas7bdat file stores the index of
     * the {@link SasFileParser.ColumnTextSubheader} whose text block stores the column format:
     * - with the length of {@link SasFileConstants#COLUMN_FORMAT_TEXT_SUBHEADER_INDEX_LENGTH} bytes,
     * - at an offset calculated as COLUMN_FORMAT_TEXT_SUBHEADER_INDEX_OFFSET bytes +
     * + 3 * the size of value types (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE}) from
     * the beginning of the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    long COLUMN_FORMAT_TEXT_SUBHEADER_INDEX_OFFSET = 22L;

    /**
     * For every table column, the sas7bdat file stores the index of the
     * {@link SasFileParser.ColumnTextSubheader} whose text block stores the column format:
     * - with the length of COLUMN_FORMAT_TEXT_SUBHEADER_INDEX_LENGTH bytes,
     * - at an offset calculated as {@link SasFileConstants#COLUMN_FORMAT_TEXT_SUBHEADER_INDEX_OFFSET} bytes +
     * + 3 * the size of value types (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE}) from
     * the beginning of the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    int COLUMN_FORMAT_TEXT_SUBHEADER_INDEX_LENGTH = 2;

    /**
     * For every table column, the sas7bdat file stores the offset (in symbols) of the column format from
     * the beginning of the text block of the {@link SasFileParser.ColumnTextSubheader} subheader
     * where it belongs:
     * - with the length of {@link SasFileConstants#COLUMN_FORMAT_OFFSET_LENGTH} bytes,
     * - at an offset calculated as COLUMN_FORMAT_OFFSET_OFFSET bytes + 3 * the size of value types
     * (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE}) from the beginning of
     * the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    long COLUMN_FORMAT_OFFSET_OFFSET = 24L;

    /**
     * For every table column, the sas7bdat file stores the offset (in symbols) of the column format from
     * the beginning of the text block of the {@link SasFileParser.ColumnTextSubheader}
     * subheader where it belongs:
     * - with the length of COLUMN_FORMAT_OFFSET_LENGTH bytes,
     * - at an offset calculated as {@link SasFileConstants#COLUMN_FORMAT_OFFSET_OFFSET} bytes +
     * + 3 * the size of value types (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE}) from
     * the beginning of the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    int COLUMN_FORMAT_OFFSET_LENGTH = 2;

    /**
     * For every table column, the sas7bdat file stores the column format length (in symbols):
     * - with the length of {@link SasFileConstants#COLUMN_FORMAT_LENGTH_LENGTH} bytes,
     * - at an offset calculated as COLUMN_FORMAT_LENGTH_OFFSET bytes + the size of three value types
     * (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE}) from the beginning of
     * the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    long COLUMN_FORMAT_LENGTH_OFFSET = 26L;

    /**
     * For every table column, the sas7bdat file stores the column format length (in symbols):
     * - with the length of COLUMN_FORMAT_LENGTH_LENGTH bytes,
     * - at an offset calculated as {@link SasFileConstants#COLUMN_FORMAT_LENGTH_OFFSET} bytes +
     * 3 * the size of value types (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE})
     * from the beginning of the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    int COLUMN_FORMAT_LENGTH_LENGTH = 2;

    /**
     * For every table column, the sas7bdat file stores the index of the
     * {@link SasFileParser.ColumnTextSubheader} subheader
     * whose text block contains the column label:
     * - with the length of {@link SasFileConstants#COLUMN_LABEL_TEXT_SUBHEADER_INDEX_LENGTH} bytes,
     * - at an offset calculated as COLUMN_LABEL_TEXT_SUBHEADER_INDEX_OFFSET bytes +
     * + 3 * the size of value types (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE}) from
     * the beginning of the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    long COLUMN_LABEL_TEXT_SUBHEADER_INDEX_OFFSET = 28L;

    /**
     * For every table column, the sas7bdat file stores the index of the
     * {@link SasFileParser.ColumnTextSubheader} subheader
     * whose text block contains the column label:
     * - with the length of COLUMN_LABEL_TEXT_SUBHEADER_INDEX_LENGTH bytes,
     * - at an offset equal to {@link SasFileConstants#COLUMN_LABEL_TEXT_SUBHEADER_INDEX_OFFSET} bytes +
     * + 3 * the size of value types (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE})
     * from the beginning of the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    int COLUMN_LABEL_TEXT_SUBHEADER_INDEX_LENGTH = 2;

    /**
     * For every table column, the sas7bdat file stores the column label`s offset (in symbols) from the beginning of
     * the text block where it belongs (see {@link SasFileConstants#COLUMN_FORMAT_TEXT_SUBHEADER_INDEX_OFFSET}):
     * - with the length of {@link SasFileConstants#COLUMN_LABEL_OFFSET_LENGTH} bytes.
     * - at an offset equal to COLUMN_LABEL_OFFSET_OFFSET bytes + 3 * the size of value types (int or long
     * depending on {@link SasFileConstants#ALIGN_2_VALUE}) from the beginning of
     * the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    long COLUMN_LABEL_OFFSET_OFFSET = 30L;

    /**
     * For every table column, the sas7bdat file stores the column label`s offset (in symbols) from the beginning of
     * the text block where it belongs (see {@link SasFileConstants#COLUMN_FORMAT_TEXT_SUBHEADER_INDEX_OFFSET}):
     * - with the length of COLUMN_LABEL_OFFSET_LENGTH bytes.
     * - at an offset equal to {@link SasFileConstants#COLUMN_LABEL_OFFSET_OFFSET} bytes + 3 * the size
     * of value types(int or long depending on {@link SasFileConstants#ALIGN_2_VALUE}) from the beginning of the
     * {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    int COLUMN_LABEL_OFFSET_LENGTH = 2;

    /**
     * For every table column, the sas7bdat file stores the length of the column label (in symbols):
     * - with the length of {@link SasFileConstants#COLUMN_LABEL_LENGTH_LENGTH} bytes.
     * - at an offset calculated as COLUMN_LABEL_LENGTH_OFFSET bytes +
     * 3 * the size of value types (int or long depending on {@link SasFileConstants#ALIGN_2_VALUE})
     * from the beginning of the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    long COLUMN_LABEL_LENGTH_OFFSET = 32L;

    /**
     * For every table column, the sas7bdat file stores the length of the column label (in symbols):
     * - with the length of COLUMN_LABEL_LENGTH_LENGTH bytes.
     * - at an offset calculated as {@link SasFileConstants#COLUMN_LABEL_LENGTH_OFFSET} bytes +
     * 3 * the size of value types(int or long depending on {@link SasFileConstants#ALIGN_2_VALUE}) from
     * the beginning of the {@link SasFileParser.FormatAndLabelSubheader} subheader.
     */
    int COLUMN_LABEL_LENGTH_LENGTH = 2;

    /**
     * Accuracy to define whether the numeric result of {@link SasFileParser#convertByteArrayToNumber(byte[])} is
     * a long or double value.
     */
    double EPSILON = 1E-14;

    /**
     * Accuracy to define whether the numeric result of {@link SasFileParser#convertByteArrayToNumber(byte[])} is NAN.
     */
    double NAN_EPSILON = 1E-300;

    /**
     * The number of milliseconds in a second.
     */
    long MILLISECONDS_IN_SECONDS = 1000L;

    /**
     * The number of seconds in a minute.
     */
    int SECONDS_IN_MINUTE = 60;

    /**
     * The number of minutes in an hour.
     */
    int MINUTES_IN_HOUR = 60;

    /**
     * The number of hours in a day.
     */
    int HOURS_IN_DAY = 24;

    /**
     * The number of days in a non-leap year.
     */
    int DAYS_IN_YEAR = 365;

    /**
     * The difference in days between 01/01/1960 (the dates starting point in SAS) and 01/01/1970 (the dates starting
     * point in Java).
     */
    int START_DATES_DAYS_DIFFERENCE = DAYS_IN_YEAR * 10 + 3;

    /**
     * The difference in seconds between 01/01/1960 (the dates starting point in SAS) and 01/01/1970 (the dates starting
     * point in Java).
     */
    int START_DATES_SECONDS_DIFFERENCE = SECONDS_IN_MINUTE * MINUTES_IN_HOUR * HOURS_IN_DAY
            * START_DATES_DAYS_DIFFERENCE;

    /**
     * The date formats to store the day, month, and year. Appear in the data of the
     * {@link SasFileParser.FormatAndLabelSubheader} subheader and are stored in {@link com.epam.parso.Column#format}.
     */
    Set<String> DATE_FORMAT_STRINGS = new HashSet<String>(Arrays.asList(
            "B8601DA",
            "E8601DA",
            "DATE",
            "DAY",
            "DDMMYY", "DDMMYYB", "DDMMYYC", "DDMMYYD", "DDMMYYN", "DDMMYYP", "DDMMYYS",
            "WEEKDATE",
            "WEEKDATX",
            "WEEKDAY",
            "DOWNAME",
            "WORDDATE",
            "WORDDATX",
            "YYMM", "YYMMC", "YYMMD", "YYMMN", "YYMMP", "YYMMS",
            "YYMMDD", "YYMMDDB", "YYMMDDC", "YYMMDDD", "YYMMDDN", "YYMMDDP", "YYMMDDS",
            "YYMON",
            "YEAR",
            "JULDAY",
            "JULIAN",
            "MMDDYY", "MMDDYYC", "MMDDYYD", "MMDDYYN", "MMDDYYP", "MMDDYYS",
            "MMYY", "MMYYC", "MMYYD", "MMYYN", "MMYYP", "MMYYS",
            "MONNAME",
            "MONTH",
            "MONYY"
    ));

    /**
     * The date formats to store the day, month, year, hour, minutes, seconds, and milliseconds.
     * Appear in the data of the {@link SasFileParser.FormatAndLabelSubheader} subheader
     * and are stored in {@link com.epam.parso.Column#format}.
     */
    Set<String> DATE_TIME_FORMAT_STRINGS = new HashSet<String>(Arrays.asList(
            "B8601DN", "B8601DT", "B8601DX", "B8601DZ", "B8601LX",
            "E8601DN", "E8601DT", "E8601DX", "E8601DZ", "E8601LX",
            "DATEAMPM",
            "DATETIME",
            "DTDATE",
            "DTMONYY",
            "DTWKDATX",
            "DTMONYY",
            "DTWKDATX",
            "DTYEAR",
            "TOD",
            "MDYAMPM"
    ));
}
