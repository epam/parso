package com.epam.parso.date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.function.Function;

import static com.epam.parso.date.SasTemporalConstants.*;
import static com.epam.parso.date.SasTemporalUtils.nChars;
import static com.epam.parso.date.SasTemporalUtils.roundSeconds;
import static java.math.RoundingMode.HALF_UP;

/**
 * Collection of SAS time formats.
 * See: https://v8doc.sas.com/sashtml/lgref/z0309859.htm
 */
enum SasTimeFormat implements SasTemporalFormat {
    /**
     * Writes time values as hours, minutes, and seconds in the form hh:mm:ss.ss.
     * See: https://v8doc.sas.com/sashtml/lgref/z0197928.htm
     */
    TIME(8) {
        /**
         * Sometimes rounding based on the given precision increases number of hours and
         * it affects the final width of the time representation.
         * So, seconds should be recalculated and precision may be changed accordingly
         * to fit the time representation into the given width.
         * <p>
         * See example:
         * TIME12.4      TIME11.3     TIME10.2    TIME10.1    TIME10.     TIME9.1
         * 9:59:59.9321  9:59:59.932  9:59:59.93  _9:59:59.9  __10:00:00  9:59:59.9
         * 9:59:59.9875  9:59:59.988  9:59:59.99  10:00:00:0  __10:00:00  _10:00:00
         * 9:59:59.9987  9:59:59.999  10:00:00.0  10:00:00:0  __10:00:00  _10:00:00
         * 9:59:59.9999  10:00:00.00  10:00:00.0  10:00:00:0  __10:00:00  _10:00:00
         *
         * @param sasSeconds       SAS seconds
         * @param width            column format width
         * @param precision        column format precision, adjusted according to actual size
         * @param minIntegralWidth width to fit concatenation of integral part "hours:minutes:seconds"
         * @return integral and remainder parts of dividing given seconds by seconds in a hour.
         */
        private BigDecimal[] roundSeconds(double sasSeconds, int width, int precision, int minIntegralWidth) {
            BigDecimal bigSeconds = new BigDecimal(sasSeconds).abs();

            while (true) {
                BigDecimal[] parts = bigSeconds.setScale(precision, HALF_UP)
                        .divideAndRemainder(BIG_SECONDS_IN_HOUR);
                if (precision == 0) {
                    return parts;
                }
                String hh = String.valueOf(parts[0].longValue());
                if (hh.length() + minIntegralWidth + precision <= width) {
                    return parts;
                }
                precision--;
            }
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            return (sasSeconds) -> {
                boolean negative = sasSeconds < 0;
                int minIntegralWidth = negative ? "-0:00:00".length() : "0:00:00".length();

                int actualPrecision = width > minIntegralWidth ? Math.min(width - minIntegralWidth, precision) : 0;

                BigDecimal[] parts = roundSeconds(sasSeconds, width, actualPrecision, minIntegralWidth);


                String hh = String.valueOf(parts[0].longValue());
                if (negative) {
                    hh = "-" + hh;
                }
                if (hh.length() > width) {
                    return nChars('*', width);
                } else if (hh.length() > width - 3) {
                    return hh;
                } else {
                    parts = parts[1].divideAndRemainder(BIG_MINUTES_IN_HOUR);
                    String mm = String.valueOf(parts[0].longValue());
                    String hhmm = hh + (mm.length() > 1 ? ":" : ":0") + mm;
                    if (hhmm.length() > width - 3) {
                        return hhmm;
                    } else {
                        String ss = parts[1].toString();
                        return hhmm + (ss.length() > 1 && ss.charAt(1) != '.' ? ":" : ":0") + ss;
                    }
                }
            };
        }
    },
    /**
     * Writes time values as the number of minutes and seconds since midnight.
     * See: https://v8doc.sas.com/sashtml/lgref/z0198053.htm
     */
    MMSS(5) {
        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            return (sasSeconds) -> {
                boolean negative = sasSeconds < 0;

                BigDecimal[] parts = new BigDecimal(sasSeconds).abs()
                        .setScale(precision, HALF_UP)
                        .divideAndRemainder(BIG_SECONDS_IN_MINUTE);

                String mm = String.valueOf(parts[0].longValue());
                if (negative) {
                    mm = "-" + mm;
                }
                if (mm.length() > width) {
                    return "**";
                } else if (mm.length() > width - 3) {
                    return mm;
                } else {
                    String ss = parts[1].toString();
                    String mmss = mm + (ss.length() > 1 && ss.charAt(1) != '.' ? ":" : ":0") + ss;
                    if (mmss.length() > width) {
                        return mmss.substring(0, width);
                    } else {
                        return mmss;
                    }
                }
            };
        }
    },
    /**
     * Writes time values as hours and minutes in the form hh:mm.
     * See: https://v8doc.sas.com/sashtml/lgref/z0198049.htm
     */
    HHMM(5) {
        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            return (sasSeconds) -> {
                boolean negative = sasSeconds < 0;

                BigDecimal[] parts = new BigDecimal(sasSeconds).abs()
                        .divide(BIG_SECONDS_IN_MINUTE, precision, BigDecimal.ROUND_HALF_UP)
                        .divideAndRemainder(BIG_MINUTES_IN_HOUR);

                String hh = String.valueOf(parts[0].longValue());
                if (negative) {
                    hh = "-" + hh;
                }
                if (hh.length() > width) {
                    return "**";
                } else if (hh.length() > width - 3) {
                    return hh;
                } else {
                    String mm = parts[1].toString();
                    String hhmm = hh + (mm.length() > 1 && mm.charAt(1) != '.' ? ":" : ":0") + mm;
                    if (hhmm.length() > width) {
                        return hhmm.substring(0, width);
                    } else {
                        return hhmm;
                    }
                }
            };
        }
    },

    /**
     * Writes time values as hours and decimal fractions of hours.
     * See: https://v8doc.sas.com/sashtml/lgref/z0198051.htm
     */
    HOUR(2) {
        /**
         * Round seconds to hours.
         *
         * @param sasSeconds SAS seconds
         * @param width      column format width
         * @param precision  column format precision
         * @return rounded seconds
         */
        private BigDecimal roundAdjustHours(double sasSeconds, int width, int precision) {
            BigDecimal bigSeconds = BigDecimal.valueOf(sasSeconds / SECONDS_IN_HOUR).abs();
            BigDecimal hours = bigSeconds.setScale(precision, HALF_UP);
            int adjustedPrecision = precision;

            while (adjustedPrecision > 0 && hours.toString().length() > width) {
                if (hours.longValue() == 0 && width - precision == 1) {
                    // special case for format like ".123" without leading zero.
                    break;
                }
                adjustedPrecision--;
                hours = bigSeconds.setScale(adjustedPrecision, HALF_UP);
            }
            return hours;
        }

        /**
         * Try to store large hours as a number in E-notation.
         *
         * @param hours hours
         * @param width column format width
         * @return hours representation
         */
        private String eNotation(BigDecimal hours, int width) {
            int i = 0;
            String hh = null;
            DecimalFormat decimalFormat;
            while (true) {
                if (i == 0) {
                    decimalFormat = new DecimalFormat("0E0");
                } else {
                    decimalFormat = new DecimalFormat("#" + nChars('0', i) + "E0");
                }
                i++;
                String tmp = decimalFormat.format(hours);
                if (tmp.length() > width) {
                    break;
                } else {
                    hh = tmp;
                }
            }
            return hh;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            return (sasSeconds) -> {
                BigDecimal hours = roundAdjustHours(sasSeconds, width, precision);

                if (precision > 0 && hours.longValue() == 0 && width - precision == 1) {
                    return hours.toString().substring(1);
                }

                String hh = hours.toString();
                if (width > 2 && hh.length() > width) {
                    hh = eNotation(hours, width);
                }

                if (hh == null || hh.length() > width) {
                    return "**";
                } else {
                    return hh;
                }
            };
        }
    },
    /**
     * Writes time values as hours, minutes, and seconds in the form hh:mm:ss.ss with AM or PM.
     * See: https://v8doc.sas.com/sashtml/lgref/z0201272.htm
     */
    TIMEAMPM(11) {
        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    },
    /**
     * Writes time values as local time, appending the Coordinated Universal Time (UTC) offset
     * for the local SAS session, using the ISO 8601 extended time notation hh:mm:ss+|–hh:mm.
     */
    E8601LZ(0) {
        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            throw new NotImplementedException();
        }
    },
    /**
     * Writes time values by using the ISO 8601 extended notation hh:mm:ss.ffffff.
     */
    E8601TM(8) {
        @Override
        public int getActualPrecision(int width, int precision) {
            return width > getDefaultWidth() ? Math.min(width - getDefaultWidth(), precision) : 0;
        }

        @Override
        public Function<Double, String> getInternalFormatFunction(int width, int precision) {
            return (sasSeconds) -> {
                if (sasSeconds < 0 || sasSeconds > SECONDS_IN_DAY) {
                    return nChars('*', width);
                }

                BigDecimal daySeconds = roundSeconds(sasSeconds, precision)
                        .remainder(BIG_SECONDS_IN_DAY);
                BigDecimal[] parts = daySeconds.divideAndRemainder(BIG_SECONDS_IN_HOUR);

                int adjustedPrecision = precision;
                int firstTwoDigitsNumberAfterSingleDigit = 10;
                if (parts[0].longValue() >= firstTwoDigitsNumberAfterSingleDigit
                        && precision > 0 && width - precision == 8) {
                    adjustedPrecision = precision - 1;
                    daySeconds = roundSeconds(sasSeconds, adjustedPrecision)
                            .remainder(BIG_SECONDS_IN_DAY);
                    parts = daySeconds.divideAndRemainder(BIG_SECONDS_IN_HOUR);
                }

                String hh = String.valueOf(parts[0].longValue());
                if (hh.length() == 1 && !(width == 4 || width == 7
                        || (adjustedPrecision > 0 && width - 8 == adjustedPrecision))) {
                    hh = '0' + hh;
                }

                parts = parts[1].divideAndRemainder(BIG_SECONDS_IN_MINUTE);
                String mm = String.valueOf(parts[0].longValue());
                String hhmm = hh + (mm.length() == 1 ? ":0" : ":") + mm;

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
            };
        }
    };

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SasTimeFormat.class);

    /**
     * Default column format width.
     */
    private final int defaultWidth;

    /**
     * Enum constructor.
     *
     * @param defaultWidth default width for format
     */
    SasTimeFormat(int defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    @Override
    public final int getDefaultWidth() {
        return defaultWidth;
    }

    @Override
    public int getActualPrecision(int width, int precision) {
        return precision;
    }

    @Override
    public final Function<Double, String> getFallbackFormatFunction(int width, int precision) {
        LOGGER.warn("Note that {}{}.{} format is not yet supported, using TIME. instead.",
                name(), width > 0 ? width : "", precision > 0 ? precision : "");
        return TIME.getInternalFormatFunction(8, 0);
    }
}
