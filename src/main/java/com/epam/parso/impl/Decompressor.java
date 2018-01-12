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
