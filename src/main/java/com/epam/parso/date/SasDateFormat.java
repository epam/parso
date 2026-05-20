package com.epam.parso.date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import static com.epam.parso.date.SasTemporalUtils.createDateTimeFormatterFromPattern;
import static com.epam.parso.date.SasTemporalUtils.sasDaysToLocalDate;

/**
 * Collection of SAS date formats.
 * See: https://v8doc.sas.com/sashtml/lgref/z0309859.htm
 * <p>
 * Note that SAS and Java have difference in calendars after the year 4000
 * because of the different amount of leap days after that year.
 * In this implementation week days and week day names are formatted
 * using Java calendar for dates after the 28Feb4000.
 */
enum SasDateFormat implements SasTemporalFormat {
    /**
     * Writes date values in the form ddmmmyy or ddmmmyyyy.
     * See: https://v8doc.sas.com/sashtml/lgref/z0195834.htm
     * Actually SAS also supports DATE10 (same result as DATE9)
     * and DATE11 (with dash as separator).
     */
    DATE(7) {
        @Override
        protected String getDatePattern(int width, int precision) {
            switch (width) {
                case 5:
                case 6:
                    return "ddMMM";
                case 9:
                case 10:
                    return "ddMMMyyyy";
                case 11:
                    return "dd-MMM-yyyy";
                case 7:
                case 8:
                default:
                    return "ddMMMyy";
            }
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            return super.getInternalFormatFunction(width, precision).andThen(String::toUpperCase);
        }
    },
    /**
     * Writes date values as the day of the month.
     * See: https://v8doc.sas.com/sashtml/lgref/z0201472.htm
     */
    DAY(2) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return "d";
        }
    },
    /**
     * Writes date values in the form ddmmyy or ddmmyyyy.
     * https://v8doc.sas.com/sashtml/lgref/z0197953.htm
     * See also:
     * https://v8doc.sas.com/sashtml/lgref/z0590669.htm
     */
    DDMMYY(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getDDMMYYxwFormatPattern(width, "/");
        }
    },
    /**
     * DDMMYYB with a blank separator.
     */
    DDMMYYB(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getDDMMYYxwFormatPattern(width, " ");
        }
    },
    /**
     * DDMMYYC with a colon separator.
     */
    DDMMYYC(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getDDMMYYxwFormatPattern(width, ":");
        }
    },
    /**
     * DDMMYYD with a dash separator.
     */
    DDMMYYD(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getDDMMYYxwFormatPattern(width, "-");
        }
    },
    /**
     * DDMMYY with N indicates no separator.
     * When x is N, the width range is 2-8.
     */
    DDMMYYN(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getDDMMYYxwFormatPattern(width, "");
        }
    },
    /**
     * DDMMYYP with a period separator.
     */
    DDMMYYP(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getDDMMYYxwFormatPattern(width, ".");
        }
    },
    /**
     * DDMMYYS with a slash separator.
     */
    DDMMYYS(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getDDMMYYxwFormatPattern(width, "/");
        }
    },
    /**
     * Writes date values in the form mmddyy or mmddyyyy.
     * https://v8doc.sas.com/sashtml/lgref/z0199367.htm
     * See also:
     * https://v8doc.sas.com/sashtml/lgref/z0590662.htm
     */
    MMDDYY(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getMMDDYYxwFormatPattern(width, "/");
        }
    },
    /**
     * MMDDYYB with a blank separator.
     */
    MMDDYYB(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getMMDDYYxwFormatPattern(width, " ");
        }
    },
    /**
     * MMDDYYC with a colon separator.
     */
    MMDDYYC(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getMMDDYYxwFormatPattern(width, ":");
        }
    },
    /**
     * MMDDYYD with a dash separator.
     */
    MMDDYYD(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getMMDDYYxwFormatPattern(width, "-");
        }
    },
    /**
     * MMDDYY with N indicates no separator.
     * When x is N, the width range is 2-8.
     */
    MMDDYYN(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getMMDDYYxwFormatPattern(width, "");
        }
    },
    /**
     * MMDDYYP with a period separator.
     */
    MMDDYYP(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getMMDDYYxwFormatPattern(width, ".");
        }
    },
    /**
     * MMDDYYS with a slash separator.
     */
    MMDDYYS(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getMMDDYYxwFormatPattern(width, "/");
        }
    },
    /**
     * Writes date values in the form yymmdd or yyyymmdd.
     * https://v8doc.sas.com/sashtml/lgref/z0197961.htm
     * See also:
     * https://v8doc.sas.com/sashtml/lgref/z0589916.htm
     */
    YYMMDD(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getYYMMDDxwFormatPattern(width, "-");
        }
    },
    /**
     * YYMMDDB with a blank separator.
     */
    YYMMDDB(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getYYMMDDxwFormatPattern(width, " ");
        }
    },
    /**
     * YYMMDDC with a colon separator.
     */
    YYMMDDC(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getYYMMDDxwFormatPattern(width, ":");
        }
    },
    /**
     * YYMMDDD with a dash separator.
     */
    YYMMDDD(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getYYMMDDxwFormatPattern(width, "-");
        }
    },
    /**
     * YYMMDD with N indicates no separator.
     * When x is N, the width range is 2-8.
     */
    YYMMDDN(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getYYMMDDxwFormatPattern(width, "");
        }
    },
    /**
     * YYMMDDP with a period separator.
     */
    YYMMDDP(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getYYMMDDxwFormatPattern(width, ".");
        }
    },
    /**
     * YYMMDDS with a slash separator.
     */
    YYMMDDS(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getYYMMDDxwFormatPattern(width, "/");
        }
    },
    /**
     * Writes date values as the month and the year and separates them with a character.
     * https://v8doc.sas.com/sashtml/lgref/z0199314.htm
     * MMYY with a M separator.
     */
    MMYY(7) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getMMYYxwFormatPattern(width, "'M'");
        }
    },
    /**
     * MMYYC with a colon separator.
     */
    MMYYC(7) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getMMYYxwFormatPattern(width, ":");
        }
    },
    /**
     * MMYYD with a dash separator.
     */
    MMYYD(7) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getMMYYxwFormatPattern(width, "-");
        }
    },
    /**
     * MMYY with N indicates no separator.
     * When no separator is specified, the width range is 4-32 and the default changes to 6.
     */
    MMYYN(6) {
        @Override
        protected String getDatePattern(int width, int precision) {
            switch (width) {
                case 4:
                case 5:
                    return "MMyy";
                default:
                    return "MMyyyy";
            }
        }
    },
    /**
     * MMYYP with a period separator.
     */
    MMYYP(7) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getMMYYxwFormatPattern(width, ".");
        }
    },
    /**
     * MMYYS with a slash separator.
     */
    MMYYS(7) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getMMYYxwFormatPattern(width, "/");
        }
    },
    /**
     * Writes date values as the year and month and separates them with a character.
     * https://v8doc.sas.com/sashtml/lgref/z0199309.htm
     * YYMM with a M separator.
     */
    YYMM(7) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getYYMMxwFormatPattern(width, "'M'");
        }
    },
    /**
     * YYMMC with a colon separator.
     */
    YYMMC(7) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getYYMMxwFormatPattern(width, ":");
        }
    },
    /**
     * YYMMD with a dash separator.
     */
    YYMMD(7) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getYYMMxwFormatPattern(width, "-");
        }
    },
    /**
     * YYMM with N indicates no separator.
     * When no separator is specified, the width range is 4-32 and the default changes to 6.
     */
    YYMMN(6) {
        @Override
        protected String getDatePattern(int width, int precision) {
            switch (width) {
                case 4:
                case 5:
                    return "yyMM";
                default:
                    return "yyyyMM";
            }
        }
    },
    /**
     * YYMMP with a period separator.
     */
    YYMMP(7) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getYYMMxwFormatPattern(width, ".");
        }
    },
    /**
     * YYMMS with a slash separator.
     */
    YYMMS(7) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return getYYMMxwFormatPattern(width, "/");
        }
    },
    /**
     * Writes date values as Julian dates in the form yyddd or yyyyddd.
     * See: https://v8doc.sas.com/sashtml/lgref/z0197940.htm
     */
    JULIAN(5) {
        @Override
        protected String getDatePattern(int width, int precision) {
            if (width == 7) {
                return "yyyyDDD";
            } else {
                return "yyDDD";
            }
        }
    },
    /**
     * Writes date values as the Julian day of the year.
     * See: https://v8doc.sas.com/sashtml/lgref/z0205162.htm
     */
    JULDAY(3) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return "D";
        }
    },
    /**
     * Writes date values as the month.
     * See: https://v8doc.sas.com/sashtml/lgref/z0171689.htm
     * Note that MONTH1. returns HEX value.
     */
    MONTH(2) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return "M";
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            if (width == 1) {
                return (sasDays) -> {
                    LocalDate date = sasDaysToLocalDate(sasDays);
                    return String.format("%X", date.getMonthValue());
                };
            } else {
                return super.getInternalFormatFunction(width, precision).andThen(String::toUpperCase);
            }
        }
    },
    /**
     * Writes date values as the year.
     * See: https://v8doc.sas.com/sashtml/lgref/z0205234.htm
     */
    YEAR(4) {
        @Override
        protected String getDatePattern(int width, int precision) {
            switch (width) {
                case 2:
                case 3:
                    return "yy";
                default:
                    return "yyyy";
            }
        }
    },
    /**
     * Writes date values as the month and the year in the form mmmyy or mmmyyyy.
     * See: https://v8doc.sas.com/sashtml/lgref/z0197959.htm
     */
    MONYY(5) {
        @Override
        protected String getDatePattern(int width, int precision) {
            if (width == 7) {
                return "MMMyyyy";
            } else {
                return "MMMyy";
            }
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            return super.getInternalFormatFunction(width, precision).andThen(String::toUpperCase);
        }
    },
    /**
     * Writes date values as the year and the month abbreviation.
     * See: https://v8doc.sas.com/sashtml/lgref/z0205240.htm
     */
    YYMON(7) {
        @Override
        protected String getDatePattern(int width, int precision) {
            switch (width) {
                case 5:
                case 6:
                    return "yyMMM";
                default:
                    return "yyyyMMM";
            }
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            return super.getInternalFormatFunction(width, precision).andThen(String::toUpperCase);
        }
    },
    /**
     * Writes date values by using the ISO 8601 basic notation yyyymmdd.
     */
    B8601DA(10) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return "yyyyMMdd";
        }
    },
    /**
     * Writes date values by using the ISO 8601 extended notation yyyy-mm-dd.
     */
    E8601DA(10) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return "yyyy-MM-dd";
        }
    },
    /**
     * Writes date values as the name of the month.
     * See: https://v8doc.sas.com/sashtml/lgref/z0201049.htm
     */
    MONNAME(9) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return "MMMM";
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            return super.getInternalFormatFunction(width, precision).andThen(s ->
                    s.substring(0, Math.min(s.length(), width)));
        }
    },
    /**
     * Writes date values as the day of the week and the date in the form day-of-week,
     * month-name dd, yy (or yyyy).
     * See: https://v8doc.sas.com/sashtml/lgref/z0201433.htm
     */
    WEEKDATE(29) {
        @Override
        protected String getDatePattern(int width, int precision) {
            switch (width) {
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    return "EEE";
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                    return "EEEE";
                case 15:
                case 16:
                    return "EEE, MMM d, yy";
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                    return "EEE, MMM d, yyyy";
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                    return "EEEE, MMM d, yyyy";
                default:
                    return "EEEE, MMMM d, yyyy";
            }
        }
    },
    /**
     * Writes date values as day of week and date in the form day-of-week,
     * dd month-name yy (or yyyy).
     * See: https://v8doc.sas.com/sashtml/lgref/z0201303.htm
     */
    WEEKDATX(29) {
        @Override
        protected String getDatePattern(int width, int precision) {
            switch (width) {
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    return "EEE";
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                    return "EEEE";
                case 15:
                case 16:
                    return "EEE, d MMM yy";
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                    return "EEE, d MMM yyyy";
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                    return "EEEE, d MMM yyyy";
                default:
                    return "EEEE, d MMMM yyyy";
            }
        }
    },
    /**
     * Writes date values as the day of the week.
     * See: https://v8doc.sas.com/sashtml/lgref/z0200757.htm
     * <p>
     * Note that SAS Universal Viewer and SAS System render
     * weekdays differently (at least for 1582 year).
     * See the difference:
     * <p>
     * Raw Value   SAS Viewer  SAS System
     * -138061      *           *
     * -138060      0           0
     * -138059      1           1
     * -138058      *           *
     * -138057      *           *
     * -138056      *           *
     * -138055      *           *
     * -138054      *           *
     * -138053      0           0
     * -138052      1           1
     * -138051      *           2
     * -138050      *           3
     * -138049      *           4
     * -138048      *           5
     * -138047      *           6
     * <p>
     * This SAS bug can't be recreated in Parso,
     * because it is not clear in which SAS system this bug is "more canonical".
     */
    WEEKDAY(1) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return "e";
        }
    },
    /**
     * Writes date values as the name of the day of the week.
     * See: https://v8doc.sas.com/sashtml/lgref/z0200842.htm
     */
    DOWNAME(9) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return "EEEE";
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            return super.getInternalFormatFunction(width, precision).andThen(s ->
                    s.substring(0, Math.min(s.length(), width)));
        }
    },
    /**
     * Writes date values as the name of the month,
     * the day, and the year in the form month-name dd, yyyy.
     * See: https://v8doc.sas.com/sashtml/lgref/z0201451.htm
     */
    WORDDATE(18) {
        @Override
        protected String getDatePattern(int width, int precision) {
            switch (width) {
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    return "MMM";
                case 9:
                case 10:
                case 11:
                    return "MMMM";
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                    return "MMM d, yyyy";
                default:
                    return "MMMM d, yyyy";
            }
        }
    },
    /**
     * Writes date values as the day, the name of the month,
     * and the year in the form dd month-name yyyy.
     * See: https://v8doc.sas.com/sashtml/lgref/z0201147.htm
     */
    WORDDATX(18) {
        @Override
        protected String getDatePattern(int width, int precision) {
            switch (width) {
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    return "MMM";
                case 9:
                case 10:
                case 11:
                    return "MMMM";
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                    return "d MMM yyyy";
                default:
                    return "d MMMM yyyy";
            }
        }
    },
    /**
     * Writes date values as the quarter of the year.
     * See: https://v8doc.sas.com/sashtml/lgref/z0201232.htm
     */
    QTR(0) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return null;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    };


    /**
     * Common method to format DDMMYY[B, C, D, N, P, S] dates.
     * See: https://v8doc.sas.com/sashtml/lgref/z0590669.htm
     *
     * @param width     date width
     * @param separator B: blank, C: colon, D: dash, P:period, S: slash
     *                  and N: for no separator
     * @return date pattern
     */
    private static String getDDMMYYxwFormatPattern(int width, String separator) {
        switch (width) {
            case 2:
            case 3:
                return "dd";
            case 4:
                return "ddMM";
            case 5:
                return "dd" + separator + "MM";
            case 6:
            case 7:
                return "ddMMyy";
            case 10:
                return "dd" + separator + "MM" + separator + "yyyy";
            default:
                if (separator.isEmpty()) {
                    return "ddMMyyyy";
                } else {
                    return "dd" + separator + "MM" + separator + "yy";
                }
        }
    }

    /**
     * Common method to format MMDDYY[B, C, D, N, P, S] dates.
     * See: https://v8doc.sas.com/sashtml/lgref/z0590662.htm
     *
     * @param width     date width
     * @param separator B: blank, C: colon, D: dash, P:period, S: slash
     *                  and N: for no separator
     * @return date pattern
     */
    private static String getMMDDYYxwFormatPattern(int width, String separator) {
        switch (width) {
            case 2:
            case 3:
                return "MM";
            case 4:
                return "MMdd";
            case 5:
                return "MM" + separator + "dd";
            case 6:
            case 7:
                return "MMddyy";
            case 10:
                return "MM" + separator + "dd" + separator + "yyyy";
            default:
                if (separator.isEmpty()) {
                    return "MMddyyyy";
                } else {
                    return "MM" + separator + "dd" + separator + "yy";
                }
        }
    }

    /**
     * Common method to format YYMMDD[B, C, D, N, P, S] dates.
     * See: https://v8doc.sas.com/sashtml/lgref/z0589916.htm
     *
     * @param width     date width
     * @param separator B: blank, C: colon, D: dash, P:period, S: slash
     *                  and N: for no separator
     * @return date pattern
     */
    private static String getYYMMDDxwFormatPattern(int width, String separator) {
        switch (width) {
            case 2:
            case 3:
                return "yy";
            case 4:
                return "yyMM";
            case 5:
                return "yy" + separator + "MM";
            case 6:
            case 7:
                return "yyMMdd";
            case 10:
                return "yyyy" + separator + "MM" + separator + "dd";
            default:
                if (separator.isEmpty()) {
                    return "yyyyMMdd";
                } else {
                    return "yy" + separator + "MM" + separator + "dd";
                }
        }
    }

    /**
     * Common method to format MMYY[C, D, N, P, S] dates.
     * See: https://v8doc.sas.com/sashtml/lgref/z0199314.htm
     *
     * @param width     date width
     * @param separator 'M' by default, C: colon, D: dash, P:period, S: slash
     * @return date pattern
     */
    private static String getMMYYxwFormatPattern(int width, String separator) {
        switch (width) {
            case 5:
            case 6:
                return "MM" + separator + "yy";
            default:
                return "MM" + separator + "yyyy";
        }
    }

    /**
     * Common method to format YYMM[C, D, N, P, S] dates.
     * See: https://v8doc.sas.com/sashtml/lgref/z0199309.htm
     *
     * @param width     date width
     * @param separator 'M' by default, C: colon, D: dash, P:period, S: slash
     * @return date pattern
     */
    private static String getYYMMxwFormatPattern(int width, String separator) {
        switch (width) {
            case 5:
            case 6:
                return "yy" + separator + "MM";
            default:
                return "yyyy" + separator + "MM";
        }
    }

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SasDateFormat.class);

    /**
     * Default format width.
     * In terms of SAS: "w specifies the width of the output field".
     */
    private final int defaultWidth;

    /**
     * Enum constructor.
     *
     * @param defaultWidth default width for format
     */
    SasDateFormat(int defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    @Override
    public final int getDefaultWidth() {
        return defaultWidth;
    }

    /**
     * Creates width-specific date pattern compatible with the java.time.format.DateTimeFormatter.
     *
     * @param width     column format width
     * @param precision column format precision
     * @return java date pattern
     */
    protected abstract String getDatePattern(int width, int precision);

    @Override
    public final Function<Double, String> getFallbackFormatFunction(int width, int precision) {
        LOGGER.warn("Note that {}{}.{} format is not yet supported, using DATE. instead.",
                name(), width > 0 ? width : "", precision > 0 ? precision : "");
        return DATE.getInternalFormatFunction(7, 0);
    }

    @Override
    public Function<Double, String> getInternalFormatFunction(int width, int precision) {
        String datePattern = getDatePattern(width, precision);
        DateTimeFormatter formatter = createDateTimeFormatterFromPattern(datePattern);
        return (sasDays) -> {
            LocalDate date = sasDaysToLocalDate(sasDays);
            return formatter.format(date);
        };
    }
}
