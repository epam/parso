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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the CHAR compression algorithm which corresponds to the literal "SASYZCRL".
 * Refer the documentation for further details.
 * It follows the general contract provided by the interface <code>Decompressor</code>.
 *
 */
final class CharDecompressor implements Decompressor {
    /**
     * Unambiguous class instance.
     */
    static final CharDecompressor INSTANCE = new CharDecompressor();
    /**
     * Object for writing logs.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CharDecompressor.class);

    /**
     * Empty private constructor for preventing multiple instances.
     */
    private CharDecompressor() {
    }

    /**
     * The function to decompress data. Compressed data are an array of bytes with control bytes and data bytes.
     * The project documentation contains descriptions of the decompression algorithm.
     *
     * @param offset       the offset of bytes array in {@link SasFileParser#cachedPage} that contains compressed data.
     * @param length       the length of bytes array that contains compressed data.
     * @param resultLength the length of bytes array that contains decompressed data.
     * @param page         an array of bytes with compressed data.
     * @return an array of bytes with decompressed data.
     */
    @Override
    public byte[] decompressRow(int offset, int length, int resultLength, byte[] page) {
        byte[] resultByteArray = new byte[resultLength];
        int currentResultArrayIndex = 0;
        int currentByteIndex = 0;
        while (currentByteIndex < length) {
            int controlByte = page[offset + currentByteIndex] & 0xF0;
            int endOfFirstByte = page[offset + currentByteIndex] & 0x0F;
            int countOfBytesToCopy;
            switch (controlByte) {
                case 0x30://intentional fall through
                case 0x20://intentional fall through
                case 0x10://intentional fall through
                case 0x00:
                    if (currentByteIndex != length - 1) {
                        countOfBytesToCopy = (page[offset + currentByteIndex + 1] & 0xFF) + 64
                           + page[offset + currentByteIndex] * 256;
                        System.arraycopy(page, offset + currentByteIndex + 2, resultByteArray,
                                currentResultArrayIndex, countOfBytesToCopy);
                        currentByteIndex += countOfBytesToCopy + 1;
                        currentResultArrayIndex += countOfBytesToCopy;
                    }
                    break;
                case 0x40:
                    int copyCounter = endOfFirstByte * 16 + (page[offset + currentByteIndex + 1] & 0xFF);
                    for (int i = 0; i < copyCounter + 18; i++) {
                        resultByteArray[currentResultArrayIndex++] = page[offset + currentByteIndex + 2];
                    }
                    currentByteIndex += 2;
                    break;
                case 0x50:
                    for (int i = 0; i < endOfFirstByte * 256 + (page[offset + currentByteIndex + 1] & 0xFF) + 17; i++) {
                        resultByteArray[currentResultArrayIndex++] = 0x40;
                    }
                    currentByteIndex++;
                    break;
                case 0x60:
                    for (int i = 0; i < endOfFirstByte * 256 + (page[offset + currentByteIndex + 1] & 0xFF) + 17; i++) {
                        resultByteArray[currentResultArrayIndex++] = 0x20;
                    }
                    currentByteIndex++;
                    break;
                case 0x70:
                    for (int i = 0; i < endOfFirstByte * 256 + (page[offset + currentByteIndex + 1] & 0xFF) + 17; i++) {
                        resultByteArray[currentResultArrayIndex++] = 0x00;
                    }
                    currentByteIndex++;
                    break;
                case 0x80:
                case 0x90:
                case 0xA0:
                case 0xB0:
                    countOfBytesToCopy = Math.min(endOfFirstByte + 1 + (controlByte - 0x80),
                            length - (currentByteIndex + 1));
                    System.arraycopy(page, offset + currentByteIndex + 1, resultByteArray,
                            currentResultArrayIndex, countOfBytesToCopy);
                    currentByteIndex += countOfBytesToCopy;
                    currentResultArrayIndex += countOfBytesToCopy;
                    break;
                case 0xC0:
                    for (int i = 0; i < endOfFirstByte + 3; i++) {
                        resultByteArray[currentResultArrayIndex++] = page[offset + currentByteIndex + 1];
                    }
                    currentByteIndex++;
                    break;
                case 0xD0:
                    for (int i = 0; i < endOfFirstByte + 2; i++) {
                        resultByteArray[currentResultArrayIndex++] = 0x40;
                    }
                    break;
                case 0xE0:
                    for (int i = 0; i < endOfFirstByte + 2; i++) {
                        resultByteArray[currentResultArrayIndex++] = 0x20;
                    }
                    break;
                case 0xF0:
                    for (int i = 0; i < endOfFirstByte + 2; i++) {
                        resultByteArray[currentResultArrayIndex++] = 0x00;
                    }
                    break;
                default:
                    LOGGER.error("Error control byte: {}", controlByte);
                    break;
            }
            currentByteIndex++;
        }

        return resultByteArray;
    }

}
