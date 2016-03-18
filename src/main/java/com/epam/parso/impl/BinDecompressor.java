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
    static final BinDecompressor INSTANCE = new BinDecompressor();

    private BinDecompressor() {
        // prevent multiple instances
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
            if ((ctrlMask >>= 1) == 0) {
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
                    for (int i = cnt; i-- > 0; ) {
                        outRow[outOffset + i] = srcRow[srcOffset];
                    }
                    srcOffset++;
                    outOffset += cnt;
                    break;

                case 1: // long rle
                    cnt += ((srcRow[srcOffset++] & 0xff) << 4);
                    cnt += 19;
                    for (int i = cnt; i-- > 0; ) {
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
                    for (int i = cnt; i-- > 0; ) {
                        outRow[outOffset + i] = outRow[outOffset - ofs + i];
                    }
                    outOffset += cnt;
                    break;

                default: // short pattern
                    ofs = cnt + 3;
                    ofs += ((srcRow[srcOffset++] & 0xff) << 4);
                    for (int i = cmd; i-- > 0; ) {
                        outRow[outOffset + i] = outRow[outOffset - ofs + i];
                    }
                    outOffset += cmd;
                    break;
            }
        }
        return outRow;
    }
}
