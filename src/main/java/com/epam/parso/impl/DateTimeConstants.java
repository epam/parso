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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is an class to store constants for parsing the sas7bdat file.
 */
public interface DateTimeConstants {
    /**
     * These are sas7bdat format references to {@link java.text.SimpleDateFormat} date formats.
     * <p>
     * UNSUPPORTED FORMATS:
     * DTYYQC, PDJULG, PDJULI, QTR, QTRR, WEEKU, WEEKV, WEEKW,
     * YYQ, YYQC, YYQD, YYQN, YYQP, YYQS, YYQR, YYQRC, YYQRD, YYQRN, YYQRP, YYQRS
     */
    Map<String, String> DATE_FORMAT_STRINGS = Collections.unmodifiableMap(new HashMap<String, String>() {
        {
            put("B8601DA", "yyyyMMdd");
            put("E8601DA", "yyyy-MM-dd");
            put("DATE", "ddMMMyyyy");
            put("DAY", "dd");
            put("DDMMYY", "dd/MM/yyyy");
            put("DDMMYYB", "dd MM yyyy");
            put("DDMMYYC", "dd:MM:yyyy");
            put("DDMMYYD", "dd-MM-yyyy");
            put("DDMMYYN", "ddMMyyyy");
            put("DDMMYYP", "dd.MM.yyyy");
            put("DDMMYYS", "dd/MM/yyyy");
            put("JULDAY", "D");
            put("JULIAN", "yyyyD");
            put("MMDDYY", "MM/dd/yyyy");
            put("MMDDYYB", "MM dd yyyy");
            put("MMDDYYC", "MM:dd:yyyy");
            put("MMDDYYD", "MM-dd-yyyy");
            put("MMDDYYN", "MMddyyyy");
            put("MMDDYYP", "MM.dd.yyyy");
            put("MMDDYYS", "MM/dd/yyyy");
            put("MMYY", "MM'M'yyyy");
            put("MMYYC", "MM:yyyy");
            put("MMYYD", "MM-yyyy");
            put("MMYYN", "MMyyyy");
            put("MMYYP", "MM.yyyy");
            put("MMYYS", "MM/yyyy");
            put("MONNAME", "MMMM");
            put("MONTH", "M");
            put("MONYY", "MMMyyyy");
            put("WEEKDATE", "EEEE, MMMM dd, yyyy");
            put("WEEKDATX", "EEEE, dd MMMM, yyyy");
            put("WEEKDAY", "u");
            put("DOWNAME", "EEEE");
            put("WORDDATE", "MMMM d, yyyy");
            put("WORDDATX", "d MMMM yyyy");
            put("YYMM", "yyyy'M'MM");
            put("YYMMC", "yyyy:MM");
            put("YYMMD", "yyyy-MM");
            put("YYMMN", "yyyyMM");
            put("YYMMP", "yyyy.MM");
            put("YYMMS", "yyyy/MM");
            put("YYMMDD", "yyyy-MM-dd");
            put("YYMMDDB", "yyyy MM dd");
            put("YYMMDDC", "yyyy:MM:dd");
            put("YYMMDDD", "yyyy-MM-dd");
            put("YYMMDDN", "yyyyMMdd");
            put("YYMMDDP", "yyyy.MM.dd");
            put("YYMMDDS", "yyyy/MM/dd");
            put("YYMON", "yyyyMMM");
            put("YEAR", "yyyy");
        }
    });

    /**
     * These are sas7bdat format references to {@link java.text.SimpleDateFormat} datetime formats.
     */
    Map<String, String> DATETIME_FORMAT_STRINGS = Collections.unmodifiableMap(new HashMap<String, String>() {
        {
            put("B8601DN", "yyyyMMdd");
            put("B8601DT", "yyyyMMdd'T'HHmmssSSS");
            put("B8601DX", "yyyyMMdd'T'HHmmssZ");
            put("B8601DZ", "yyyyMMdd'T'HHmmssZ");
            put("B8601LX", "yyyyMMdd'T'HHmmssZ");
            put("E8601DN", "yyyy-MM-dd");
            put("E8601DT", "yyyy-MM-dd'T'HH:mm:ss.SSS");
            put("E8601DX", "yyyy-MM-dd'T'HH:mm:ssZ");
            put("E8601DZ", "yyyy-MM-dd'T'HH:mm:ssZ");
            put("E8601LX", "yyyy-MM-dd'T'HH:mm:ssZ");
            put("DATEAMPM", "ddMMMyyyy:HH:mm:ss.SS a");
            put("DATETIME", "ddMMMyyyy:HH:mm:ss.SS");
            put("DTDATE", "ddMMMyyyy");
            put("DTMONYY", "MMMyyyy");
            put("DTWKDATX", "EEEE, dd MMMM, yyyy");
            put("DTYEAR", "yyyy");
            put("MDYAMPM", "MM/dd/yyyy H:mm a");
            put("TOD", "HH:mm:ss.SS");
        }
    });

    /**
     * These are time formats that are used in sas7bdat files.
     */
    Set<String> TIME_FORMAT_STRINGS = new HashSet<>(Arrays.asList(
            "TIME", "HHMM", "E8601LZ", "E8601TM", "HOUR", "MMSS", "TIMEAMPM"
    ));
}
