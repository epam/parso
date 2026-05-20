package com.epam.parso.date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import static com.epam.parso.date.SasTemporalConstants.*;
import static com.epam.parso.date.SasTemporalUtils.*;

/**
 * Collection of SAS datetime formats.
 * See: https://v8doc.sas.com/sashtml/lgref/z0309859.htm
 * <p>
 * Note that SAS and Java have difference in calendars after the year 4000
 * because of the different amount of leap days after that year.
 * In this implementation week days and week day names are formatted
 * using Java calendar for dates after the 28Feb4000.
 */
enum SasDateTimeFormat implements SasTemporalFormat {
    /**
     * Writes datetime values in the form ddmmmyy:hh:mm:ss.ss.
     * See: https://v8doc.sas.com/sashtml/lgref/z0197923.htm
     */
    DATETIME(16) {
        /**
         * Calculate pattern string based on width, without fractional part.
         * @param width column format width
         * @return pattern
         */
        private String getNoFractionDatePattern(int width) {
            if (width >= 19) {
                return "ddMMMyyyy:HH:mm:ss";
            }
            switch (width) {
                case 7:
                case 8:
                    return "ddMMMyy";
                case 9:
                    return "ddMMMyyyy";
                case 10:
                case 11:
                case 12:
                    return "ddMMMyy:HH";
                case 13:
                case 14:
                case 15:
                    return "ddMMMyy:HH:mm";
                case 16:
                case 17:
                case 18:
                default:
                    return "ddMMMyy:HH:mm:ss";
            }
        }

        @Override
        public String getDatePattern(int width, int precision) {
            int noFractionWidth = width - precision;
            if (noFractionWidth < 16) {
                noFractionWidth = width;
            }

            String noFractionString = getNoFractionDatePattern(noFractionWidth);

            if (precision > 0) {
                int fractionWidth = Math.min(precision, width - 17);
                if (fractionWidth == 0) {
                    return noFractionString + '.';
                } else if (fractionWidth > 0) {
                    return noFractionString + '.' + nChars('S', fractionWidth);
                }
            }
            return noFractionString;
        }

        @Override
        public int getActualPrecision(int width, int precision) {
            return width > getDefaultWidth() ? Math.min(width - getDefaultWidth(), precision) : 0;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            return super.getInternalFormatFunction(width, precision).andThen(String::toUpperCase);
        }
    },
    /**
     * Writes dates from datetime values by using the ISO 8601 basic notation yyyymmdd.
     */
    B8601DN(10) {
        @Override
        public String getDatePattern(int width, int precision) {
            return "yyyyMMdd";
        }
    },
    /**
     * Writes datetime values by using the ISO 8601 basic notation yyyymmddThhmmss<ffffff>.
     */
    B8601DT(19) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return null;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    },
    /**
     * Adjusts a Coordinated Universal Time (UTC) datetime value to the user local date and time.
     * Then, writes the local date and time by using the ISO 8601 datetime
     * and time zone basic notation yyyymmddThhmmss+hhmm.
     */
    B8601DX(26) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return null;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    },
    /**
     * Reads Coordinated Universal Time (UTC) datetime values that are specified using the
     * ISO 8601 datetime basic notation yyyymmddThhmmss+|–hhmm or yyyymmddThhmmss<ffffff>Z.
     */
    B8601DZ(26) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return null;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    },
    /**
     * Writes datetime values as local time by appending a time zone offset difference between the local time and UTC,
     * using the ISO 8601 basic notation yyyymmddThhmmss+|–hhmm.
     */
    B8601LX(26) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return null;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    },
    /**
     * Writes dates from SAS datetime values by using the ISO 8601 extended notation yyyy-mm-dd.
     */
    E8601DN(10) {
        @Override
        public String getDatePattern(int width, int precision) {
            return "yyyy-MM-dd";
        }
    },
    /**
     * Reads datetime values that are specified using the
     * ISO 8601 extended notation yyyy-mm-ddThh:mm:ss.<ffffff>.
     */
    E8601DT(19) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return null;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    },
    /**
     * Adjusts a Coordinated Universal Time (UTC) datetime value to the user local date and time.
     * Then, writes the local date and time by using the ISO 8601 datetime
     * and time zone extended notation yyyy-mm-ddThh:mm:ss+hh:mm.
     */
    E8601DX(26) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return null;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    },
    /**
     * Reads Coordinated Universal Time (UTC) datetime values that are specified using the ISO 8601
     * datetime extended notation yyyy-mm-ddThh:mm:ss+|–hh:mm.<fffff> or yyyy-mm-ddThh:mm:ss.<fffff>Z.
     */
    E8601DZ(26) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return null;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    },
    /**
     * Writes datetime values as local time by appending a time zone offset difference between the local time and UTC,
     * using the ISO 8601 extended notation yyyy-mm-ddThh:mm:ss+|–hh:mm.
     */
    E8601LX(26) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return null;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    },
    /**
     * Writes datetime values in the form ddmmmyy:hh:mm:ss.ss with AM or PM.
     * See: https://v8doc.sas.com/sashtml/lgref/z0196050.htm
     */
    DATEAMPM(19) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return null;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    },
    /**
     * Expects a datetime value as input and writes date values in the form ddmmmyy or ddmmmyyyy.
     */
    DTDATE(7) {
        @Override
        public String getDatePattern(int width, int precision) {
            switch (width) {
                case 9:
                    return "ddMMMyyyy";
                case 5:
                case 6:
                    return "ddMMM";
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
     * Writes the date part of a datetime value as the month and year in the form mmmyy or mmmyyyy.
     */
    DTMONYY(5) {
        @Override
        public String getDatePattern(int width, int precision) {
            switch (width) {
                case 7:
                    return "MMMyyyy";
                case 5:
                case 6:
                default:
                    return "MMMyy";
            }
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            return super.getInternalFormatFunction(width, precision).andThen(String::toUpperCase);
        }
    },
    /**
     * Writes the date part of a SAS datetime value as the day of the week and the date in the form
     * day-of-week, dd month-name yy (or yyyy).
     */
    DTWKDATX(29) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return null;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    },
    /**
     * Writes the date part of a SAS datetime value as the year in the form yy or yyyy.
     */
    DTYEAR(4) {
        @Override
        public String getDatePattern(int width, int precision) {
            switch (width) {
                case 2:
                case 3:
                    return "yy";
                case 4:
                default:
                    return "yyyy";
            }
        }
    },
    /**
     * Writes datetime values in the form mm/dd/yy<yy> hh:mm AM|PM.
     * The year can be either two or four digits.
     */
    MDYAMPM(10) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return null;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    },
    /**
     * Writes the time portion of datetime values in the form hh:mm:ss.ss.
     * See: https://v8doc.sas.com/sashtml/lgref/z0201157.htm
     */
    TOD(8) {
        @Override
        protected String getDatePattern(int width, int precision) {
            return null;
        }

        @Override
        public int getActualPrecision(int width, int precision) {
            return width > getDefaultWidth() ? Math.min(width - getDefaultWidth(), precision) : 0;
        }

        /**
         * Round and truncate SAS seconds to a single day.
         * @param sasSeconds SAS seconds
         * @param precision column format precision
         * @return seconds
         */
        private BigDecimal daySeconds(double sasSeconds, int precision) {
            BigDecimal bigSeconds = roundSeconds(sasSeconds, precision)
                    .abs().remainder(BIG_SECONDS_IN_DAY);
            if (sasSeconds < 0 && bigSeconds.compareTo(BigDecimal.ZERO) > 0) {
                bigSeconds = BIG_SECONDS_IN_DAY.subtract(bigSeconds);
            }
            return bigSeconds;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            return (sasSeconds) -> {
                BigDecimal daySeconds = daySeconds(sasSeconds, precision);
                BigDecimal[] parts = daySeconds.divideAndRemainder(BIG_SECONDS_IN_HOUR);

                int adjustedPrecision = precision;
                int firstTwoDigitsNumberAfterSingleDigit = 10;
                if (parts[0].longValue() >= firstTwoDigitsNumberAfterSingleDigit
                        && precision > 0 && width - precision == 8) {
                    adjustedPrecision = precision - 1;
                    daySeconds = daySeconds(sasSeconds, adjustedPrecision);
                    parts = daySeconds.divideAndRemainder(BIG_SECONDS_IN_HOUR);
                }

                String hh = String.valueOf(parts[0].longValue());
                if (hh.length() == 1 && !(width == 4 || width == 7
                        || (adjustedPrecision > 0 && width - 8 == adjustedPrecision))) {
                    hh = '0' + hh;
                }

                if (hh.length() > width - 3) {
                    return hh;
                } else {
                    parts = parts[1].divideAndRemainder(BIG_SECONDS_IN_MINUTE);
                    String mm = String.valueOf(parts[0].longValue());
                    String hhmm = hh + (mm.length() == 1 ? ":0" : ":") + mm;
                    if (hhmm.length() > width - 3) {
                        return hhmm;
                    } else {
                        String ss = String.valueOf(parts[1].longValue());
                        String hhmmss = hhmm + (ss.length() == 1 ? ":0" : ":") + ss;
                        if (adjustedPrecision == 0 || hhmmss.length() > width - adjustedPrecision) {
                            return hhmmss;
                        } else {
                            adjustedPrecision = Math.min(adjustedPrecision, width - hhmmss.length());
                            String nanos = parts[1].remainder(BigDecimal.ONE).toString()
                                    .substring(1, adjustedPrecision + 2);
                            return hhmmss + nanos;
                        }
                    }
                }
            };
        }
    };
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SasDateTimeFormat.class);

    /**
     * Default format width.
     * In terms of SAS "w specifies the width of the output field".
     */
    private final int defaultWidth;

    /**
     * Enum constructor.
     *
     * @param defaultWidth default width for format
     */
    SasDateTimeFormat(int defaultWidth) {
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
        LOGGER.warn("Note that {}{}.{} format is not yet supported, using DATETIME. instead.",
                name(), width > 0 ? width : "", precision > 0 ? precision : "");
        return DATETIME.getInternalFormatFunction(16, 0);
    }

    @Override
    public Function<Double, String> getInternalFormatFunction(int width, int precision) {
        String datePattern = getDatePattern(width, precision);
        DateTimeFormatter formatter = createDateTimeFormatterFromPattern(datePattern);
        return (sasSeconds) -> formatter.format(sasSecondsToLocalDateTime(sasSeconds, precision));
    }
}
