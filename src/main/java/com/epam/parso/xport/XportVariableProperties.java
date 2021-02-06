package com.epam.parso.xport;

import static com.epam.parso.common.BytesHelper.*;
import static com.epam.parso.xport.impl.XportFileConstants.BYTES_IN_VARIABLE;
import static com.epam.parso.xport.impl.XportFileConstants.DEFAULT_ENDIANNESS;
import static com.epam.parso.xport.impl.XportFileConstants.NAMESTR_LABEL_LENGTH;
import static com.epam.parso.xport.impl.XportFileConstants.NAMESTR_LONG_NAME_LENGTH;

import com.epam.parso.Column;
import com.epam.parso.ColumnFormat;
import com.epam.parso.common.BytesHelper;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * XPORT variable (column) properties.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class XportVariableProperties {

    /**
     * Variable type.
     */
    private VariableType type;

    /**
     * Hash of name (always 0).
     */
    private int nameHash;

    /**
     * Length of variable in observation.
     */
    private int variableLength;

    /**
     * Variable number (column sequential number, starting from 1).
     */
    private int varnum;

    /**
     * Name of variable truncated to 8 characters.
     */
    private String name;

    /**
     * Name of variable (for v8 files only. For v5 it's the same as {@link XportVariableProperties#name}).
     */
    private String longName;

    /**
     * Label of variable.
     * Note that data set labels can be up to 256 characters as of SAS 8, but only the first 40 characters are stored
     * in the XPORT format.
     */
    private String label;

    /**
     * Length of label field (The variable label truncated to 40 characters goes into
     * {@link XportVariableProperties#label}, and the total length of the label goes into
     * this field). For v8 files only.
     */
    private int labelLength;

    /**
     * Full label of variable.
     * For v8 files only.
     */
    private String longLabel;

    /**
     * Name of format.
     */
    private String formatName;

    /**
     * Long name of format  (for v8 files only).
     */
    private String formatDescription;

    /**
     * Format field length or 0.
     */
    private int formatLength;

    /**
     * Format number of decimals.
     */
    private int formatNumberOfDecimals;

    /**
     * 0=left justification, 1=right just.
     */
    private int formatJustification;

    /**
     * Name of input format.
     */
    private String inputFormatName;

    /**
     * Long name of input format  (for v8 files only).
     */
    private String inputFormatDescription;

    /**
     * Informat length attribute.
     */
    private int inputFormatLength;

    /**
     * Informat number of decimals.
     */
    private int inputFormatNumberOfDecimals;

    /**
     * Position of value in observation.
     */
    private long position;

    /**
     * Offset of this variable in a data row (calculated as a sum of 'variableLength' of all previous variables in a
     * row).
     */
    private int variableOffset;

    /**
     * Variable (column) type enum.
     */
    @AllArgsConstructor
    @Getter
    public enum VariableType {

        /**
         * Numeric: long or double.
         */
        NUMERIC(1, Number.class),

        /**
         * Characters (string).
         */
        CHAR(2, String.class);

        /**
         * Code of the type in the input file.
         */
        private final int typeCode;

        /**
         * Corresponding Java class.
         */
        private final Class<?> clazz;

        /**
         * Create enum instance from type code obtained from the input file.
         * @param typeCode type code obtained from the input file.
         * @return corresponding VariableType
         */
        static VariableType fromCode(int typeCode) {
            return Arrays.stream(VariableType.values())
                .filter(v -> typeCode == v.getTypeCode())
                .findAny()
                .orElseThrow(NoSuchElementException::new);
        }
    }

    /**
     * Constructor to build instance of properties from input byte array.
     * @param namestr raw byte array contains variable (column) description.
     * @param version SAS transport file (XPORT) format version.
     */
    public XportVariableProperties(byte[] namestr, XportVersion version) {

        AtomicInteger offset = new AtomicInteger();

        type = VariableType.fromCode(readNextShort(namestr, offset));
        nameHash = readNextShort(namestr, offset);
        variableLength = readNextShort(namestr, offset);
        varnum = readNextShort(namestr, offset);
        name = readNextString(namestr, offset, BYTES_IN_VARIABLE);
        label = readNextString(namestr, offset, NAMESTR_LABEL_LENGTH);
        // this will be overwritten for V8
        longLabel = label;
        formatName = readNextString(namestr, offset, BYTES_IN_VARIABLE);
        formatLength = readNextShort(namestr, offset);
        formatNumberOfDecimals = readNextShort(namestr, offset);
        formatJustification = readNextShort(namestr, offset);
        // (unused, for alignment and future)
        readNextString(namestr, offset, Short.BYTES);
        inputFormatName = readNextString(namestr, offset, BYTES_IN_VARIABLE);
        inputFormatLength = readNextShort(namestr, offset);
        inputFormatNumberOfDecimals = readNextShort(namestr, offset);
        // should it be parsed as IBM number or IEEE 754 number? As IBM number it is always 0, as IEEE the result
        // doesn't look to make sense too as it is too large. Reading as IEEE 754 double is implemented for now. Use
        // BytesHelper.convertIbmByteArrayToNumber instead for reading the number as an IBM double.
        position = BytesHelper.bytesToLong(getSubArray(namestr, offset, Long.BYTES), DEFAULT_ENDIANNESS);
        if (version == XportVersion.VERSION_8) {
            longName = readNextString(namestr, offset, NAMESTR_LONG_NAME_LENGTH);
            labelLength = readNextShort(namestr, offset);
        } else {
            longName = name;
        }
    }

    /**
     * Read next value of short type on current offset, increment offset on read value size.
     * @param namestr raw byte array contains variable (column) description.
     * @param offset position in the input array in bytes to start reading.
     * @return parsed value.
     */
    private int readNextShort(byte[] namestr, AtomicInteger offset) {
        return bytesToShort(getSubArray(namestr, offset, Short.BYTES), DEFAULT_ENDIANNESS);
    }

    /**
     * Read next value of string type on current offset, increment offset on read value size.
     * @param namestr raw byte array contains variable (column) description.
     * @param offset position in the input array in bytes to start reading.
     * @param length of the desired string to read.
     * @return parsed value.
     */
    private String readNextString(byte[] namestr, AtomicInteger offset, int length) {
        return bytesToString(getSubArray(namestr, offset, length)).trim();
    }

    /**
     * Get subarray from source array.
     * @param source source array.
     * @param offset position in the input array in bytes to start reading.
     * @param length length of the output array.
     * @return copy of specified range of the source array.
     */
    private byte[] getSubArray(byte[] source, AtomicInteger offset, int length) {
        int currentOffset = offset.get();
        return Arrays.copyOfRange(source, currentOffset, offset.updateAndGet(o -> currentOffset + length));
    }

    /**
     * Convenience method for compatibility with data writer. Some info from this class is missing in Column class.
     * @return corresponding column object
     */
    public Column toColumn() {
        return new Column(varnum, longName, longLabel, new ColumnFormat(formatName, formatLength,
            formatNumberOfDecimals), type.clazz, variableLength);
    }
}
