package com.epam.parso.date;

/**
 * Collection of SAS date formats.
 */
enum SasDateFormat  {
    /**
     * Writes date values in the form ddmmmyy or ddmmmyyyy.
     * See: https://v8doc.sas.com/sashtml/lgref/z0195834.htm
     */
    DATE,
    /**
     * Writes date values as the day of the month.
     * See: https://v8doc.sas.com/sashtml/lgref/z0201472.htm
     */
    DAY,
    /**
     * Writes date values in the form ddmmyy or ddmmyyyy.
     * https://v8doc.sas.com/sashtml/lgref/z0197953.htm
     * See also:
     * https://v8doc.sas.com/sashtml/lgref/z0590669.htm
     */
    DDMMYY,
    /**
     * DDMMYYB with a blank separator.
     */
    DDMMYYB,
    /**
     * DDMMYYC with a colon separator.
     */
    DDMMYYC,
    /**
     * DDMMYYD with a dash separator.
     */
    DDMMYYD,
    /**
     * DDMMYY with N indicates no separator.
     * When x is N, the width range is 2-8.
     */
    DDMMYYN,
    /**
     * DDMMYYP with a period separator.
     */
    DDMMYYP,
    /**
     * DDMMYYS with a slash separator.
     */
    DDMMYYS,
    /**
     * Writes date values in the form mmddyy or mmddyyyy.
     * https://v8doc.sas.com/sashtml/lgref/z0199367.htm
     * See also:
     * https://v8doc.sas.com/sashtml/lgref/z0590662.htm
     */
    MMDDYY,
    /**
     * MMDDYYB with a blank separator.
     */
    MMDDYYB,
    /**
     * MMDDYYC with a colon separator.
     */
    MMDDYYC,
    /**
     * MMDDYYD with a dash separator..
     */
    MMDDYYD,
    /**
     * MMDDYY with N indicates no separator.
     * When x is N, the width range is 2-8.
     */
    MMDDYYN,
    /**
     * MMDDYYP with a period separator.
     */
    MMDDYYP,
    /**
     * MMDDYYS with a slash separator.
     */
    MMDDYYS,
    /**
     * Writes date values in the form yymmdd or yyyymmdd.
     * https://v8doc.sas.com/sashtml/lgref/z0197961.htm
     * See also:
     * https://v8doc.sas.com/sashtml/lgref/z0589916.htm
     */
    YYMMDD,
    /**
     * YYMMDDB with a blank separator.
     */
    YYMMDDB,
    /**
     * YYMMDDC with a colon separator.
     */
    YYMMDDC,
    /**
     * YYMMDDD with a dash separator.
     */
    YYMMDDD,
    /**
     * YYMMDD with N indicates no separator.
     * When x is N, the width range is 2-8.
     */
    YYMMDDN,
    /**
     * YYMMDDP with a period separator.
     */
    YYMMDDP,
    /**
     * YYMMDDS with a slash separator.
     */
    YYMMDDS,
    /**
     * Writes date values as the month and the year and separates them with a character.
     * https://v8doc.sas.com/sashtml/lgref/z0199314.htm
     * MMYY with a M separator.
     */
    MMYY,
    /**
     * MMYYC with a colon separator.
     */
    MMYYC,
    /**
     * MMYYD with a dash separator..
     */
    MMYYD,
    /**
     * MMYY with N indicates no separator.
     * When no separator is specified, the width range is 4-32 and the default changes to 6.
     */
    MMYYN,
    /**
     * MMYYP with a period separator.
     */
    MMYYP,
    /**
     * MMYYS with a slash separator.
     */
    MMYYS,
    /**
     * Writes date values as the year and month and separates them with a character.
     * https://v8doc.sas.com/sashtml/lgref/z0199309.htm
     * YYMM with a M separator.
     */
    YYMM,
    /**
     * YYMMC with a colon separator.
     */
    YYMMC,
    /**
     * YYMMD with a dash separator..
     */
    YYMMD,
    /**
     * YYMM with N indicates no separator.
     * When no separator is specified, the width range is 4-32 and the default changes to 6.
     */
    YYMMN,
    /**
     * YYMMP with a period separator.
     */
    YYMMP,
    /**
     * YYMMS with a slash separator.
     */
    YYMMS,
    /**
     * Writes date values as Julian dates in the form yyddd or yyyyddd.
     * See: https://v8doc.sas.com/sashtml/lgref/z0197940.htm
     */
    JULIAN,
    /**
     * Writes date values as the Julian day of the year.
     * See: https://v8doc.sas.com/sashtml/lgref/z0205162.htm
     */
    JULDAY,
    /**
     * Writes date values as the month.
     * See: https://v8doc.sas.com/sashtml/lgref/z0171689.htm
     * Note that MONTH1. returns HEX value.
     */
    MONTH,
    /**
     * Writes date values as the year.
     * See: https://v8doc.sas.com/sashtml/lgref/z0205234.htm
     */
    YEAR,
    /**
     * Writes date values as the month and the year in the form mmmyy or mmmyyyy.
     * See: https://v8doc.sas.com/sashtml/lgref/z0197959.htm
     */
    MONYY,
    /**
     * Writes date values as the year and the month abbreviation.
     * See: https://v8doc.sas.com/sashtml/lgref/z0205240.htm
     */
    YYMON,
    /**
     * Writes date values by using the ISO 8601 basic notation yyyymmdd.
     */
    B8601DA,
    /**
     * Writes date values by using the ISO 8601 extended notation yyyy-mm-dd.
     */
    E8601DA,
    /**
     * Writes date values as the name of the month.
     * See: https://v8doc.sas.com/sashtml/lgref/z0201049.htm
     */
    MONNAME,
    /**
     * Writes date values as the day of the week and the date in the form day-of-week,
     * month-name dd, yy (or yyyy).
     * See: https://v8doc.sas.com/sashtml/lgref/z0201433.htm
     */
    WEEKDATE,
    /**
     * Writes date values as day of week and date in the form day-of-week,
     * dd month-name yy (or yyyy).
     * See: https://v8doc.sas.com/sashtml/lgref/z0201303.htm
     */
    WEEKDATX,
    /**
     * Writes date values as the day of the week.
     * See: https://v8doc.sas.com/sashtml/lgref/z0200757.htm
     */
    WEEKDAY,
    /**
     * Writes date values as the name of the day of the week.
     * See: https://v8doc.sas.com/sashtml/lgref/z0200842.htm
     */
    DOWNAME,
    /**
     * Writes date values as the name of the month,
     * the day, and the year in the form month-name dd, yyyy.
     * See: https://v8doc.sas.com/sashtml/lgref/z0201451.htm
     */
    WORDDATE,
    /**
     * Writes date values as the day, the name of the month,
     * and the year in the form dd month-name yyyy.
     * See: https://v8doc.sas.com/sashtml/lgref/z0201147.htm
     */
    WORDDATX
}
