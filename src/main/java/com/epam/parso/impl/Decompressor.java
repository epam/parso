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

/**
 * Interface which should be implemented in all data decompressors.
 */
interface Decompressor {

    /**
     * The function to decompress data. Compressed data are an array of bytes with control bytes and data bytes.
     * The project documentation contains descriptions of the decompression algorithm.
     *
     * @param offset       the offset of bytes array in <code>page</code> that contains compressed data.
     * @param srcLength    the length of bytes array that contains compressed data.
     * @param resultLength the length of bytes array that contains decompressed data.
     * @param page         an array of bytes with compressed data.
     * @return an array of bytes with decompressed data.
     */
    byte[] decompressRow(int offset, int srcLength, int resultLength, byte[] page);
}
