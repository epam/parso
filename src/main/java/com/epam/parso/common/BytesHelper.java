package com.epam.parso.common;

import static com.epam.parso.common.ParserMessageConstants.EMPTY_INPUT_STREAM;
import static com.epam.parso.impl.SasFileConstants.BYTES_IN_DOUBLE;
import static java.util.stream.Collectors.toCollection;

import com.epam.parso.impl.SasFileConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Helper for byte reading operations.
 */
public final class BytesHelper {

    /**
     * Private constructor for utility class.
     */
    private BytesHelper() {
    }

    /**
     * Missing values are written out with the first byte (the exponent) indicating
     * the proper missing values. All subsequent bytes are 0x00. The first byte is:
     * type byte
     * ._ 0x5f
     * . 0x2e
     * .A 0x41
     * .B 0x42
     * ....
     * .Z 0x5a
     */
    public static final Set<Byte> XPORT_MISSING_VALUES =
        IntStream.rangeClosed(0x41, 0x5a)
            .mapToObj(val -> (byte) val)
            .collect(toCollection(HashSet::new));
    static {
        XPORT_MISSING_VALUES.add((byte) 0x5f);
        XPORT_MISSING_VALUES.add((byte) 0x2e);
    }

    /**
     * The function to convert an array of bytes into a numeral of the {@link Short} type.
     * For convenience, the resulting number is converted into the int type.
     *
     * @param bytes a long number represented by an array of bytes.
     * @param endianness  1 for the little-endian sequence, 0 for big-endian.
     * @return a number of the int type that is the conversion result.
     */
    public static int bytesToShort(byte[] bytes, int endianness) {
        return byteArrayToByteBuffer(bytes, endianness).getShort();
    }

    /**
     * The function to convert an array of bytes into an int number.
     *
     * @param bytes a long number represented by an array of bytes.
     * @param endianness  1 for the little-endian sequence, 0 for big-endian.
     * @return a number of the int type that is the conversion result.
     */
    public static int bytesToInt(byte[] bytes, int endianness) {
        return byteArrayToByteBuffer(bytes, endianness).getInt();
    }

    /**
     * The function to convert an array of bytes into a long number.
     *
     * @param bytes a long number represented by an array of bytes.
     * @param endianness  1 for the little-endian sequence, 0 for big-endian.
     * @return a number of the long type that is the conversion result.
     */
    public static long bytesToLong(byte[] bytes, int endianness) {
        return byteArrayToByteBuffer(bytes, endianness).getLong();
    }

    /**
     * The function to convert an array of bytes into a string.
     *
     * @param bytes a string represented by an array of bytes.
     * @param charset the charset to be used to decode the bytes.
     * @return the conversion result string.
     */
    public static String bytesToString(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }

    /**
     * The function to convert an array of bytes into a string.
     *
     * @param bytes a string represented by an array of bytes.
     * @return the conversion result string.
     */
    public static String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    /**
     * The function to convert a sub-range of an array of bytes into a string.
     *
     * @param bytes  a string represented by an array of bytes.
     * @param offset the initial offset
     * @param length the length
     * @param encoding the character encoding
     * @return the conversion result string.
     * @throws UnsupportedEncodingException    when unknown encoding.
     * @throws StringIndexOutOfBoundsException when invalid offset and/or length.
     */
    public static String bytesToString(byte[] bytes, int offset, int length, String encoding)
        throws UnsupportedEncodingException, StringIndexOutOfBoundsException {
        return new String(bytes, offset, length, encoding);
    }

    /**
     * The function to convert an array of bytes into a string.
     * @param bytes  a string represented by an array of bytes.
     * @param offset the initial offset
     * @param length the length
     * @return the conversion result string.
     * @throws UnsupportedEncodingException    when unknown encoding.
     * @throws StringIndexOutOfBoundsException when invalid offset and/or length.
     */
    public static String bytesToString(byte[] bytes, int offset, int length)
        throws UnsupportedEncodingException, StringIndexOutOfBoundsException {
        return bytesToString(bytes, offset, length, StandardCharsets.US_ASCII.name());
    }

    /**
     * The function to convert an array of bytes with any order of bytes into {@link ByteBuffer}.
     * {@link ByteBuffer} has the order of bytes defined in the file located at the
     * {@link SasFileConstants#ALIGN_2_OFFSET} offset.
     * Later the parser converts result {@link ByteBuffer} into a number.
     *
     * @param data the input array of bytes with the little-endian or big-endian order.
     * @param endianness  1 for the little-endian sequence, 0 for big-endian.
     * @return {@link ByteBuffer} with the order of bytes defined in the file located at
     * the {@link SasFileConstants#ALIGN_2_OFFSET} offset.
     */
    public static ByteBuffer byteArrayToByteBuffer(byte[] data, int endianness) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        if (endianness == 0) {
            return byteBuffer;
        } else {
            return byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    /**
     * The function to convert an array of bytes in the IEEE 754 format into a double number.
     *
     * @param bytes a double number represented by an array of bytes.
     * @param endianness  1 for the little-endian sequence, 0 for big-endian.
     * @return a number of the double type that is the conversion result.
     */
    public static double ieeeBytesToDouble(byte[] bytes, int endianness) {
        ByteBuffer original = byteArrayToByteBuffer(bytes, endianness);

        if (bytes.length < BYTES_IN_DOUBLE) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(BYTES_IN_DOUBLE);
            if (endianness == 1) {
                byteBuffer.position(BYTES_IN_DOUBLE - bytes.length);
            }
            byteBuffer.put(original);
            byteBuffer.order(original.order());
            byteBuffer.position(0);
            original = byteBuffer;
        }

        return original.getDouble();
    }

    /**
     * The function to convert an array of bytes into a number. The result can be double or long values.
     * The numbers in XPORT are stored in the IBM format. A number is considered long if the difference between the
     * whole
     * number and its integer part is less than {@link SasFileConstants#EPSILON}.
     *
     * @param mass the number represented by an array of bytes.
     * @return number of a long or double type.
     */
    public static Object convertIbmByteArrayToNumber(byte[] mass) {
        double resultDouble = BytesHelper.ibmBytesToDouble(mass);
        return convertDoubleToNumber(resultDouble);
    }

    /**
     * The function to convert a double into a number. The result can be double or long values.
     * A number is considered long if the difference between the whole
     * number and its integer part is less than {@link SasFileConstants#EPSILON}.
     *
     * @param resultDouble value as a double.
     * @return number of a long or double type.
     */
    public static Object convertDoubleToNumber(double resultDouble) {
        if (Double.isNaN(resultDouble) || (resultDouble < SasFileConstants.NAN_EPSILON && resultDouble > 0)) {
            return null;
        }

        long resultLong = Math.round(resultDouble);
        if (Math.abs(resultDouble - resultLong) >= SasFileConstants.EPSILON) {
            return resultDouble;
        } else {
            return resultLong;
        }
    }

    /**
     * The function to convert an array of bytes in IBM format into a double number.
     *
     * IEEE format:
     * SEEEEEEEEEEEMMMM ............ MMMM
     * Sign bit, 11 bits exponent, 52 bit fraction. Exponent is
     * excess 1023. The fraction is multiplied by a power of 2 of
     * the actual exponent. Normalized floating point numbers are
     * represented with the binary point immediately to the left
     * of the fraction with an implied "1" to the left of the
     * binary point.
     *
     * IBM format:
     * SEEEEEEEMMMM ......... MMMM
     * Sign bit, 7 bit exponent, 56 bit fraction. Exponent is
     * excess 64. The fraction is multiplied bya power of 16 of
     * the actual exponent. Normalized floating point numbers are
     * represented with the radix point immediately to the left of
     * the high order hex fraction digit.
     *
     * @param bytes a double number represented by an array of bytes.
     * @return a number of the double type that is the conversion result.
     */
    public static double ibmBytesToDouble(byte[] bytes) {

        // parse the 64 bits of IBM float as one 8-byte unsigned long long
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long aLong = byteBuffer.getLong();

        // 1-bit sign, 7-bits exponent, 56-bits mantissa
        long sign = aLong & 0x8000000000000000L;
        long exponent = (aLong >> 56) & 0x7F;
        long mantissa = aLong & 0x00FFFFFFFFFFFFFFL;

        if (mantissa == 0) {
            if (bytes[0] == (byte) 0x00) {
                return 0.0;
            } else if (bytes[0] == (byte) 0x80) {
                return -0.0;
            } else if (XPORT_MISSING_VALUES.contains(bytes[0])) {
                return Double.NaN;
            } else {
                throw new IllegalArgumentException("Neither zero nor NaN");
            }
        }

        // The fraction bit to the left of the binary point in the ieee format was set and the number was shifted
        // 0, 1, 2, or 3 places. This will tell us how to adjust the ibm exponent
        // to be a power of 2 ieee exponent and how to shift the
        // fraction bits to restore the correct magnitude.
        // IBM-format exponent is base 16, so the mantissa can have up to 3
        // leading zero-bits in the binary mantissa. IEEE format exponent
        // is base 2, so we don't need any leading zero-bits and will shift
        // accordingly.
        int shift;
        if ((aLong & 0x0080000000000000L) != 0) {
            shift = 3;
        } else if ((aLong & 0x0040000000000000L) != 0) {
            shift = 2;
        } else if ((aLong & 0x0020000000000000L) != 0) {
            shift = 1;
        } else {
            shift = 0;
        }
        mantissa >>= shift;

        // clear the 1 bit to the left of the binary point. This is implicit in IEEE specification
        mantissa &= 0xffefffffffffffffL;

        // The ibm exponent is excess 64 but is adjusted by 65 since during conversion to ibm
        // format the exponent is incremented by 1 and the fraction
        // bits left 4 positions to the right of the radix point
        exponent -= 65;
        // IBM exponent is base 16, IEEE is base 2, so we multiply by 4
        exponent <<= 2;
        // IEEE exponent is excess 1023, but we also increment for each
        // right-shift when aligning the mantissa's first 1-bit
        exponent += shift + 1023;

        // IEEE: 1-bit sign, 11-bits exponent, 52-bits mantissa
        // We didn't shift the sign bit, so it's already in the right spot
        long ieee = sign | (exponent << 52) | mantissa;
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(ieee).flip();

        return buffer.getDouble();
    }

    /**
     * Skip specified number of bytes of data from the input stream,
     * or fail if there are not enough left.
     *
     * @param inputStream input stream
     * @param skipByteBuffer byte buffer used for skip operations. Actually the data containing in this buffer is
     *                       ignored, because it only used for dummy reads.
     * @param numberOfBytesToSkip the number of bytes to skip
     * @throws IOException if the number of bytes skipped was incorrect
     */
    public static void skipBytes(InputStream inputStream, byte[] skipByteBuffer, long numberOfBytesToSkip)
        throws IOException {

        long remainBytes = numberOfBytesToSkip;
        long readBytes;
        while (remainBytes > 0) {
            try {
                readBytes = inputStream.read(skipByteBuffer, 0,
                    (int) Math.min(remainBytes, skipByteBuffer.length));
                if (readBytes < 0) { // EOF
                    break;
                }
            } catch (IOException e) {
                throw new IOException(EMPTY_INPUT_STREAM);
            }
            remainBytes -= readBytes;
        }

        long actuallySkipped = numberOfBytesToSkip - remainBytes;

        if (actuallySkipped != numberOfBytesToSkip) {
            throw new IOException("Expected to skip " + numberOfBytesToSkip
                + " to the end of the header, but skipped " + actuallySkipped + " instead.");
        }
    }
}
