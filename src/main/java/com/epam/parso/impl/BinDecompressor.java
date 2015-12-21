/**
 * *************************************************************************
 * Copyright (C) 2015 EPAM
 * <p>
 * This file is part of Parso.
 * <p>
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * <p>
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * *************************************************************************
 */

package com.epam.parso.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Implementation of the BIN compression algorithm which corresponds to the literal "SASYZCR2".
 * Refer the documentation for further details.
 * It follows the general contract provided by the interface <code>Decompressor</code>.
 */
final class BinDecompressor implements Decompressor {
    static final BinDecompressor INSTANCE = new BinDecompressor();
    private static final Logger LOGGER = LoggerFactory.getLogger(BinDecompressor.class);

    private BinDecompressor() {
        // prevent multiple instances
    }

    /**
     * As described in the documentation, first 16 bits indicate which blocks are compressed.
     * Next, each block is preceded by a marker which may consist of one, two or three bytes.
     * This marker contains the information which compression is used (BIN or simple RLE) and
     * the block length.
     *
     * @param pageoffset   the offset of bytes array in <code>page</code> that contains compressed data.
     * @param srcLength    the length of bytes array that contains compressed data.
     * @param resultLength the length of bytes array that contains decompressed data.
     * @param page         an array of bytes with compressed data.
     * @return decompressed row
     */
    @Override
    public byte[] decompressRow(int pageoffset, int srcLength, int resultLength, byte[] page) {

        byte[] srcRow = Arrays.copyOfRange(page, pageoffset, srcLength + pageoffset);
        byte[] outRow = new byte[resultLength];
        int srcOffset = 0;
        int outOffset = 0;

        while (srcOffset < srcRow.length - 2) {

            //read the two bytes prefix and interpret it as a 16-bit string.
            byte[] prefixBits = bytesAsBits(srcRow, srcOffset);

            srcOffset += 2;
            for (int bitIndex = 0; (bitIndex < 16) && (srcOffset < srcRow.length); bitIndex++) {

                // if the byte for this chunk is set to 0, then just copy one
                // byte as is. This byte is not relevant for the compression
                if (prefixBits[bitIndex] == 0) {
                    outRow = ensureCapacity(outRow, outOffset);
                    outRow[outOffset] = srcRow[srcOffset];
                    srcOffset++;
                    outOffset++;
                    continue;
                }

                byte markerByte = srcRow[srcOffset];
                byte nextByte = srcRow[srcOffset + 1]; // the second byte may play different roles

                if (isShortRLE(markerByte)) {
                    int length = getLengthOfRLEPattern(markerByte);
                    outRow = ensureCapacity(outRow, outOffset + length);

                    byte[] pattern = cloneByte(nextByte, length);

                    System.arraycopy(pattern, 0, outRow, outOffset, length);
                    outOffset += length;
                    srcOffset += 2;
                    continue;
                }

                if (isSingleByteMarker(markerByte)
                        && !(((byte) (nextByte & (byte) 0xF0)) == (((byte) (nextByte << 4)) & (byte) 0xF0))) {

                    int length = getLengthOfOneBytePattern(markerByte);
                    outRow = ensureCapacity(outRow, outOffset + length);

                    int backOffset = getOffsetForOneBytePattern(markerByte);
                    System.arraycopy(outRow, outOffset - backOffset, outRow,
                            outOffset, length);

                    srcOffset++;
                    outOffset += length;
                    continue;
                }

                byte[] twoBytesMarker = Arrays.copyOfRange(srcRow, srcOffset,
                        srcOffset + 2);
                if (isTwoBytesMarker(twoBytesMarker)) {
                    int length = getLengthOfTwoBytesPattern(twoBytesMarker);

                    outRow = ensureCapacity(outRow, outOffset + length);

                    int backOffset = getOffsetForTwoBytesPattern(twoBytesMarker);
                    System.arraycopy(outRow, outOffset - backOffset, outRow,
                            outOffset, length);

                    srcOffset += 2;
                    outOffset += length;
                    continue;
                }

                byte[] threeBytesMarker = Arrays.copyOfRange(srcRow, srcOffset,
                        srcOffset + 3);

                if (isThreeBytesMarker(threeBytesMarker)) {
                    int type = (byte) ((threeBytesMarker[0] >> 4) & (byte) 0x0F);
                    int backOffset = 0;
                    if (type == 2) {
                        backOffset = getOffsetForThreeBytesPattern(threeBytesMarker);
                    }
                    int length = getLengthOfThreeBytesPattern(
                            type, threeBytesMarker);
                    outRow = ensureCapacity(outRow, outOffset + length);

                    byte[] pattern;
                    if (type == 1) { //RLE pattern
                        pattern = cloneByte(threeBytesMarker[2], length);
                    } else { //Base-offset pattern
                        pattern = Arrays.copyOfRange(outRow, outOffset - backOffset,
                                outOffset - backOffset + length);
                    }

                    System.arraycopy(pattern, 0, outRow, outOffset, length);
                    srcOffset += 3;
                    outOffset += length;
                } else {
                    LOGGER.error("Unknown marker {} at offset {}", srcRow[srcOffset], srcOffset);
                    return srcRow;
                }
            }
        }
        return outRow;
    }

    private boolean isShortRLE(byte firstByteofCB) {
        return firstByteofCB >= 0x00 && firstByteofCB <= 0x05;
    }

    private int getLengthOfRLEPattern(byte firstByteofCB) {
        if (firstByteofCB <= 0x05) {
            return firstByteofCB + 3;
        }
        return 0;
    }

    private boolean isSingleByteMarker(byte firstByteofCB) {
        List<Byte> trueValues = Arrays.asList(new Byte[]{0x02, 0x04, 0x06, 0x08, 0x0A});

        return trueValues.contains(firstByteofCB);
    }

    private int getLengthOfOneBytePattern(byte firstByteofCB) {
        return (isSingleByteMarker(firstByteofCB)) ? firstByteofCB + 14 : 0;
    }

    private int getOffsetForOneBytePattern(byte firstByteofCB) {
        if (firstByteofCB == 0x08) {
            return 24;
        }

        if (firstByteofCB == 0x0A) {
            return 40;
        }

        return 0;
    }

    private boolean isTwoBytesMarker(byte[] doubleBytesCB) {
        return (byte) ((doubleBytesCB[0] >> 4) & 0xF) > 2;
    }

    private int getLengthOfTwoBytesPattern(byte[] doubleBytesCB) {
        return (byte) ((doubleBytesCB[0] >> 4) & 0xF);
    }

    private int getOffsetForTwoBytesPattern(byte[] doubleBytesCB) {
        return 3 + (byte) (doubleBytesCB[0] & 0xF)
                + (doubleBytesCB[1] * 16);
    }

    private boolean isThreeBytesMarker(byte[] threeByteMarker) {
        byte flag = (byte) (threeByteMarker[0] >> 4);
        return ((flag & 0xF) == 2) || ((flag & 0xF) == 1);
    }

    private int getLengthOfThreeBytesPattern(int type, byte[] threeByteMarker) {
        if (type == 1) {
            return 19 + (byte) (threeByteMarker[0] & 0xF) + (threeByteMarker[1] * 16);
        } else if (type == 2) {
            return (int) threeByteMarker[2] + 16;
        }
        return 0;
    }

    private int getOffsetForThreeBytesPattern(byte[] tripleBytesCB) {
        return 3 + tripleBytesCB[0] & 0xF + (tripleBytesCB[1] * 16);
    }

    private byte[] ensureCapacity(byte[] src, int capacity) {
        if (capacity >= src.length) {
            return Arrays.copyOf(src, Math.max(capacity, 2 * src.length));
        }
        return src;
    }

    private byte[] bytesAsBits(byte[] src, int offset) {
        byte[] result = new byte[16];

        for (int i = 0; i < 2; i++) {
            byte b = src[offset + i];
            for (int bit = 0; bit <= 7; bit++) {
                //we read the bits from right to left,
                // so the index in the result array is (7-bit) + offset
                result[8 * i + (7 - bit)] = (byte) (((b & (1 << bit)) == 0) ? 0 : 1);
            }
        }
        return result;
    }

    private byte[] cloneByte(byte b, int length) {
        byte[] result = new byte[length];
        Arrays.fill(result, b);
        return result;
    }
}
