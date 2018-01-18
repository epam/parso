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

/**
 * Implementation of the BIN compression algorithm which corresponds to the literal "SASYZCR2".
 * Refer the documentation for further details.
 * It follows the general contract provided by the interface <code>Decompressor</code>.
 *
 * History
 *   01.08.2015 (Gabor Bakos): Replaced the implementation to an alternative
 */
final class BinDecompressor implements Decompressor {
    /**
     * Unambiguous class instance.
     */
    static final BinDecompressor INSTANCE = new BinDecompressor();

    /**
     * Empty private constructor for preventing multiple instances.
     */
    private BinDecompressor() {
    }

    /**
     * As described in the documentation, first 16 bits indicate which blocks are compressed.
     * Next, each block is preceded by a marker which may consist of one, two or three bytes.
     * This marker contains the information which compression is used (BIN or simple RLE) and
     * the block length.
     * <p>
     * Based on http://www.drdobbs.com/a-simple-data-compression-technique/184402606?pgno=2
     *
     * @param pageoffset   the offset of bytes array in <code>page</code> that contains compressed data.
     * @param srcLength    the length of bytes array that contains compressed data.
     * @param resultLength the length of bytes array that contains decompressed data.
     * @param page         an array of bytes with compressed data.
     * @return decompressed row
     */
    @Override
    public byte[] decompressRow(final int pageoffset, final int srcLength, final int resultLength, final byte[] page) {

        byte[] srcRow = Arrays.copyOfRange(page, pageoffset, srcLength + pageoffset);
        byte[] outRow = new byte[resultLength];
        int srcOffset = 0;
        int outOffset = 0;
        int ctrlBits = 0, ctrlMask = 0;
        while (srcOffset < srcLength) {

            ctrlMask >>= 1;
            if (ctrlMask == 0) {
                ctrlBits = (((srcRow[srcOffset]) & 0xff) << 8) | (srcRow[srcOffset + 1] & 0xff);
                srcOffset += 2;
                ctrlMask = 0x8000;
            }

            // just copy this char if control bit is zero
            if ((ctrlBits & ctrlMask) == 0) {
                outRow[outOffset++] = srcRow[srcOffset++];
                continue;
            }

            // undo the compression code
            final int cmd = (srcRow[srcOffset] >> 4) & 0x0F;
            int cnt = srcRow[srcOffset++] & 0x0F;

            switch (cmd) {
                case 0: // short rle
                    cnt += 3;
                    for (int i = 0; i < cnt; i++) {
                        outRow[outOffset + i] = srcRow[srcOffset];
                    }
                    srcOffset++;
                    outOffset += cnt;
                    break;

                case 1: // long rle
                    cnt += ((srcRow[srcOffset++] & 0xff) << 4);
                    cnt += 19;
                    for (int i = 0; i < cnt; i++) {
                        outRow[outOffset + i] = srcRow[srcOffset];
                    }
                    srcOffset++;
                    outOffset += cnt;
                    break;

                case 2: // long pattern
                    int ofs = cnt + 3;
                    ofs += ((srcRow[srcOffset++] & 0xff) << 4);
                    cnt = srcRow[srcOffset++] & 0xff;
                    cnt += 16;
                    System.arraycopy(outRow, outOffset - ofs, outRow, outOffset, cnt);
                    outOffset += cnt;
                    break;

                default: // short pattern
                    ofs = cnt + 3;
                    ofs += ((srcRow[srcOffset++] & 0xff) << 4);
                    System.arraycopy(outRow, outOffset - ofs, outRow, outOffset, cmd);
                    outOffset += cmd;
                    break;
            }
        }
        return outRow;
    }
}
