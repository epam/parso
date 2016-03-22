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

import com.epam.parso.Column;
import com.epam.parso.SasFileProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Date;

/**
 * This is a class that parses sas7bdat files. When parsing a sas7bdat file, to interact with the library,
 * do not use this class but use {@link SasFileReaderImpl} instead.
 */
public final class SasFileParser {
    /**
     * Object for writing logs.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SasFileParser.class);
    /**
     * The mapping of subheader signatures to the corresponding elements in {@link SubheaderIndexes}.
     * Depending on the value at the {@link SasFileConstants#ALIGN_2_OFFSET} offset, signatures take 4 bytes
     * for 32-bit version sas7bdat files and 8 bytes for the 64-bit version files.
     */
    private static final Map<Long, SubheaderIndexes> SUBHEADER_SIGNATURE_TO_INDEX;
    /**
     * The mapping of the supported string literals to the compression method they mean.
     */
    private static final Map<String, Decompressor> LITERALS_TO_DECOMPRESSOR = new HashMap<String, Decompressor>();

    static {
        Map<Long, SubheaderIndexes> tmpMap = new HashMap<Long, SubheaderIndexes>();
        tmpMap.put((long) 0xF7F7F7F7, SubheaderIndexes.ROW_SIZE_SUBHEADER_INDEX);
        tmpMap.put((long) 0xF6F6F6F6, SubheaderIndexes.COLUMN_SIZE_SUBHEADER_INDEX);
        tmpMap.put((long) 0xFFFFFC00, SubheaderIndexes.SUBHEADER_COUNTS_SUBHEADER_INDEX);
        tmpMap.put((long) 0xFFFFFFFD, SubheaderIndexes.COLUMN_TEXT_SUBHEADER_INDEX);
        tmpMap.put((long) 0xFFFFFFFF, SubheaderIndexes.COLUMN_NAME_SUBHEADER_INDEX);
        tmpMap.put((long) 0xFFFFFFFC, SubheaderIndexes.COLUMN_ATTRIBUTES_SUBHEADER_INDEX);
        tmpMap.put((long) 0xFFFFFBFE, SubheaderIndexes.FORMAT_AND_LABEL_SUBHEADER_INDEX);
        tmpMap.put((long) 0xFFFFFFFE, SubheaderIndexes.COLUMN_LIST_SUBHEADER_INDEX);
        tmpMap.put(0x00000000F7F7F7F7L, SubheaderIndexes.ROW_SIZE_SUBHEADER_INDEX);
        tmpMap.put(0x00000000F6F6F6F6L, SubheaderIndexes.COLUMN_SIZE_SUBHEADER_INDEX);
        tmpMap.put(0xF7F7F7F700000000L, SubheaderIndexes.ROW_SIZE_SUBHEADER_INDEX);
        tmpMap.put(0xF6F6F6F600000000L, SubheaderIndexes.COLUMN_SIZE_SUBHEADER_INDEX);
        tmpMap.put(0x00FCFFFFFFFFFFFFL, SubheaderIndexes.SUBHEADER_COUNTS_SUBHEADER_INDEX);
        tmpMap.put(0xFDFFFFFFFFFFFFFFL, SubheaderIndexes.COLUMN_TEXT_SUBHEADER_INDEX);
        tmpMap.put(0xFFFFFFFFFFFFFFFFL, SubheaderIndexes.COLUMN_NAME_SUBHEADER_INDEX);
        tmpMap.put(0xFCFFFFFFFFFFFFFFL, SubheaderIndexes.COLUMN_ATTRIBUTES_SUBHEADER_INDEX);
        tmpMap.put(0xFEFBFFFFFFFFFFFFL, SubheaderIndexes.FORMAT_AND_LABEL_SUBHEADER_INDEX);
        tmpMap.put(0xFEFFFFFFFFFFFFFFL, SubheaderIndexes.COLUMN_LIST_SUBHEADER_INDEX);
        SUBHEADER_SIGNATURE_TO_INDEX = Collections.unmodifiableMap(tmpMap);
    }

    static {
        LITERALS_TO_DECOMPRESSOR.put(SasFileConstants.COMPRESS_CHAR_IDENTIFYING_STRING, CharDecompressor.INSTANCE);
        LITERALS_TO_DECOMPRESSOR.put(SasFileConstants.COMPRESS_BIN_IDENTIFYING_STRING, BinDecompressor.INSTANCE);
    }

    /**
     * The input stream through which the sas7bdat is read.
     */
    private final DataInputStream sasFileStream;
    /**
     * The flag of data output in binary or string format.
     */
    private final Boolean byteOutput;
    /**
     * The list of current page data subheaders.
     */
    private final List<SubheaderPointer> currentPageDataSubheaderPointers = new ArrayList<SubheaderPointer>();
    /**
     * The variable to store all the properties from the sas7bdat file.
     */
    private final SasFileProperties sasFileProperties = new SasFileProperties();
    /**
     * The list of text blocks with information about file compression and table columns (name, label, format).
     * Every element corresponds to a {@link SasFileParser.ColumnTextSubheader}. The first text block includes
     * the information about compression.
     */
    private final List<String> columnsNamesStrings = new ArrayList<String>();
    /**
     * The list of column names.
     */
    private final List<String> columnsNamesList = new ArrayList<String>();
    /**
     * The list of column types. There can be {@link Number} and {@link String} types.
     */
    private final List<Class<?>> columnsTypesList = new ArrayList<Class<?>>();
    /**
     * The list of offsets of data in every column inside a row. Used to locate the left border of a cell.
     */
    private final List<Long> columnsDataOffset = new ArrayList<Long>();
    /**
     * The list of data lengths of every column inside a row. Used to locate the right border of a cell.
     */
    private final List<Integer> columnsDataLength = new ArrayList<Integer>();
    /**
     * The list of table columns to store their name, label, and format.
     */
    private final List<Column> columns = new ArrayList<Column>();
    /**
     * The mapping between elements from {@link SubheaderIndexes} and classes corresponding
     * to each subheader. This is necessary because defining the subheader type being processed is dynamic.
     * Every class has an overridden function that processes the related subheader type.
     */
    private final Map<SubheaderIndexes, ProcessingSubheader> subheaderIndexToClass;
    /**
     * Default encoding for output strings.
     */
    private String encoding = "ASCII";
    /**
     * A cache to store the current page of the sas7bdat file. Used to avoid posing buffering requirements
     * to {@link SasFileParser#sasFileStream}.
     */
    private byte[] cachedPage;
    /**
     * The type of the current page when reading the file. If it is other than {@link SasFileConstants#PAGE_META_TYPE},
     * {@link SasFileConstants#PAGE_MIX_TYPE} and {@link SasFileConstants#PAGE_DATA_TYPE} page is skipped.
     */
    private int currentPageType;
    /**
     * Number current page blocks.
     */
    private int currentPageBlockCount;
    /**
     * Number current page subheaders.
     */
    private int currentPageSubheadersCount;
    /**
     * The index of the current byte when reading the file.
     */
    private int currentFilePosition;
    /**
     * The index of the current column when reading the file.
     */
    private int currentColumnNumber;
    /**
     * The index of the current row when reading the file.
     */
    private int currentRowInFileIndex;
    /**
     * The index of the current row when reading the page.
     */
    private int currentRowOnPageIndex;
    /**
     * Last read row from sas7bdat file.
     */
    private Object[] currentRow;
    /**
     * True if stream is at the end of file.
     */
    private boolean eof;
    /**
     * The constructor that reads metadata from the sas7bdat, parses it and puts the results in
     * {@link SasFileParser#sasFileProperties}.
     *
     * @param builder the container with properties information.
     */
    private SasFileParser(Builder builder) {
        sasFileStream = new DataInputStream(builder.sasFileStream);
        encoding = builder.encoding;
        byteOutput = builder.byteOutput;

        Map<SubheaderIndexes, ProcessingSubheader> tmpMap = new HashMap<SubheaderIndexes, ProcessingSubheader>();
        tmpMap.put(SubheaderIndexes.ROW_SIZE_SUBHEADER_INDEX, new RowSizeSubheader());
        tmpMap.put(SubheaderIndexes.COLUMN_SIZE_SUBHEADER_INDEX, new ColumnSizeSubheader());
        tmpMap.put(SubheaderIndexes.SUBHEADER_COUNTS_SUBHEADER_INDEX, new SubheaderCountsSubheader());
        tmpMap.put(SubheaderIndexes.COLUMN_TEXT_SUBHEADER_INDEX, new ColumnTextSubheader());
        tmpMap.put(SubheaderIndexes.COLUMN_NAME_SUBHEADER_INDEX, new ColumnNameSubheader());
        tmpMap.put(SubheaderIndexes.COLUMN_ATTRIBUTES_SUBHEADER_INDEX, new ColumnAttributesSubheader());
        tmpMap.put(SubheaderIndexes.FORMAT_AND_LABEL_SUBHEADER_INDEX, new FormatAndLabelSubheader());
        tmpMap.put(SubheaderIndexes.COLUMN_LIST_SUBHEADER_INDEX, new ColumnListSubheader());
        tmpMap.put(SubheaderIndexes.DATA_SUBHEADER_INDEX, new DataSubheader());
        subheaderIndexToClass = Collections.unmodifiableMap(tmpMap);

        try {
            getMetadataFromSasFile();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * The method that reads and parses metadata from the sas7bdat and puts the results in
     * {@link SasFileParser#sasFileProperties}.
     *
     * @throws IOException - appears if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     */
    private void getMetadataFromSasFile() throws IOException {
        boolean endOfMetadata = false;
        processSasFileHeader();
        cachedPage = new byte[sasFileProperties.getPageLength()];
        while (!endOfMetadata) {
            try {
                sasFileStream.readFully(cachedPage, 0, sasFileProperties.getPageLength());
            } catch (EOFException ex) {
                eof = true;
                break;
            }
            endOfMetadata = processSasFilePageMeta();
        }
    }

    /**
     * The method to read and parse metadata from the sas7bdat file`s header in {@link SasFileParser#sasFileProperties}.
     * After reading is complete, {@link SasFileParser#currentFilePosition} is set to the end of the header whose length
     * is stored at the {@link SasFileConstants#HEADER_SIZE_OFFSET} offset.
     *
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     */
    private void processSasFileHeader() throws IOException {
        int align1 = 0;
        int align2 = 0;

        Long[] offsetForAlign = {SasFileConstants.ALIGN_1_OFFSET, SasFileConstants.ALIGN_2_OFFSET};
        Integer[] lengthForAlign = {SasFileConstants.ALIGN_1_LENGTH, SasFileConstants.ALIGN_2_LENGTH};
        List<byte[]> varsForAlign = getBytesFromFile(offsetForAlign, lengthForAlign);

        if (varsForAlign.get(0)[0] == SasFileConstants.U64_BYTE_CHECKER_VALUE) {
            align2 = SasFileConstants.ALIGN_2_VALUE;
            sasFileProperties.setU64(true);
        }

        if (varsForAlign.get(1)[0] == SasFileConstants.ALIGN_1_CHECKER_VALUE) {
            align1 = SasFileConstants.ALIGN_1_VALUE;
        }

        int totalAlign = align1 + align2;

        Long[] offset = {SasFileConstants.ENDIANNESS_OFFSET, SasFileConstants.DATASET_OFFSET, SasFileConstants
                .FILE_TYPE_OFFSET, SasFileConstants.DATE_CREATED_OFFSET + align1,
                SasFileConstants.DATE_MODIFIED_OFFSET + align1, SasFileConstants.HEADER_SIZE_OFFSET + align1,
                SasFileConstants.PAGE_SIZE_OFFSET + align1,
                SasFileConstants.PAGE_COUNT_OFFSET + align1, SasFileConstants.SAS_RELEASE_OFFSET + totalAlign,
                SasFileConstants.SAS_SERVER_TYPE_OFFSET + totalAlign,
                SasFileConstants.OS_VERSION_NUMBER_OFFSET + totalAlign, SasFileConstants.OS_MAKER_OFFSET
                + totalAlign, SasFileConstants.OS_NAME_OFFSET + totalAlign};
        Integer[] length = {SasFileConstants.ENDIANNESS_LENGTH, SasFileConstants.DATASET_LENGTH, SasFileConstants
                .FILE_TYPE_LENGTH, SasFileConstants.DATE_CREATED_LENGTH,
                SasFileConstants.DATE_MODIFIED_LENGTH, SasFileConstants.HEADER_SIZE_LENGTH, SasFileConstants
                .PAGE_SIZE_LENGTH, SasFileConstants.PAGE_COUNT_LENGTH + align2,
                SasFileConstants.SAS_RELEASE_LENGTH, SasFileConstants.SAS_SERVER_TYPE_LENGTH, SasFileConstants
                .OS_VERSION_NUMBER_LENGTH, SasFileConstants.OS_MAKER_LENGTH, SasFileConstants.OS_NAME_LENGTH};
        List<byte[]> vars = getBytesFromFile(offset, length);

        sasFileProperties.setEndianness(vars.get(0)[0]);
        sasFileProperties.setName(bytesToString(vars.get(1)).trim());
        sasFileProperties.setFileType(bytesToString(vars.get(2)).trim());
        sasFileProperties.setDateCreated(bytesToDateTime(vars.get(3)));
        sasFileProperties.setDateModified(bytesToDateTime(vars.get(4)));
        sasFileProperties.setHeaderLength(bytesToInt(vars.get(5)));
        sasFileProperties.setPageLength(bytesToInt(vars.get(6)));
        sasFileProperties.setPageCount(bytesToLong(vars.get(7)));
        sasFileProperties.setSasRelease(bytesToString(vars.get(8)).trim());
        sasFileProperties.setServerType(bytesToString(vars.get(9)).trim());
        sasFileProperties.setOsType(bytesToString(vars.get(10)).trim());
        if (vars.get(12)[0] != 0) {
            sasFileProperties.setOsName(bytesToString(vars.get(12)).trim());
        } else {
            sasFileProperties.setOsName(bytesToString(vars.get(11)).trim());
        }

        if (sasFileStream != null) {
            int bytesLeft = sasFileProperties.getHeaderLength() - currentFilePosition;

            long actuallySkipped = 0;
            while (actuallySkipped < bytesLeft) {
                actuallySkipped += sasFileStream.skip(bytesLeft - actuallySkipped);
            }
            currentFilePosition = 0;
        }
    }

    /**
     * The method to read pages of the sas7bdat file. First, the method reads the page type
     * (at the {@link SasFileConstants#PAGE_TYPE_OFFSET} offset), the number of rows on the page
     * (at the {@link SasFileConstants#BLOCK_COUNT_OFFSET} offset), and the number of subheaders
     * (at the {@link SasFileConstants#SUBHEADER_COUNT_OFFSET} offset). Then, depending on the page type,
     * the method calls the function to process the page.
     *
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     * @return true if all metadata is read.
     */
    private boolean processSasFilePageMeta() throws IOException {
        int bitOffset = sasFileProperties.isU64() ? SasFileConstants.PAGE_BIT_OFFSET_X64 : SasFileConstants
                .PAGE_BIT_OFFSET_X86;
        readPageHeader();
        List<SubheaderPointer> subheaderPointers = new ArrayList<SubheaderPointer>();
        if (currentPageType == SasFileConstants.PAGE_META_TYPE || currentPageType == SasFileConstants.PAGE_MIX_TYPE) {
            processPageMetadata(bitOffset, subheaderPointers);
        }
        return currentPageType == SasFileConstants.PAGE_DATA_TYPE || currentPageType == SasFileConstants
                .PAGE_MIX_TYPE || currentPageDataSubheaderPointers.size() != 0;
    }

    /**
     * The method to parse and read metadata of a page, used for pages of the {@link SasFileConstants#PAGE_META_TYPE}
     * and {@link SasFileConstants#PAGE_MIX_TYPE} types. The method goes through subheaders, one by one, and calls
     * the processing functions depending on their signatures.
     *
     * @param bitOffset         the offset from the beginning of the page at which the page stores its metadata.
     * @param subheaderPointers the number of subheaders on the page.
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} string is impossible.
     */
    private void processPageMetadata(int bitOffset, List<SubheaderPointer> subheaderPointers)
            throws IOException {
        subheaderPointers.clear();
        for (int subheaderPointerIndex = 0; subheaderPointerIndex < currentPageSubheadersCount;
             subheaderPointerIndex++) {
            SubheaderPointer currentSubheaderPointer = processSubheaderPointers((long) bitOffset
                    + SasFileConstants.SUBHEADER_POINTERS_OFFSET, subheaderPointerIndex);
            subheaderPointers.add(currentSubheaderPointer);
            if (currentSubheaderPointer.compression != SasFileConstants.TRUNCATED_SUBHEADER_ID) {
                long subheaderSignature = readSubheaderSignature(currentSubheaderPointer.offset);
                SubheaderIndexes subheaderIndex = chooseSubheaderClass(subheaderSignature,
                        currentSubheaderPointer.compression, currentSubheaderPointer.type);
                if (subheaderIndex != null) {
                    if (subheaderIndex != SubheaderIndexes.DATA_SUBHEADER_INDEX) {
                        LOGGER.debug("Subheader process function name: {}", subheaderIndex);
                        subheaderIndexToClass.get(subheaderIndex).processSubheader(
                                subheaderPointers.get(subheaderPointerIndex).offset,
                                subheaderPointers.get(subheaderPointerIndex).length);
                    } else {
                        currentPageDataSubheaderPointers.add(subheaderPointers.get(subheaderPointerIndex));
                    }
                } else {
                    LOGGER.debug("Unknown subheader signature");
                }
            }
        }
    }

    /**
     * The function to read a subheader signature at the offset known from its ({@link SubheaderPointer}).
     *
     * @param subheaderPointerOffset the offset at which the subheader is located.
     * @return - the subheader signature to search for in the {@link SasFileParser#SUBHEADER_SIGNATURE_TO_INDEX}
     * mapping later.
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     */
    private long readSubheaderSignature(Long subheaderPointerOffset) throws IOException {
        int intOrLongLength = sasFileProperties.isU64() ? SasFileConstants.BYTES_IN_LONG : SasFileConstants
                .BYTES_IN_INT;
        Long[] subheaderOffsetMass = {subheaderPointerOffset};
        Integer[] subheaderLengthMass = {intOrLongLength};
        List<byte[]> subheaderSignatureMass = getBytesFromFile(subheaderOffsetMass,
                subheaderLengthMass);
        return bytesToLong(subheaderSignatureMass.get(0));
    }

    /**
     * The function to determine the subheader type by its signature, {@link SubheaderPointer#compression},
     * and {@link SubheaderPointer#type}.
     *
     * @param subheaderSignature the subheader signature to search for in the
     *                           {@link SasFileParser#SUBHEADER_SIGNATURE_TO_INDEX} mapping
     * @param compression        the type of subheader compression ({@link SubheaderPointer#compression})
     * @param type               the subheader type ({@link SubheaderPointer#type})
     * @return an element from the  {@link SubheaderIndexes} enumeration that defines the type of
     * the current subheader
     */
    private SubheaderIndexes chooseSubheaderClass(long subheaderSignature, int compression, int type) {
        SubheaderIndexes subheaderIndex = SUBHEADER_SIGNATURE_TO_INDEX.get(subheaderSignature);
        if (sasFileProperties.isCompressed() && subheaderIndex == null && (compression == SasFileConstants
                .COMPRESSED_SUBHEADER_ID || compression == 0) && type == SasFileConstants.COMPRESSED_SUBHEADER_TYPE) {
            subheaderIndex = SubheaderIndexes.DATA_SUBHEADER_INDEX;
        }
        return subheaderIndex;
    }

    /**
     * The function to read the pointer with the subheaderPointerIndex index from the list of {@link SubheaderPointer}
     * located at the subheaderPointerOffset offset.
     *
     * @param subheaderPointerOffset the offset before the list of {@link SubheaderPointer}.
     * @param subheaderPointerIndex  the index of the subheader pointer being read.
     * @return the subheader pointer.
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     */
    private SubheaderPointer processSubheaderPointers(long subheaderPointerOffset, int subheaderPointerIndex)
            throws IOException {
        int intOrLongLength = sasFileProperties.isU64() ? SasFileConstants.BYTES_IN_LONG : SasFileConstants
                .BYTES_IN_INT;
        int subheaderPointerLength = sasFileProperties.isU64() ? SasFileConstants.SUBHEADER_POINTER_LENGTH_X64
                : SasFileConstants.SUBHEADER_POINTER_LENGTH_X86;
        long totalOffset = subheaderPointerOffset + subheaderPointerLength * ((long) subheaderPointerIndex);
        Long[] offset = {totalOffset, totalOffset + intOrLongLength, totalOffset + 2L * intOrLongLength,
                totalOffset + 2L * intOrLongLength + 1};
        Integer[] length = {intOrLongLength, intOrLongLength, 1, 1};
        List<byte[]> vars = getBytesFromFile(offset, length);

        long subheaderOffset = bytesToLong(vars.get(0));
        long subheaderLength = bytesToLong(vars.get(1));
        byte subheaderCompression = vars.get(2)[0];
        byte subheaderType = vars.get(3)[0];

        return new SubheaderPointer(subheaderOffset, subheaderLength, subheaderCompression, subheaderType);
    }

    /**
     * Return the compression literal if it is contained in the input string.
     * If the are many the first match is return.
     * If there are no matches, <code>null</code> is returned
     *
     * @param src input string to look for matches
     * @return First match of a supported compression literal or null if no literal matches the input string.
     */
    private String findCompressionLiteral(String src) {
        if (src == null) {
            LOGGER.warn("Null provided as the file compression literal, assuming no compression");
            return null;
        }

        for (String supported : LITERALS_TO_DECOMPRESSOR.keySet()) {
            if (src.contains(supported)) {
                return supported;
            }
        }
        LOGGER.debug("No supported compression literal found, assuming no compression");
        return null;
    }

    /**
     * The function to read next row from current sas7bdat file.
     *
     * @return the object array containing elements of current row.
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     */
    Object[] readNext() throws IOException {
        if (currentRowInFileIndex++ >= sasFileProperties.getRowCount() || eof) {
            return null;
        }
        int bitOffset = sasFileProperties.isU64() ? SasFileConstants.PAGE_BIT_OFFSET_X64
                : SasFileConstants.PAGE_BIT_OFFSET_X86;
        switch (currentPageType) {
            case SasFileConstants.PAGE_META_TYPE:
                SubheaderPointer currentSubheaderPointer =
                        currentPageDataSubheaderPointers.get(currentRowOnPageIndex++);
                subheaderIndexToClass.get(SubheaderIndexes.DATA_SUBHEADER_INDEX).processSubheader(
                        currentSubheaderPointer.offset, currentSubheaderPointer.length);
                if (currentRowOnPageIndex == currentPageDataSubheaderPointers.size()) {
                    readNextPage();
                    currentRowOnPageIndex = 0;
                }
                break;
            case SasFileConstants.PAGE_MIX_TYPE:
                int subheaderPointerLength = sasFileProperties.isU64() ? SasFileConstants.SUBHEADER_POINTER_LENGTH_X64
                        : SasFileConstants.SUBHEADER_POINTER_LENGTH_X86;
                int alignCorrection = (bitOffset + SasFileConstants.SUBHEADER_POINTERS_OFFSET
                        + currentPageSubheadersCount * subheaderPointerLength) % SasFileConstants.BITS_IN_BYTE;
                currentRow = processByteArrayWithData(bitOffset + SasFileConstants.SUBHEADER_POINTERS_OFFSET
                        + alignCorrection + currentPageSubheadersCount * subheaderPointerLength
                        + currentRowOnPageIndex++ * sasFileProperties.getRowLength(), sasFileProperties.getRowLength());
                if (currentRowOnPageIndex == Math.min(sasFileProperties.getRowCount(),
                        sasFileProperties.getMixPageRowCount())) {
                    readNextPage();
                    currentRowOnPageIndex = 0;
                }
                break;
            case SasFileConstants.PAGE_DATA_TYPE:
                currentRow = processByteArrayWithData(bitOffset + SasFileConstants.SUBHEADER_POINTERS_OFFSET
                        + currentRowOnPageIndex++ * sasFileProperties.getRowLength(), sasFileProperties.getRowLength());
                if (currentRowOnPageIndex == currentPageBlockCount) {
                    readNextPage();
                    currentRowOnPageIndex = 0;
                }
                break;
            default:
                break;
        }
        return Arrays.copyOf(currentRow, currentRow.length);
    }

    /**
     * The method to read next page from sas7bdat file and put it into {@link SasFileParser#cachedPage}. If this page
     * has {@link SasFileConstants#PAGE_META_TYPE} type method process it's subheaders. Method skips page with type
     * other than {@link SasFileConstants#PAGE_META_TYPE}, {@link SasFileConstants#PAGE_MIX_TYPE} or
     * {@link SasFileConstants#PAGE_DATA_TYPE} and reads next.
     *
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     */
    private void readNextPage() throws IOException {
        processNextPage();
        while (currentPageType != SasFileConstants.PAGE_META_TYPE && currentPageType != SasFileConstants.PAGE_MIX_TYPE
                && currentPageType != SasFileConstants.PAGE_DATA_TYPE) {
            if (eof) {
                return;
            }
            processNextPage();
        }
    }

    /**
     * Put next page to cache and read it's header.
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} string is impossible.
     */
    private void processNextPage() throws IOException {
        int bitOffset = sasFileProperties.isU64() ? SasFileConstants.PAGE_BIT_OFFSET_X64
                : SasFileConstants.PAGE_BIT_OFFSET_X86;
        currentPageDataSubheaderPointers.clear();

        try {
            sasFileStream.readFully(cachedPage, 0, sasFileProperties.getPageLength());
        } catch (EOFException ex) {
            eof = true;
            return;
        }

        readPageHeader();
        if (currentPageType == SasFileConstants.PAGE_META_TYPE) {
            List<SubheaderPointer> subheaderPointers = new ArrayList<SubheaderPointer>();
            processPageMetadata(bitOffset, subheaderPointers);
        }
    }

    /**
     * The method to read page metadata and store it in {@link SasFileParser#currentPageType},
     * {@link SasFileParser#currentPageBlockCount} and {@link SasFileParser#currentPageSubheadersCount}.
     *
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} string is impossible.
     */
    private void readPageHeader() throws IOException {
        int bitOffset = sasFileProperties.isU64() ? SasFileConstants.PAGE_BIT_OFFSET_X64 : SasFileConstants
                .PAGE_BIT_OFFSET_X86;
        Long[] offset = {bitOffset + SasFileConstants.PAGE_TYPE_OFFSET, bitOffset + SasFileConstants.BLOCK_COUNT_OFFSET,
                bitOffset + SasFileConstants.SUBHEADER_COUNT_OFFSET};
        Integer[] length = {SasFileConstants.PAGE_TYPE_LENGTH, SasFileConstants.BLOCK_COUNT_LENGTH, SasFileConstants
                .SUBHEADER_COUNT_LENGTH};
        List<byte[]> vars = getBytesFromFile(offset, length);

        currentPageType = bytesToShort(vars.get(0));
        LOGGER.debug("Page type: {}", currentPageType);
        currentPageBlockCount = bytesToShort(vars.get(1));
        LOGGER.debug("Block count: {}", currentPageBlockCount);
        currentPageSubheadersCount = bytesToShort(vars.get(2));
        LOGGER.debug("Subheader count: {}", currentPageSubheadersCount);
    }

    /**
     * The function to convert the array of bytes that stores the data of a row into an array of objects.
     * Each object corresponds to a table cell.
     *
     * @param rowOffset - the offset of the row in cachedPage.
     * @param rowLength - the length of the row.
     * @return the array of objects storing the data of the row.
     */
    private Object[] processByteArrayWithData(long rowOffset, long rowLength) {
        Object[] rowElements = new Object[(int) sasFileProperties.getColumnsCount()];
        byte[] temp, source;
        int offset;
        if (sasFileProperties.isCompressed() && rowLength < sasFileProperties.getRowLength()) {
            Decompressor decompressor = LITERALS_TO_DECOMPRESSOR.get(sasFileProperties.getCompressionMethod());
            source = decompressor.decompressRow((int) rowOffset, (int) rowLength,
                    (int) sasFileProperties.getRowLength(), cachedPage);
            offset = 0;
        } else {
            source = cachedPage;
            offset = (int) rowOffset;
        }

        for (int currentColumnIndex = 0; currentColumnIndex < sasFileProperties.getColumnsCount()
                && columnsDataLength.get(currentColumnIndex) != 0; currentColumnIndex++) {
            int length = columnsDataLength.get(currentColumnIndex);
            if (columns.get(currentColumnIndex).getType() == Number.class) {
                temp = Arrays.copyOfRange(source, offset + (int) (long) columnsDataOffset.get(currentColumnIndex),
                        offset + (int) (long) columnsDataOffset.get(currentColumnIndex) + length);
                if (columnsDataLength.get(currentColumnIndex) <= 2) {
                    rowElements[currentColumnIndex] = bytesToShort(temp);
                } else {
                    if (columns.get(currentColumnIndex).getFormat().isEmpty()) {
                        rowElements[currentColumnIndex] = convertByteArrayToNumber(temp);
                    } else {
                        if (SasFileConstants.DATE_TIME_FORMAT_STRINGS.contains(
                                columns.get(currentColumnIndex).getFormat())) {
                            rowElements[currentColumnIndex] = bytesToDateTime(temp);
                        } else {
                            if (SasFileConstants.DATE_FORMAT_STRINGS.contains(
                                    columns.get(currentColumnIndex).getFormat())) {
                                rowElements[currentColumnIndex] = bytesToDate(temp);
                            } else {
                                rowElements[currentColumnIndex] = convertByteArrayToNumber(temp);
                            }
                        }
                    }
                }
            } else {
                byte[] bytes = trimBytesArray(source,
                        offset + columnsDataOffset.get(currentColumnIndex).intValue(), length);
                if (byteOutput) {
                    rowElements[currentColumnIndex] = bytes;
                } else {
                    try {
                        rowElements[currentColumnIndex] = (bytes == null ? null : new String(bytes, encoding));
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }

        return rowElements;
    }

    /**
     * The function to read the list of bytes arrays from the sas7bdat file. The array of offsets and the array of
     * lengths serve as input data that define the location and number of bytes the function must read.
     *
     * @param offset the array of offsets.
     * @param length the array of lengths.
     * @return the list of bytes arrays.
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     */
    private List<byte[]> getBytesFromFile(Long[] offset, Integer[] length) throws IOException {
        List<byte[]> vars = new ArrayList<byte[]>();
        if (cachedPage == null) {
            for (int i = 0; i < offset.length; i++) {
                byte[] temp = new byte[length[i]];
                long actuallySkipped = 0;
                while (actuallySkipped < offset[i] - currentFilePosition) {
                    actuallySkipped += sasFileStream.skip(offset[i] - currentFilePosition - actuallySkipped);
                }
                try {
                    sasFileStream.readFully(temp, 0, length[i]);
                } catch (EOFException e) {
                    eof = true;
                }
                currentFilePosition = (int) (long) offset[i] + length[i];
                vars.add(temp);
            }
        } else {
            for (int i = 0; i < offset.length; i++) {
                vars.add(Arrays.copyOfRange(cachedPage, (int) (long) offset[i], (int) (long) offset[i] + length[i]));
            }
        }
        return vars;
    }

    /**
     * The function to convert a bytes array into a number (int or long depending on the value located at
     * the {@link SasFileConstants#ALIGN_2_OFFSET} offset).
     *
     * @param byteBuffer the long value represented by a bytes array.
     * @return a long value. If the number was stored as int, then after conversion it is converted to long
     * for convenience.
     */
    private long correctLongProcess(ByteBuffer byteBuffer) {
        if (sasFileProperties.isU64()) {
            return byteBuffer.getLong();
        } else {
            return byteBuffer.getInt();
        }
    }

    /**
     * The function to convert an array of bytes with any order of bytes into {@link ByteBuffer}.
     * {@link ByteBuffer} has the order of bytes defined in the file located at the
     * {@link SasFileConstants#ALIGN_2_OFFSET} offset.
     * Later the parser converts result {@link ByteBuffer} into a number.
     *
     * @param data the input array of bytes with the little-endian or big-endian order.
     * @return {@link ByteBuffer} with the order of bytes defined in the file located at
     * the {@link SasFileConstants#ALIGN_2_OFFSET} offset.
     */
    private ByteBuffer byteArrayToByteBuffer(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        if (sasFileProperties.getEndianness() == 0) {
            return byteBuffer;
        } else {
            return byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    /**
     * The function to convert an array of bytes into a number. The result can be double or long values.
     * The numbers are stored in the IEEE 754 format. A number is considered long if the difference between the whole
     * number and its integer part is less than {@link SasFileConstants#EPSILON}.
     *
     * @param mass the number represented by an array of bytes.
     * @return number of a long or double type.
     */
    private Object convertByteArrayToNumber(byte[] mass) {
        ByteBuffer original = byteArrayToByteBuffer(mass);

        if (mass.length < SasFileConstants.BYTES_IN_DOUBLE) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(SasFileConstants.BYTES_IN_DOUBLE);
            if (sasFileProperties.getEndianness() == 1) {
                byteBuffer.position(SasFileConstants.BYTES_IN_DOUBLE - mass.length);
            }
            byteBuffer.put(original);
            byteBuffer.order(original.order());
            byteBuffer.position(0);
            original = byteBuffer;
        }

        double resultDouble = original.getDouble();
        original.clear();

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
     * The function to convert an array of bytes into a numeral of the {@link Short} type.
     * For convenience, the resulting number is converted into the int type.
     *
     * @param bytes a long number represented by an array of bytes.
     * @return a number of the int type that is the conversion result.
     */
    private int bytesToShort(byte[] bytes) {
        return byteArrayToByteBuffer(bytes).getShort();
    }

    /**
     * The function to convert an array of bytes into an int number.
     *
     * @param bytes a long number represented by an array of bytes.
     * @return a number of the int type that is the conversion result.
     */
    private int bytesToInt(byte[] bytes) {
        return byteArrayToByteBuffer(bytes).getInt();
    }

    /**
     * The function to convert an array of bytes into a long number.
     *
     * @param bytes a long number represented by an array of bytes.
     * @return a number of the long type that is the conversion result.
     */
    private long bytesToLong(byte[] bytes) {
        return correctLongProcess(byteArrayToByteBuffer(bytes));
    }

    /**
     * The function to convert an array of bytes into a string.
     *
     * @param bytes a string represented by an array of bytes.
     * @return the conversion result string.
     */
    private String bytesToString(byte[] bytes) {
        return new String(bytes);
    }

    /**
     * The function to convert an array of bytes that stores the number of seconds elapsed from 01/01/1960 into
     * a variable of the {@link Date} type. The {@link SasFileConstants#DATE_TIME_FORMAT_STRINGS} variable stores
     * the formats of the columns that store such data.
     *
     * @param bytes an array of bytes that stores the type.
     * @return a variable of the {@link Date} type.
     */
    private Date bytesToDateTime(byte[] bytes) {
        double doubleSeconds = byteArrayToByteBuffer(bytes).getDouble();
        return Double.isNaN(doubleSeconds) ? null : new Date((long) ((doubleSeconds
                - SasFileConstants.START_DATES_SECONDS_DIFFERENCE) * SasFileConstants.MILLISECONDS_IN_SECONDS));
    }

    /**
     * The function to convert an array of bytes that stores the number of days elapsed from 01/01/1960 into a variable
     * of the {@link Date} type. {@link SasFileConstants#DATE_FORMAT_STRINGS} stores the formats of columns that contain
     * such data.
     *
     * @param bytes the array of bytes that stores the number of days from 01/01/1960.
     * @return a variable of the {@link Date} type.
     */
    private Date bytesToDate(byte[] bytes) {
        double doubleDays = byteArrayToByteBuffer(bytes).getDouble();
        return Double.isNaN(doubleDays) ? null : new Date((long) ((doubleDays
                - SasFileConstants.START_DATES_DAYS_DIFFERENCE)
                * SasFileConstants.SECONDS_IN_MINUTE * SasFileConstants.MINUTES_IN_HOUR
                * SasFileConstants.HOURS_IN_DAY * SasFileConstants.MILLISECONDS_IN_SECONDS));
    }

    /**
     * The function to remove excess symbols from the end of a bytes array. Excess symbols are line end characters,
     * tabulation characters, and spaces, which do not contain useful information.
     *
     * @param source an array of bytes containing required data.
     * @param offset the offset in source of required data.
     * @param length the length of required data.
     * @return the array of bytes without excess symbols at the end.
     */
    private byte[] trimBytesArray(byte[] source, int offset, int length) {
        int lengthFromBegin;
        for (lengthFromBegin = offset + length; lengthFromBegin > offset; lengthFromBegin--) {
            if (source[lengthFromBegin - 1] != ' ' && source[lengthFromBegin - 1] != '\0'
                    && source[lengthFromBegin - 1] != '\t') {
                break;
            }
        }

        if (lengthFromBegin - offset != 0) {
            return Arrays.copyOfRange(source, offset, lengthFromBegin);
        } else {
            return null;
        }
    }

    /**
     * Columns getter.
     * @return columns list.
     */
    List<Column> getColumns() {
        return columns;
    }

    /**
     * The function to get sasFileParser.
     *
     * @return the object of the {@link SasFileProperties} class that stores file metadata.
     */
    SasFileProperties getSasFileProperties() {
        return sasFileProperties;
    }

    /**
     * Enumeration of all subheader types used in sas7bdat files.
     */
    private enum SubheaderIndexes {
        /**
         * Index which define row size subheader, which contains rows size in bytes and the number of rows.
         */
        ROW_SIZE_SUBHEADER_INDEX,

        /**
         * Index which define column size subheader, which contains columns count.
         */
        COLUMN_SIZE_SUBHEADER_INDEX,

        /**
         * Index which define subheader counts subheader, which contains currently not used data.
         */
        SUBHEADER_COUNTS_SUBHEADER_INDEX,

        /**
         * Index which define column text subheader, which contains type of file compression
         * and info about columns (name, label, format).
         */
        COLUMN_TEXT_SUBHEADER_INDEX,

        /**
         * Index which define column name subheader, which contains column names.
         */
        COLUMN_NAME_SUBHEADER_INDEX,

        /**
         * Index which define column attributes subheader, which contains column attributes, such as type.
         */
        COLUMN_ATTRIBUTES_SUBHEADER_INDEX,

        /**
         * Index which define format and label subheader, which contains info about format of objects in column
         * and tooltip text for columns.
         */
        FORMAT_AND_LABEL_SUBHEADER_INDEX,

        /**
         * Index which define column list subheader, which contains currently not used data.
         */
        COLUMN_LIST_SUBHEADER_INDEX,

        /**
         * Index which define data subheader, which contains sas7bdat file rows data.
         */
        DATA_SUBHEADER_INDEX
    }

    /**
     * The interface that is implemented by all classes that process subheaders.
     */
    private interface ProcessingSubheader {
        /**
         * Method which should be overwritten in implementing this interface classes.
         * @param subheaderOffset offset in bytes from the beginning of subheader.
         * @param subheaderLength length of subheader in bytes.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        void processSubheader(long subheaderOffset, long subheaderLength) throws IOException;
    }

    /**
     * SasFileParser builder class made using builder pattern.
     */
    static class Builder {
        /**
         * Builder variable for {@link SasFileParser#sasFileStream} variable.
         */
        private InputStream sasFileStream;

        /**
         * Default value for {@link SasFileParser#encoding} variable.
         */
        private String encoding = "ASCII";

        /**
         * Default value for {@link SasFileParser#byteOutput} variable.
         */
        private Boolean byteOutput = false;

        /**
         * The function to specify builders sasFileStream variable.
         *
         * @param val value to be set.
         * @return result builder.
         */
        Builder sasFileStream(InputStream val) {
            sasFileStream = val;
            return this;
        }

        /**
         * The function to specify builders encoding variable.
         *
         * @param val value to be set.
         * @return result builder.
         */
        Builder encoding(String val) {
            encoding = val;
            return this;
        }

        /**
         * The function to specify builders byteOutput variable.
         *
         * @param val value to be set.
         * @return result builder.
         */
        Builder byteOutput(Boolean val) {
            byteOutput = val;
            return this;
        }

        /**
         * The function to create variable of SasFileParser class using current builder.
         *
         * @return newly built SasFileParser
         */
        SasFileParser build() {
            return new SasFileParser(this);
        }
    }

    /**
     * The class to store subheaders pointers that contain information about the offset, length, type
     * and compression of subheaders (see {@link SasFileConstants#TRUNCATED_SUBHEADER_ID},
     * {@link SasFileConstants#COMPRESSED_SUBHEADER_ID}, {@link SasFileConstants#COMPRESSED_SUBHEADER_TYPE}
     * for details).
     */
    class SubheaderPointer {
        /**
         * The offset from the beginning of a page at which a subheader is stored.
         */
        private final long offset;

        /**
         * The subheader length.
         */
        private final long length;

        /**
         * The type of subheader compression. If the type is {@link SasFileConstants#TRUNCATED_SUBHEADER_ID}
         * the subheader does not contain information relevant to the current issues. If the type is
         * {@link SasFileConstants#COMPRESSED_SUBHEADER_ID} the subheader can be compressed
         * (depends on {@link SubheaderPointer#type}).
         */
        private final byte compression;

        /**
         * The subheader type. If the type is {@link SasFileConstants#COMPRESSED_SUBHEADER_TYPE}
         * the subheader is compressed. Otherwise, there is no compression.
         */
        private final byte type;

        /**
         * The constructor of the {@link SubheaderPointer} class that defines values of all its variables.
         *
         * @param offset      the offset of the subheader from the beginning of the page.
         * @param length      the subheader length.
         * @param compression the subheader compression type. If the type is
         *                    {@link SasFileConstants#TRUNCATED_SUBHEADER_ID}, the subheader does not contain useful
         *                    information. If the type is {@link SasFileConstants#COMPRESSED_SUBHEADER_ID},
         *                    the subheader can be compressed (depends on {@link SubheaderPointer#type}).
         * @param type        the subheader type. If the type is {@link SasFileConstants#COMPRESSED_SUBHEADER_TYPE}
         *                    the subheader is compressed, otherwise, it is not.
         */
        SubheaderPointer(long offset, long length, byte compression, byte type) {
            this.offset = offset;
            this.length = length;
            this.compression = compression;
            this.type = type;
        }
    }

    /**
     * The class to process subheaders of the RowSizeSubheader type that store information about the table rows length
     * (in bytes), the number of rows in the table and the number of rows on the last page of the
     * {@link SasFileConstants#PAGE_MIX_TYPE} type. The results are stored in {@link SasFileProperties#rowLength},
     * {@link SasFileProperties#rowCount}, and {@link SasFileProperties#mixPageRowCount}, respectively.
     */
    class RowSizeSubheader implements ProcessingSubheader {
        /**
         * The function to read the following metadata about rows of the sas7bdat file:
         * {@link SasFileProperties#rowLength}, {@link SasFileProperties#rowCount},
         * and {@link SasFileProperties#mixPageRowCount}.
         *
         * @param subheaderOffset the offset at which the subheader is located.
         * @param subheaderLength the subheader length.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        @Override
        public void processSubheader(long subheaderOffset, long subheaderLength) throws IOException {
            int intOrLongLength = sasFileProperties.isU64() ? SasFileConstants.BYTES_IN_LONG : SasFileConstants
                    .BYTES_IN_INT;
            Long[] offset = {subheaderOffset + SasFileConstants.ROW_LENGTH_OFFSET_MULTIPLIER * intOrLongLength,
                    subheaderOffset + SasFileConstants.ROW_COUNT_OFFSET_MULTIPLIER * intOrLongLength,
                    subheaderOffset + SasFileConstants.ROW_COUNT_ON_MIX_PAGE_OFFSET_MULTIPLIER * intOrLongLength};
            Integer[] length = {intOrLongLength, intOrLongLength, intOrLongLength};
            List<byte[]> vars = getBytesFromFile(offset, length);

            if (sasFileProperties.getRowLength() == 0) {
                sasFileProperties.setRowLength(bytesToLong(vars.get(0)));
            }
            if (sasFileProperties.getRowCount() == 0) {
                sasFileProperties.setRowCount(bytesToLong(vars.get(1)));
            }
            if (sasFileProperties.getMixPageRowCount() == 0) {
                sasFileProperties.setMixPageRowCount(bytesToLong(vars.get(2)));
            }
        }
    }

    /**
     * The class to process subheaders of the ColumnSizeSubheader type that store information about
     * the number of table columns. The {@link SasFileProperties#columnsCount} variable stores the results.
     */
    class ColumnSizeSubheader implements ProcessingSubheader {
        /**
         * The function to read the following metadata about columns of the sas7bdat file:
         * {@link SasFileProperties#columnsCount}.
         *
         * @param subheaderOffset the offset at which the subheader is located.
         * @param subheaderLength the subheader length.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        @Override
        public void processSubheader(long subheaderOffset, long subheaderLength) throws IOException {
            int intOrLongLength = sasFileProperties.isU64() ? SasFileConstants.BYTES_IN_LONG : SasFileConstants
                    .BYTES_IN_INT;
            Long[] offset = {subheaderOffset + intOrLongLength};
            Integer[] length = {intOrLongLength};
            List<byte[]> vars = getBytesFromFile(offset, length);

            sasFileProperties.setColumnsCount(bytesToLong(vars.get(0)));
        }
    }

    /**
     * The class to process subheaders of the SubheaderCountsSubheader type that does not contain
     * any information relevant to the current issues.
     */
    class SubheaderCountsSubheader implements ProcessingSubheader {
        /**
         * The function to read metadata. At the moment the function is empty as the information in
         * SubheaderCountsSubheader is not needed for the current issues.
         *
         * @param subheaderOffset the offset at which the subheader is located.
         * @param subheaderLength the subheader length.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        @Override
        public void processSubheader(long subheaderOffset, long subheaderLength) throws IOException {
        }
    }

    /**
     * The class to process subheaders of the ColumnTextSubheader type that store information about
     * file compression and table columns (name, label, format). The first subheader of this type includes the file
     * compression information. The results are stored in {@link SasFileParser#columnsNamesStrings} and
     * {@link SasFileProperties#compressionMethod}.
     */
    class ColumnTextSubheader implements ProcessingSubheader {
        /**
         * The function to read the text block with information about file compression and table columns (name, label,
         * format) from a subheader. The first text block of this type includes the file compression information.
         *
         * @param subheaderOffset the offset at which the subheader is located.
         * @param subheaderLength the subheader length.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        @Override
        public void processSubheader(long subheaderOffset, long subheaderLength) throws IOException {
            int intOrLongLength = sasFileProperties.isU64() ? SasFileConstants.BYTES_IN_LONG : SasFileConstants
                    .BYTES_IN_INT;
            int textBlockSize;

            Long[] offset = {subheaderOffset + intOrLongLength};
            Integer[] length = {SasFileConstants.TEXT_BLOCK_SIZE_LENGTH};
            List<byte[]> vars = getBytesFromFile(offset, length);
            textBlockSize = byteArrayToByteBuffer(vars.get(0)).getShort();

            offset[0] = subheaderOffset + intOrLongLength;
            length[0] = textBlockSize;
            vars = getBytesFromFile(offset, length);

            columnsNamesStrings.add(bytesToString(vars.get(0)));
            if (columnsNamesStrings.size() == 1) {
                String columnName = columnsNamesStrings.get(0);
                String compessionLiteral = findCompressionLiteral(columnName);
                sasFileProperties.setCompressionMethod(compessionLiteral); //might be null
            }
        }
    }

    /**
     * The class to process subheaders of the ColumnNameSubheader type that store information about the index of
     * corresponding subheader of the ColumnTextSubheader type whose text field stores the name of the column
     * corresponding to the current subheader. They also store the offset (in symbols) of the names from the beginning
     * of the text field and the length of names (in symbols). The {@link SasFileParser#columnsNamesList} list stores
     * the resulting names.
     */
    class ColumnNameSubheader implements ProcessingSubheader {
        /**
         * The function to read the following data from the subheader:
         * - the index that stores the name of the column corresponding to the current subheader,
         * - the offset (in symbols) of the name inside the text block,
         * - the length (in symbols) of the name.
         *
         * @param subheaderOffset the offset at which the subheader is located.
         * @param subheaderLength the subheader length.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        @Override
        public void processSubheader(long subheaderOffset, long subheaderLength) throws IOException {
            int intOrLongLength = sasFileProperties.isU64() ? SasFileConstants.BYTES_IN_LONG : SasFileConstants
                    .BYTES_IN_INT;
            long columnNamePointersCount = (subheaderLength - 2 * intOrLongLength - 12) / 8;
            int i;
            for (i = 0; i < columnNamePointersCount; i++) {
                Long[] offset = {subheaderOffset + intOrLongLength + SasFileConstants.COLUMN_NAME_POINTER_LENGTH * (i
                        + 1) + SasFileConstants.COLUMN_NAME_TEXT_SUBHEADER_OFFSET,
                        subheaderOffset + intOrLongLength + SasFileConstants.COLUMN_NAME_POINTER_LENGTH * (i + 1)
                                + SasFileConstants.COLUMN_NAME_OFFSET_OFFSET,
                        subheaderOffset + intOrLongLength + SasFileConstants.COLUMN_NAME_POINTER_LENGTH * (i + 1)
                                + SasFileConstants.COLUMN_NAME_LENGTH_OFFSET};
                Integer[] length = {SasFileConstants.COLUMN_NAME_TEXT_SUBHEADER_LENGTH, SasFileConstants
                        .COLUMN_NAME_OFFSET_LENGTH, SasFileConstants.COLUMN_NAME_LENGTH_LENGTH};
                List<byte[]> vars = getBytesFromFile(offset, length);

                int textSubheaderIndex = bytesToShort(vars.get(0));
                int columnNameOffset = bytesToShort(vars.get(1));
                int columnNameLength = bytesToShort(vars.get(2));
                columnsNamesList.add(columnsNamesStrings.get(textSubheaderIndex).substring(columnNameOffset,
                        columnNameOffset + columnNameLength).intern());
            }
        }
    }

    /**
     * The class to process subheaders of the ColumnAttributesSubheader type that store information about
     * the data length (in bytes) of the current column and about the offset (in bytes) of the current column`s data
     * from the beginning of the row with data. They also store the column`s data type: {@link Number} and
     * {@link String}. The resulting names are stored in the {@link SasFileParser#columnsDataOffset},
     * {@link SasFileParser#columnsDataLength}, and{@link SasFileParser#columnsTypesList}.
     */
    class ColumnAttributesSubheader implements ProcessingSubheader {
        /**
         * The function to read the length, offset and type of data in cells related to the column from the subheader
         * that stores information about this column.
         *
         * @param subheaderOffset the offset at which the subheader is located.
         * @param subheaderLength the subheader length.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        @Override
        public void processSubheader(long subheaderOffset, long subheaderLength) throws IOException {
            int intOrLongLength = sasFileProperties.isU64() ? SasFileConstants.BYTES_IN_LONG : SasFileConstants
                    .BYTES_IN_INT;
            long columnAttributesVectorsCount = (subheaderLength - 2 * intOrLongLength - 12) / (intOrLongLength + 8);
            for (int i = 0; i < columnAttributesVectorsCount; i++) {
                Long[] offset = {subheaderOffset + intOrLongLength + SasFileConstants.COLUMN_DATA_OFFSET_OFFSET
                        + i * (intOrLongLength + 8),
                        subheaderOffset + 2 * intOrLongLength + SasFileConstants.COLUMN_DATA_LENGTH_OFFSET
                                + i * (intOrLongLength + 8),
                        subheaderOffset + 2 * intOrLongLength + SasFileConstants.COLUMN_TYPE_OFFSET
                                + i * (intOrLongLength + 8)};
                Integer[] length = {intOrLongLength, SasFileConstants.COLUMN_DATA_LENGTH_LENGTH, SasFileConstants
                        .COLUMN_TYPE_LENGTH};
                List<byte[]> vars = getBytesFromFile(offset, length);

                columnsDataOffset.add(bytesToLong(vars.get(0)));
                columnsDataLength.add(bytesToInt(vars.get(1)));
                columnsTypesList.add(vars.get(2)[0] == 1 ? Number.class : String.class);
            }
        }
    }

    /**
     * The class to process subheaders of the FormatAndLabelSubheader type that store the following information:
     * - the index of the ColumnTextSubheader type subheader whose text field contains the column format,
     * - the index of the ColumnTextSubheader type whose text field stores the label of the column corresponding
     * to the current subheader,
     * - offsets (in symbols) of the formats and labels from the beginning of the text field,
     * - lengths of the formats and labels (in symbols),
     * The {@link SasFileParser#columns} list stores the results.
     */
    class FormatAndLabelSubheader implements ProcessingSubheader {
        /**
         * The function to read the following data from the subheader:
         * - the index that stores the format of the column corresponding to the current subheader,
         * - the offset (in symbols) of the format inside the text block,
         * - the format length (in symbols),
         * - the index that stores the label of the column corresponding to the current subheader,
         * - the offset (in symbols) of the label inside the text block,
         * - the label length (in symbols).
         *
         * @param subheaderOffset the offset at which the subheader is located.
         * @param subheaderLength the subheader length.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        @Override
        public void processSubheader(long subheaderOffset, long subheaderLength) throws IOException {
            int intOrLongLength = sasFileProperties.isU64() ? SasFileConstants.BYTES_IN_LONG : SasFileConstants
                    .BYTES_IN_INT;
            Long[] offset = {subheaderOffset + SasFileConstants.COLUMN_FORMAT_TEXT_SUBHEADER_INDEX_OFFSET + 3
                    * intOrLongLength,
                    subheaderOffset + SasFileConstants.COLUMN_FORMAT_OFFSET_OFFSET + 3 * intOrLongLength,
                    subheaderOffset + SasFileConstants.COLUMN_FORMAT_LENGTH_OFFSET + 3 * intOrLongLength,
                    subheaderOffset + SasFileConstants.COLUMN_LABEL_TEXT_SUBHEADER_INDEX_OFFSET + 3 * intOrLongLength,
                    subheaderOffset + SasFileConstants.COLUMN_LABEL_OFFSET_OFFSET + 3 * intOrLongLength,
                    subheaderOffset + SasFileConstants.COLUMN_LABEL_LENGTH_OFFSET + 3 * intOrLongLength};
            Integer[] length = {SasFileConstants.COLUMN_FORMAT_TEXT_SUBHEADER_INDEX_LENGTH, SasFileConstants
                    .COLUMN_FORMAT_OFFSET_LENGTH,
                    SasFileConstants.COLUMN_FORMAT_LENGTH_LENGTH, SasFileConstants
                    .COLUMN_LABEL_TEXT_SUBHEADER_INDEX_LENGTH,
                    SasFileConstants.COLUMN_LABEL_OFFSET_LENGTH, SasFileConstants.COLUMN_LABEL_LENGTH_LENGTH};
            List<byte[]> vars = getBytesFromFile(offset, length);

            // min used to prevent incorrect data which appear in some files
            int textSubheaderIndexForFormat = Math.min(bytesToShort(vars.get(0)), columnsNamesStrings.size() - 1);
            int columnFormatOffset = bytesToShort(vars.get(1));
            int columnFormatLength = bytesToShort(vars.get(2));
            // min used to prevent incorrect data which appear in some files
            int textSubheaderIndexForLabel = Math.min(bytesToShort(vars.get(3)), columnsNamesStrings.size() - 1);
            int columnLabelOffset = bytesToShort(vars.get(4));
            int columnLabelLength = bytesToShort(vars.get(5));
            String columnLabel = columnsNamesStrings.get(textSubheaderIndexForLabel).substring(
                    columnLabelOffset, columnLabelOffset + columnLabelLength).intern();
            String columnFormat = columnsNamesStrings.get(textSubheaderIndexForFormat).substring(
                    columnFormatOffset, columnFormatOffset + columnFormatLength).intern();
            LOGGER.debug("Column format: {}", columnFormat);
            columns.add(new Column(currentColumnNumber + 1, columnsNamesList.get(columns.size()),
                    columnLabel, columnFormat, columnsTypesList.get(columns.size()),
                    columnsDataLength.get(currentColumnNumber++)));
        }
    }

    /**
     * The class to process subheaders of the ColumnListSubheader type that do not store any information relevant
     * to the current tasks.
     */
    class ColumnListSubheader implements ProcessingSubheader {
        /**
         * The method to read metadata. It is empty at the moment because the data stored in ColumnListSubheader
         * are not used.
         *
         * @param subheaderOffset the offset at which the subheader is located.
         * @param subheaderLength the subheader length.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        @Override
        public void processSubheader(long subheaderOffset, long subheaderLength) throws IOException {
        }
    }

    /**
     * The class to process subheaders of the DataSubheader type that keep compressed or uncompressed data.
     */
    class DataSubheader implements ProcessingSubheader {
        /**
         * The method to read compressed or uncompressed data from the subheader. The results are stored as a row
         * in {@link SasFileParser#currentRow}. The {@link SasFileParser#processByteArrayWithData(long, long)} function
         * converts the array of bytes into a list of objects.
         *
         * @param subheaderOffset the offset at which the subheader is located.
         * @param subheaderLength the subheader length.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        @Override
        public void processSubheader(long subheaderOffset, long subheaderLength) throws IOException {
            currentRow = processByteArrayWithData(subheaderOffset, subheaderLength);
        }
    }
}
