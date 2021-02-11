package com.epam.parso.date;

import java.util.function.Function;

import static com.epam.parso.date.SasTemporalUtils.nChars;

/**
 * Base interface for date, time and datetime enums.
 */
interface SasTemporalFormat {
    /**
     * Returns default width for the current format.
     *
     * @return default width
     */
    int getDefaultWidth();

    /**
     * Actual width can be less than specified in the format.
     *
     * @param width given width
     * @return actual width
     */
    default int getActualWidth(int width) {
        return width == 0 ? getDefaultWidth() : width;
    }

    /**
     * Actual precision can be less than specified in the format.
     *
     * @param width     actual width
     * @param precision given precision
     * @return actual precision
     */
    default int getActualPrecision(int width, int precision) {
        return width > getDefaultWidth() + 1 ? Math.min(width - getDefaultWidth() - 1, precision) : 0;
    }

    /**
     * Create function for formatting of a valid date using
     * pre-calculated and adjusted width and precision.
     * This function is not responsible to align result using paddings,
     * result is already trimmed.
     *
     * @param width     actual width
     * @param precision actual precision
     * @return format function
     */
    Function<Double, String> getInternalFormatFunction(int width, int precision);

    /**
     * If the current format is not implemented, then fallback format function can be applied.
     *
     * @param width     format width
     * @param precision format precision
     * @return format function
     */
    Function<Double, String> getFallbackFormatFunction(int width, int precision);

    /**
     * Create format function for the given width and precision.
     * When applied this function converts seconds to string representation.
     * In most cases the created function has state (it may have pre-calculated
     * patterns or options for the given width and precision)
     *
     * @param width     column format width
     * @param precision column format precision
     * @param trim      true to keep result trimmed
     * @return format function
     */
    default Function<Double, String> getFormatFunction(int width, int precision, boolean trim) {
        int actualWidth = getActualWidth(width);
        int actualPrecision = getActualPrecision(actualWidth, precision);

        Function<Double, String> internalFormatFunction;
        try {
            internalFormatFunction = getInternalFormatFunction(actualWidth, actualPrecision);
        } catch (NotImplementedException e) {
            return getFallbackFormatFunction(actualWidth, actualPrecision);
        }
        return (seconds) -> {
            if (seconds == null || seconds.isNaN()) {
                return ".";
            } else {
                String result = internalFormatFunction.apply(seconds);
                if (trim) {
                    return result;
                } else {
                    if (actualWidth > result.length()) {
                        return nChars(' ', actualWidth - result.length()) + result;
                    } else {
                        return result;
                    }
                }
            }
        };
    }
}
