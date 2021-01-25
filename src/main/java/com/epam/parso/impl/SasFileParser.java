/**
 * *************************************************************************
 * Copyright (C) 2015 EPAM
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * *************************************************************************
 */

package com.epam.parso.impl;

import com.epam.parso.Column;
import com.epam.parso.ColumnFormat;
import com.epam.parso.ColumnMissingInfo;
import com.epam.parso.SasFileProperties;
import com.epam.parso.date.OutputDateType;
import com.epam.parso.date.SasTemporalFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import static com.epam.parso.impl.ParserMessageConstants.BLOCK_COUNT;
import static com.epam.parso.impl.ParserMessageConstants.COLUMN_FORMAT;
import static com.epam.parso.impl.ParserMessageConstants.EMPTY_INPUT_STREAM;
import static com.epam.parso.impl.ParserMessageConstants.FILE_NOT_VALID;
import static com.epam.parso.impl.ParserMessageConstants.NO_SUPPORTED_COMPRESSION_LITERAL;
import static com.epam.parso.impl.ParserMessageConstants.NULL_COMPRESSION_LITERAL;
import static com.epam.parso.impl.ParserMessageConstants.PAGE_TYPE;
import static com.epam.parso.impl.ParserMessageConstants.SUBHEADER_COUNT;
import static com.epam.parso.impl.ParserMessageConstants.SUBHEADER_PROCESS_FUNCTION_NAME;
import static com.epam.parso.impl.ParserMessageConstants.UNKNOWN_SUBHEADER_SIGNATURE;
import static com.epam.parso.impl.SasFileConstants.*;

/**
 * This is a class that parses sas7bdat files. When parsing a sas7bdat file, to interact with the library
 * use {@link SasFileReaderImpl} which is a wrapper for SasFileParser. Despite this, SasFileParser
 * is publicly available, it can be instanced via {@link SasFileParser.Builder} and used directly.
 * Public access to the SasFileParser class was added in scope of this issue:
 * @see <a href="https://github.com/epam/parso/issues/51"></a>.
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
    private static final Map<String, Decompressor> LITERALS_TO_DECOMPRESSOR = new HashMap<>();

    /**
     * Sanity check on maximum page length.
     */
    private static final int MAX_PAGE_LENGTH = 10000000;

    /**
     * Byte buffer used for skip operations.
     * Actually the data containing in this buffer is ignored,
     * because it only used for dummy reads.
     */
    private static final byte[] SKIP_BYTE_BUFFER = new byte[4096];

    static {
        Map<Long, SubheaderIndexes> tmpMap = new HashMap<>();
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
        tmpMap.put(0xF7F7F7F7FFFFFBFEL, SubheaderIndexes.ROW_SIZE_SUBHEADER_INDEX);
        tmpMap.put(0xF6F6F6F6FFFFFBFEL, SubheaderIndexes.COLUMN_SIZE_SUBHEADER_INDEX);
        tmpMap.put(0x00FCFFFFFFFFFFFFL, SubheaderIndexes.SUBHEADER_COUNTS_SUBHEADER_INDEX);
        tmpMap.put(0xFDFFFFFFFFFFFFFFL, SubheaderIndexes.COLUMN_TEXT_SUBHEADER_INDEX);
        tmpMap.put(0xFFFFFFFFFFFFFFFFL, SubheaderIndexes.COLUMN_NAME_SUBHEADER_INDEX);
        tmpMap.put(0xFCFFFFFFFFFFFFFFL, SubheaderIndexes.COLUMN_ATTRIBUTES_SUBHEADER_INDEX);
        tmpMap.put(0xFEFBFFFFFFFFFFFFL, SubheaderIndexes.FORMAT_AND_LABEL_SUBHEADER_INDEX);
        tmpMap.put(0xFEFFFFFFFFFFFFFFL, SubheaderIndexes.COLUMN_LIST_SUBHEADER_INDEX);
        SUBHEADER_SIGNATURE_TO_INDEX = Collections.unmodifiableMap(tmpMap);
    }

    static {
        LITERALS_TO_DECOMPRESSOR.put(COMPRESS_CHAR_IDENTIFYING_STRING, CharDecompressor.INSTANCE);
        LITERALS_TO_DECOMPRESSOR.put(COMPRESS_BIN_IDENTIFYING_STRING, BinDecompressor.INSTANCE);
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
     * Output date type.
     */
    private final OutputDateType outputDateType;

    /**
     * The list of current page data subheaders.
     */
    private final List<SubheaderPointer> currentPageDataSubheaderPointers = new ArrayList<>();
    /**
     * The variable to store all the properties from the sas7bdat file.
     */
    private final SasFileProperties sasFileProperties = new SasFileProperties();
    /**
     * The list of text blocks with information about file compression and table columns (name, label, format).
     * Every element corresponds to a {@link SasFileParser.ColumnTextSubheader}. The first text block includes
     * the information about compression.
     */
    private final List<byte[]> columnsNamesBytes = new ArrayList<>();
    /**
     * The list of column names.
     */
    private final List<String> columnsNamesList = new ArrayList<>();
    /**
     * The list of column types. There can be {@link Number} and {@link String} types.
     */
    private final List<Class<?>> columnsTypesList = new ArrayList<>();
    /**
     * The list of offsets of data in every column inside a row. Used to locate the left border of a cell.
     */
    private final List<Long> columnsDataOffset = new ArrayList<>();
    /**
     * The list of data lengths of every column inside a row. Used to locate the right border of a cell.
     */
    private final List<Integer> columnsDataLength = new ArrayList<>();
    /**
     * The list of table columns to store their name, label, and format.
     */
    private final List<Column> columns = new ArrayList<>();
    /**
     * The mapping between elements from {@link SubheaderIndexes} and classes corresponding
     * to each subheader. This is necessary because defining the subheader type being processed is dynamic.
     * Every class has an overridden function that processes the related subheader type.
     */
    private final Map<SubheaderIndexes, ProcessingSubheader> subheaderIndexToClass;
    /**
     * Default encoding for output strings.
     */
    private String encoding = "US-ASCII";
    /**
     * A cache to store the current page of the sas7bdat file. Used to avoid posing buffering requirements
     * to {@link SasFileParser#sasFileStream}.
     */
    private byte[] cachedPage;
    /**
     * The type of the current page when reading the file. If it is other than
     * {@link PageType#PAGE_TYPE_META}, {@link PageType#PAGE_TYPE_MIX}, {@link PageType#PAGE_TYPE_DATA}
     * and {@link PageType#PAGE_TYPE_AMD} page is skipped.
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
     * The offset of the file label from the beginning of the {@link SasFileParser.ColumnTextSubheader} subheader.
     */
    private int fileLabelOffset;

    /**
     * The offset of the compression method from the beginning of the
     * {@link SasFileParser.ColumnTextSubheader} subheader.
     */
    private int compressionMethodOffset;

    /**
     * The length of the compression method which is stored in the
     * {@link SasFileParser.ColumnTextSubheader} subheader
     * with {@link SasFileParser#compressionMethodOffset} offset.
     */
    private int compressionMethodLength;

    /**
     * The length of file label which is stored in the {@link SasFileParser.ColumnTextSubheader} subheader
     * with {@link SasFileParser#fileLabelOffset} offset.
     */
    private int fileLabelLength;

    /**
     * The list of missing column information.
     */
    private final List<ColumnMissingInfo> columnMissingInfoList = new ArrayList<>();

    /**
     * An bit representation in String marking deleted records.
     */
    private String deletedMarkers = "";

    /**
     * Instance of SasDateFormatter.
     */
    private final SasTemporalFormatter sasTemporalFormatter = new SasTemporalFormatter();

    /**
     * The constructor that reads metadata from the sas7bdat, parses it and puts the results in
     * {@link SasFileParser#sasFileProperties}.
     *
     * @param builder the container with properties information.
     */
    private SasFileParser(Builder builder) {
        sasFileStream = new DataInputStream(builder.sasFileStream);
        byteOutput = builder.byteOutput;
        outputDateType = builder.outputDateType;

        Map<SubheaderIndexes, ProcessingSubheader> tmpMap = new HashMap<>();
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
            getMetadataFromSasFile(builder.encoding);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * The method that reads and parses metadata from the sas7bdat and puts the results in
     * {@link SasFileParser#sasFileProperties}.
     *
     * @param encoding - builder variable for {@link SasFileParser#encoding} variable.
     * @throws IOException - appears if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     */
    private void getMetadataFromSasFile(String encoding) throws IOException {
        boolean endOfMetadata = false;
        processSasFileHeader(encoding);
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
     * @param builderEncoding - builder variable for {@link SasFileParser#encoding} variable.
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     */
    private void processSasFileHeader(String builderEncoding) throws IOException {
        int align1 = 0;
        int align2 = 0;

        Long[] offsetForAlign = {ALIGN_1_OFFSET, ALIGN_2_OFFSET};
        Integer[] lengthForAlign = {ALIGN_1_LENGTH, ALIGN_2_LENGTH};
        List<byte[]> varsForAlign = getBytesFromFile(offsetForAlign, lengthForAlign);

        if (varsForAlign.get(0)[0] == U64_BYTE_CHECKER_VALUE) {
            align2 = ALIGN_2_VALUE;
            sasFileProperties.setU64(true);
        }

        if (varsForAlign.get(1)[0] == ALIGN_1_CHECKER_VALUE) {
            align1 = ALIGN_1_VALUE;
        }

        int totalAlign = align1 + align2;

        Long[] offset = {ENDIANNESS_OFFSET, ENCODING_OFFSET, DATASET_OFFSET, FILE_TYPE_OFFSET,
                DATE_CREATED_OFFSET + align1, DATE_MODIFIED_OFFSET + align1, HEADER_SIZE_OFFSET + align1,
                PAGE_SIZE_OFFSET + align1, PAGE_COUNT_OFFSET + align1, SAS_RELEASE_OFFSET + totalAlign,
                SAS_SERVER_TYPE_OFFSET + totalAlign, OS_VERSION_NUMBER_OFFSET + totalAlign,
                OS_MAKER_OFFSET + totalAlign, OS_NAME_OFFSET + totalAlign};
        Integer[] length = {ENDIANNESS_LENGTH, ENCODING_LENGTH, DATASET_LENGTH, FILE_TYPE_LENGTH, DATE_CREATED_LENGTH,
                DATE_MODIFIED_LENGTH, HEADER_SIZE_LENGTH, PAGE_SIZE_LENGTH, PAGE_COUNT_LENGTH + align2,
                SAS_RELEASE_LENGTH, SAS_SERVER_TYPE_LENGTH, OS_VERSION_NUMBER_LENGTH, OS_MAKER_LENGTH, OS_NAME_LENGTH};
        List<byte[]> vars = getBytesFromFile(offset, length);

        sasFileProperties.setEndianness(vars.get(0)[0]);
        if (!isSasFileValid()) {
            throw new IOException(FILE_NOT_VALID);
        }

        String fileEncoding = SAS_CHARACTER_ENCODINGS.get(vars.get(1)[0]);
        if (builderEncoding != null) {
            this.encoding = builderEncoding;
        } else {
            this.encoding = fileEncoding != null ? fileEncoding : this.encoding;
        }
        sasFileProperties.setEncoding(fileEncoding);
        sasFileProperties.setName(bytesToString(vars.get(2)).trim());
        sasFileProperties.setFileType(bytesToString(vars.get(3)).trim());
        sasFileProperties.setDateCreated(bytesToDateTime(vars.get(4)));
        sasFileProperties.setDateModified(bytesToDateTime(vars.get(5)));
        sasFileProperties.setHeaderLength(bytesToInt(vars.get(6)));
        int pageLength = bytesToInt(vars.get(7));
        if (pageLength > MAX_PAGE_LENGTH) {
            throw new IOException("Page limit ("
                    + pageLength + ") exceeds maximum: " + MAX_PAGE_LENGTH);
        }
        sasFileProperties.setPageLength(pageLength);
        sasFileProperties.setPageCount(bytesToLong(vars.get(8)));
        sasFileProperties.setSasRelease(bytesToString(vars.get(9)).trim());
        sasFileProperties.setServerType(bytesToString(vars.get(10)).trim());
        sasFileProperties.setOsType(bytesToString(vars.get(11)).trim());
        if (vars.get(13)[0] != 0) {
            sasFileProperties.setOsName(bytesToString(vars.get(13)).trim());
        } else {
            sasFileProperties.setOsName(bytesToString(vars.get(12)).trim());
        }

        if (sasFileStream != null) {
            skipBytes(sasFileProperties.getHeaderLength() - currentFilePosition);
            currentFilePosition = 0;
        }
    }

    /**
     * Skip specified number of bytes of data from the input stream,
     * or fail if there are not enough left.
     *
     * @param numberOfBytesToSkip the number of bytes to skip
     * @throws IOException if the number of bytes skipped was incorrect
     */
    private void skipBytes(long numberOfBytesToSkip) throws IOException {

        long remainBytes = numberOfBytesToSkip;
        long readBytes;
        while (remainBytes > 0) {
            try {
                readBytes = sasFileStream.read(SKIP_BYTE_BUFFER, 0,
                        (int) Math.min(remainBytes, SKIP_BYTE_BUFFER.length));
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

    /**
     * The method to validate sas7bdat file. If sasFileProperties contains an encoding value other than
     * {@link SasFileConstants#LITTLE_ENDIAN_CHECKER} or {@link SasFileConstants#BIG_ENDIAN_CHECKER}
     * the file is considered invalid.
     *
     * @return true if the value of encoding equals to {@link SasFileConstants#LITTLE_ENDIAN_CHECKER}
     * or {@link SasFileConstants#BIG_ENDIAN_CHECKER}
     */
    private boolean isSasFileValid() {
        return sasFileProperties.getEndianness() == LITTLE_ENDIAN_CHECKER
                || sasFileProperties.getEndianness() == BIG_ENDIAN_CHECKER;
    }

    /**
     * The method to read pages of the sas7bdat file. First, the method reads the page type
     * (at the {@link SasFileConstants#PAGE_TYPE_OFFSET} offset), the number of rows on the page
     * (at the {@link SasFileConstants#BLOCK_COUNT_OFFSET} offset), and the number of subheaders
     * (at the {@link SasFileConstants#SUBHEADER_COUNT_OFFSET} offset). Then, depending on the page type,
     * the method calls the function to process the page.
     *
     * @return true if all metadata is read.
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     */
    private boolean processSasFilePageMeta() throws IOException {
        int bitOffset = sasFileProperties.isU64() ? PAGE_BIT_OFFSET_X64 : PAGE_BIT_OFFSET_X86;
        readPageHeader();
        List<SubheaderPointer> subheaderPointers = new ArrayList<>();
        if (PageType.PAGE_TYPE_META.contains(currentPageType) || PageType.PAGE_TYPE_MIX.contains(currentPageType)) {
            processPageMetadata(bitOffset, subheaderPointers);
        }
        return PageType.PAGE_TYPE_DATA.contains(currentPageType) || PageType.PAGE_TYPE_MIX.contains(currentPageType)
                || currentPageDataSubheaderPointers.size() != 0;
    }

    /**
     * The method to parse and read metadata of a page, used for pages of the {@link PageType#PAGE_TYPE_META}
     * and {@link PageType#PAGE_TYPE_MIX} types. The method goes through subheaders, one by one, and calls
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
            try {
                SubheaderPointer currentSubheaderPointer = processSubheaderPointers((long) bitOffset
                        + SUBHEADER_POINTERS_OFFSET, subheaderPointerIndex);
                subheaderPointers.add(currentSubheaderPointer);
                if (currentSubheaderPointer.compression != TRUNCATED_SUBHEADER_ID) {
                    long subheaderSignature = readSubheaderSignature(currentSubheaderPointer.offset);
                    SubheaderIndexes subheaderIndex = chooseSubheaderClass(subheaderSignature,
                            currentSubheaderPointer.compression, currentSubheaderPointer.type);
                    if (subheaderIndex != null) {
                        if (subheaderIndex != SubheaderIndexes.DATA_SUBHEADER_INDEX) {
                            LOGGER.debug(SUBHEADER_PROCESS_FUNCTION_NAME, subheaderIndex);
                            subheaderIndexToClass.get(subheaderIndex).processSubheader(
                                    subheaderPointers.get(subheaderPointerIndex).offset,
                                    subheaderPointers.get(subheaderPointerIndex).length);
                        } else {
                            currentPageDataSubheaderPointers.add(subheaderPointers.get(subheaderPointerIndex));
                        }
                    } else {
                        LOGGER.debug(UNKNOWN_SUBHEADER_SIGNATURE);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Encountered broken page metadata. Skipping subheader.");
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
        int intOrLongLength = sasFileProperties.isU64() ? BYTES_IN_LONG : BYTES_IN_INT;
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
        if (sasFileProperties.isCompressed() && subheaderIndex == null && (compression == COMPRESSED_SUBHEADER_ID
                || compression == 0) && type == COMPRESSED_SUBHEADER_TYPE) {
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
        int intOrLongLength = sasFileProperties.isU64() ? BYTES_IN_LONG : BYTES_IN_INT;
        int subheaderPointerLength = sasFileProperties.isU64() ? SUBHEADER_POINTER_LENGTH_X64
                : SUBHEADER_POINTER_LENGTH_X86;
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
     * Match the input string against the known compression methods.
     *
     * @param compressionMethod the name of the compression method, like "SASYZCRL"
     * @return true if the method is matched or false otherwise.
     */
    private boolean matchCompressionMethod(String compressionMethod) {
        if (compressionMethod == null) {
            LOGGER.warn(NULL_COMPRESSION_LITERAL);
            return false;
        }
        if (LITERALS_TO_DECOMPRESSOR.containsKey(compressionMethod)) {
            return true;
        }
        LOGGER.debug(NO_SUPPORTED_COMPRESSION_LITERAL);
        return false;
    }

    /**
     * The function to return the index of the current row when reading the file sas7bdat file.
     *
     * @return current row index
     */
    Integer getOffset() {
      return currentRowInFileIndex;
    }

    /**
     * The function to read and process all columns of next row from current sas7bdat file.
     *
     * @return the object array containing elements of current row.
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     */
    public Object[] readNext() throws IOException {
        return readNext(null);
    }

    /**
     * The function to read and process specified columns of next row from current sas7bdat file.
     *
     * @param columnNames list of column names which should be processed, if null then all columns are processed.
     * @return the object array containing elements of current row.
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     */
    public Object[] readNext(List<String> columnNames) throws IOException {
        if (currentRowInFileIndex++ >= sasFileProperties.getRowCount() || eof) {
            return null;
        }
        int bitOffset = sasFileProperties.isU64() ? PAGE_BIT_OFFSET_X64 : PAGE_BIT_OFFSET_X86;
        currentRow = null;
        switch (currentPageType) {
            case PAGE_META_TYPE_1:
            case PAGE_META_TYPE_2:
            case PAGE_CMETA_TYPE:
                if (currentPageDataSubheaderPointers.size() == 0 && currentPageType == PAGE_CMETA_TYPE) {
                    readNextPage();
                    currentRowOnPageIndex = 0;
                }
                SubheaderPointer currentSubheaderPointer =
                        currentPageDataSubheaderPointers.get(currentRowOnPageIndex++);
                ((ProcessingDataSubheader) subheaderIndexToClass.get(SubheaderIndexes.DATA_SUBHEADER_INDEX))
                        .processSubheader(currentSubheaderPointer.offset, currentSubheaderPointer.length, columnNames);
                if (currentRowOnPageIndex == currentPageDataSubheaderPointers.size()) {
                    readNextPage();
                    currentRowOnPageIndex = 0;
                }
                break;
            case PAGE_MIX_TYPE_1:
                // Mix pages that contain all valid records
                int subheaderPointerLength = sasFileProperties.isU64() ? SUBHEADER_POINTER_LENGTH_X64
                        : SUBHEADER_POINTER_LENGTH_X86;
                int alignCorrection = (bitOffset + SUBHEADER_POINTERS_OFFSET + currentPageSubheadersCount
                        * subheaderPointerLength) % BITS_IN_BYTE;

                currentRow = processByteArrayWithData(bitOffset + SUBHEADER_POINTERS_OFFSET + alignCorrection
                        + currentPageSubheadersCount * subheaderPointerLength + currentRowOnPageIndex++
                        * sasFileProperties.getRowLength(), sasFileProperties.getRowLength(), columnNames);

                if (currentRowOnPageIndex == Math.min(sasFileProperties.getRowCount(),
                        sasFileProperties.getMixPageRowCount())) {
                    readNextPage();
                    currentRowOnPageIndex = 0;
                }
                break;
            case PAGE_MIX_TYPE_2:
                // Mix pages that contain valid and deleted records
                if (Objects.equals(deletedMarkers, "")) {
                    readDeletedInfo();
                    LOGGER.info(deletedMarkers);
                }
                subheaderPointerLength = sasFileProperties.isU64() ? SUBHEADER_POINTER_LENGTH_X64
                        : SUBHEADER_POINTER_LENGTH_X86;
                alignCorrection = (bitOffset + SUBHEADER_POINTERS_OFFSET + currentPageSubheadersCount
                        * subheaderPointerLength) % BITS_IN_BYTE;

                if (deletedMarkers.charAt(currentRowOnPageIndex) == '0') {
                    currentRow = processByteArrayWithData(bitOffset + SUBHEADER_POINTERS_OFFSET + alignCorrection
                            + currentPageSubheadersCount * subheaderPointerLength + currentRowOnPageIndex++
                            * sasFileProperties.getRowLength(), sasFileProperties.getRowLength(), columnNames);
                } else {
                    currentRowOnPageIndex++;
                }
                if (currentRowOnPageIndex == Math.min(sasFileProperties.getRowCount(),
                        sasFileProperties.getMixPageRowCount())) {
                    readNextPage();
                    currentRowOnPageIndex = 0;
                }
                break;
            case PAGE_DATA_TYPE:
                // Data pages that contain all valid records
                currentRow = processByteArrayWithData(bitOffset + SUBHEADER_POINTERS_OFFSET + currentRowOnPageIndex++
                        * sasFileProperties.getRowLength(), sasFileProperties.getRowLength(), columnNames);
                if (currentRowOnPageIndex == currentPageBlockCount) {
                    readNextPage();
                    currentRowOnPageIndex = 0;
                }
                break;
            case PAGE_DATA_TYPE_2:
                // Data pages that contain valid and deleted records
                if (Objects.equals(deletedMarkers, "")) {
                    readDeletedInfo();
                    LOGGER.info(deletedMarkers);
                    LOGGER.info(Integer.toString(deletedMarkers.length()));
                    LOGGER.info(Integer.toString(currentPageBlockCount));
                }
                if (deletedMarkers.charAt(currentRowOnPageIndex) == '0') {
                    currentRow = processByteArrayWithData(bitOffset + SUBHEADER_POINTERS_OFFSET
                            + currentRowOnPageIndex++
                            * sasFileProperties.getRowLength(), sasFileProperties.getRowLength(), columnNames);
                } else {
                    currentRowOnPageIndex++;
                }
                if (currentRowOnPageIndex == currentPageBlockCount) {
                    readNextPage();
                    currentRowOnPageIndex = 0;
                }
                break;
            default:
                break;
        }
        if (currentRow == null) {
            return null;
        }
        return Arrays.copyOf(currentRow, currentRow.length);
    }

    /**
     * The method to read next page from sas7bdat file and put it into {@link SasFileParser#cachedPage}. If this page
     * has {@link PageType#PAGE_TYPE_META} type method process it's subheaders. Method skips page with type other
     * than {@link PageType#PAGE_TYPE_META}, {@link PageType#PAGE_TYPE_MIX} or {@link PageType#PAGE_TYPE_DATA}
     * and reads next.
     *
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
     */
    private void readNextPage() throws IOException {
        deletedMarkers = "";
        processNextPage();
        while (!PageType.PAGE_TYPE_META.contains(currentPageType) && !PageType.PAGE_TYPE_MIX.contains(currentPageType)
                && !PageType.PAGE_TYPE_DATA.contains(currentPageType)) {
            if (eof) {
                return;
            }
            processNextPage();
        }
    }

    /**
     * Put next page to cache and read it's header.
     *
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} string is impossible.
     */
    private void processNextPage() throws IOException {
        int bitOffset = sasFileProperties.isU64() ? PAGE_BIT_OFFSET_X64 : PAGE_BIT_OFFSET_X86;
        currentPageDataSubheaderPointers.clear();

        try {
            sasFileStream.readFully(cachedPage, 0, sasFileProperties.getPageLength());
        } catch (EOFException ex) {
            eof = true;
            return;
        }

        readPageHeader();
        if (PageType.PAGE_TYPE_META.contains(currentPageType) || PageType.PAGE_TYPE_AMD.contains(currentPageType)
                || PageType.PAGE_TYPE_MIX.contains(currentPageType)) {
            List<SubheaderPointer> subheaderPointers = new ArrayList<>();
            processPageMetadata(bitOffset, subheaderPointers);
            readDeletedInfo();
            if (PageType.PAGE_TYPE_AMD.contains(currentPageType)) {
                processMissingColumnInfo();
            }
        }
    }

    /**
     * The function to process missing column information.
     *
     * @throws UnsupportedEncodingException when unknown encoding.
     */
    private void processMissingColumnInfo() throws UnsupportedEncodingException {
        for (ColumnMissingInfo columnMissingInfo : columnMissingInfoList) {
            String missedInfo = bytesToString(columnsNamesBytes.get(columnMissingInfo.getTextSubheaderIndex()),
                    columnMissingInfo.getOffset(), columnMissingInfo.getLength()).intern();
            Column column = columns.get(columnMissingInfo.getColumnId());
            switch (columnMissingInfo.getMissingInfoType()) {
                case NAME:
                    column.setName(missedInfo);
                    break;
                case FORMAT:
                    column.setFormat(new ColumnFormat(missedInfo));
                    break;
                case LABEL:
                    column.setLabel(missedInfo);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * The method to read page metadata and store it in {@link SasFileParser#currentPageType},
     * {@link SasFileParser#currentPageBlockCount} and {@link SasFileParser#currentPageSubheadersCount}.
     *
     * @throws IOException if reading from the {@link SasFileParser#sasFileStream} string is impossible.
     */
    private void readPageHeader() throws IOException {
        int bitOffset = sasFileProperties.isU64() ? PAGE_BIT_OFFSET_X64 : PAGE_BIT_OFFSET_X86;
        Long[] offset = {bitOffset + PAGE_TYPE_OFFSET, bitOffset + BLOCK_COUNT_OFFSET, bitOffset
                + SUBHEADER_COUNT_OFFSET};
        Integer[] length = {PAGE_TYPE_LENGTH, BLOCK_COUNT_LENGTH, SUBHEADER_COUNT_LENGTH};
        List<byte[]> vars = getBytesFromFile(offset, length);

        currentPageType = bytesToShort(vars.get(0));
        LOGGER.debug(PAGE_TYPE, currentPageType);
        currentPageBlockCount = bytesToShort(vars.get(1));
        LOGGER.debug(BLOCK_COUNT, currentPageBlockCount);
        currentPageSubheadersCount = bytesToShort(vars.get(2));
        LOGGER.debug(SUBHEADER_COUNT, currentPageSubheadersCount);
    }

    /**
     * The function to get the deleted record pointers.
     *
     * @throws IOException if reading pointers is impossible.
     */
    private void readDeletedInfo() throws IOException {
        long deletedPointerOffset;
        int subheaderPointerLength;
        int bitOffset;
        if (sasFileProperties.isU64()) {
            deletedPointerOffset = PAGE_DELETED_POINTER_OFFSET_X64;
            subheaderPointerLength = SUBHEADER_POINTER_LENGTH_X64;
            bitOffset = PAGE_BIT_OFFSET_X64 + 8;
        } else {
            deletedPointerOffset = PAGE_DELETED_POINTER_OFFSET_X86;
            subheaderPointerLength = SUBHEADER_POINTER_LENGTH_X86;
            bitOffset = PAGE_BIT_OFFSET_X86 + 8;
        }
        int alignCorrection = (bitOffset + SUBHEADER_POINTERS_OFFSET + currentPageSubheadersCount
                * subheaderPointerLength) % BITS_IN_BYTE;
        List<byte[]> vars = getBytesFromFile(new Long[] {deletedPointerOffset},
                new Integer[] {PAGE_DELETED_POINTER_LENGTH});

        long currentPageDeletedPointer = bytesToInt(vars.get(0));
        long deletedMapOffset = bitOffset + currentPageDeletedPointer + alignCorrection
                + (currentPageSubheadersCount * subheaderPointerLength)
                + ((currentPageBlockCount - currentPageSubheadersCount) * sasFileProperties.getRowLength());
        List<byte[]> bytes = getBytesFromFile(new Long[] {deletedMapOffset},
            new Integer[] {(int) Math.ceil((currentPageBlockCount - currentPageSubheadersCount) / 8.0)});

        byte[] x = bytes.get(0);
        for (byte b : x) {
            deletedMarkers += String.format("%8s", Integer.toString(b & 0xFF, 2)).replace(" ", "0");
        }
    }

    /**
     * The function to convert the array of bytes that stores the data of a row into an array of objects.
     * Each object corresponds to a table cell.
     *
     * @param rowOffset   - the offset of the row in cachedPage.
     * @param rowLength   - the length of the row.
     * @param columnNames - list of column names which should be processed.
     * @return the array of objects storing the data of the row.
     */
    private Object[] processByteArrayWithData(long rowOffset, long rowLength, List<String> columnNames) {
        Object[] rowElements;
        if (columnNames != null) {
            rowElements = new Object[columnNames.size()];
        } else {
            rowElements = new Object[(int) sasFileProperties.getColumnsCount()];
        }
        byte[] source;
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
            if (columnNames == null) {
                rowElements[currentColumnIndex] = processElement(source, offset, currentColumnIndex);
            } else {
                String name = columns.get(currentColumnIndex).getName();
                if (columnNames.contains(name)) {
                    rowElements[columnNames.indexOf(name)] = processElement(source, offset, currentColumnIndex);
                }
            }
        }

        return rowElements;
    }

    /**
     * The function to process element of row.
     *
     * @param source             an array of bytes containing required data.
     * @param offset             the offset in source of required data.
     * @param currentColumnIndex index of the current element.
     * @return object storing the data of the element.
     */
    private Object processElement(byte[] source, int offset, int currentColumnIndex) {
        byte[] temp;
        int length = columnsDataLength.get(currentColumnIndex);
        if (columns.get(currentColumnIndex).getType() == Number.class) {
            temp = Arrays.copyOfRange(source, offset + (int) (long) columnsDataOffset.get(currentColumnIndex),
                    offset + (int) (long) columnsDataOffset.get(currentColumnIndex) + length);
            if (columnsDataLength.get(currentColumnIndex) <= 2) {
                return bytesToShort(temp);
            } else {
                if (columns.get(currentColumnIndex).getFormat().getName().isEmpty()) {
                    return convertByteArrayToNumber(temp);
                } else {
                    ColumnFormat columnFormat = columns.get(currentColumnIndex).getFormat();
                    String sasDateFormat = columnFormat.getName();
                    if (SasTemporalFormatter.isDateTimeFormat(sasDateFormat)) {
                        return bytesToDateTime(temp, outputDateType, columnFormat);
                    } else if (SasTemporalFormatter.isDateFormat(sasDateFormat)) {
                        return bytesToDate(temp, outputDateType, columnFormat);
                    } else if (SasTemporalFormatter.isTimeFormat(sasDateFormat)) {
                        return bytesToTime(temp, outputDateType, columnFormat);
                    } else {
                        return convertByteArrayToNumber(temp);
                    }
                }
            }
        } else {
            byte[] bytes = trimBytesArray(source,
                    offset + columnsDataOffset.get(currentColumnIndex).intValue(), length);
            if (byteOutput) {
                return bytes;
            } else {
                try {
                    return (bytes == null ? null : bytesToString(bytes));
                } catch (UnsupportedEncodingException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return null;
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
        List<byte[]> vars = new ArrayList<>();
        if (cachedPage == null) {
            for (int i = 0; i < offset.length; i++) {
                byte[] temp = new byte[length[i]];
                skipBytes(offset[i] - currentFilePosition);
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
                if (cachedPage.length < offset[i]) {
                    throw new IOException(EMPTY_INPUT_STREAM);
                }
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
        double resultDouble = bytesToDouble(mass);

        if (Double.isNaN(resultDouble) || (resultDouble < NAN_EPSILON && resultDouble > 0)) {
            return null;
        }

        long resultLong = Math.round(resultDouble);
        if (Math.abs(resultDouble - resultLong) >= EPSILON) {
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
     * @throws UnsupportedEncodingException when unknown encoding.
     */
    private String bytesToString(byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, encoding);
    }

    /**
     * The function to convert a sub-range of an array of bytes into a string.
     *
     * @param bytes  a string represented by an array of bytes.
     * @param offset the initial offset
     * @param length the length
     * @return the conversion result string.
     * @throws UnsupportedEncodingException    when unknown encoding.
     * @throws StringIndexOutOfBoundsException when invalid offset and/or length.
     */
    private String bytesToString(byte[] bytes, int offset, int length)
            throws UnsupportedEncodingException, StringIndexOutOfBoundsException {
        return new String(bytes, offset, length, encoding);
    }

    /**
     * The function to convert an array of bytes that stores the number of seconds
     * elapsed from 01/01/1960 into a variable of the {@link Date} type.
     *
     * @param bytes an array of bytes that stores the type.
     * @return a variable of the {@link Date} type.
     */
    private Date bytesToDateTime(byte[] bytes) {
        double doubleSeconds = bytesToDouble(bytes);
        if (Double.isNaN(doubleSeconds)) {
            return null;
        } else {
            return sasTemporalFormatter.formatSasSecondsAsJavaDate(doubleSeconds);
        }
    }

    /**
     * The function to convert an array of bytes that stores the number of seconds
     * elapsed from 01/01/1960 into the date represented according to outputDateType.
     *
     * @param bytes          an array of bytes that stores the type.
     * @param outputDateType type of the date formatting
     * @param columnFormat   SAS date format
     * @return datetime representation
     */
    private Object bytesToDateTime(byte[] bytes, OutputDateType outputDateType, ColumnFormat columnFormat) {
        double doubleSeconds = bytesToDouble(bytes);
        return sasTemporalFormatter.formatSasDateTime(doubleSeconds, outputDateType,
                columnFormat.getName(), columnFormat.getWidth(), columnFormat.getPrecision());
    }

    /**
     * The function to convert an array of bytes that stores the number of seconds
     * since midnight into the date represented according to outputDateType.
     *
     * @param bytes          an array of bytes that stores the type.
     * @param outputDateType type of the date formatting
     * @param columnFormat   SAS date format
     * @return time representation
     */
    private Object bytesToTime(byte[] bytes, OutputDateType outputDateType, ColumnFormat columnFormat) {
        double doubleSeconds = bytesToDouble(bytes);
        return sasTemporalFormatter.formatSasTime(doubleSeconds, outputDateType,
                columnFormat.getName(), columnFormat.getWidth(), columnFormat.getPrecision());
    }


    /**
     * The function to convert an array of bytes that stores the number of days
     * elapsed from 01/01/1960  into the date represented according to outputDateType.
     *
     * @param bytes          the array of bytes that stores the number of days from 01/01/1960.
     * @param outputDateType type of the date formatting
     * @param columnFormat   SAS date format
     * @return date representation
     */
    private Object bytesToDate(byte[] bytes, OutputDateType outputDateType, ColumnFormat columnFormat) {
        double doubleDays = bytesToDouble(bytes);
        return sasTemporalFormatter.formatSasDate(doubleDays, outputDateType,
                columnFormat.getName(), columnFormat.getWidth(), columnFormat.getPrecision());
    }

    /**
     * The function to convert an array of bytes into a double number.
     *
     * @param bytes a double number represented by an array of bytes.
     * @return a number of the double type that is the conversion result.
     */
    private double bytesToDouble(byte[] bytes) {
        ByteBuffer original = byteArrayToByteBuffer(bytes);

        if (bytes.length < BYTES_IN_DOUBLE) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(BYTES_IN_DOUBLE);
            if (sasFileProperties.getEndianness() == 1) {
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
     *
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
    public SasFileProperties getSasFileProperties() {
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
         *
         * @param subheaderOffset offset in bytes from the beginning of subheader.
         * @param subheaderLength length of subheader in bytes.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        void processSubheader(long subheaderOffset, long subheaderLength) throws IOException;
    }

    /**
     * The interface that is implemented by classes that process data subheader.
     */
    private interface ProcessingDataSubheader extends ProcessingSubheader {
        /**
         * Method which should be overwritten in implementing this interface classes.
         *
         * @param subheaderOffset offset in bytes from the beginning of subheader.
         * @param subheaderLength length of subheader in bytes.
         * @param columnNames     list of column names which should be processed.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        void processSubheader(long subheaderOffset, long subheaderLength, List<String> columnNames) throws IOException;
    }

    /**
     * SasFileParser builder class made using builder pattern.
     */
    public static class Builder {

        /**
         * Empty private constructor to prevent instantiation without the {@link SasFileParser#sasFileStream} variable.
         */
        private Builder() {

        }

        /**
         * Builder variable for {@link SasFileParser#sasFileStream} variable.
         */
        private InputStream sasFileStream;

        /**
         * Builder variable for {@link SasFileParser#encoding} variable.
         */
        private String encoding;

        /**
         * Default value for {@link SasFileParser#outputDateType} variable.
         */
        private OutputDateType outputDateType = OutputDateType.JAVA_DATE_LEGACY;

        /**
         * Default value for {@link SasFileParser#byteOutput} variable.
         */
        private Boolean byteOutput = false;

        /**
         * The constructor that specifies builders sasFileStream variable.
         *
         * @param sasFileStream value for {@link SasFileParser#sasFileStream} variable.
         */
        public Builder(InputStream sasFileStream) {
            this.sasFileStream = sasFileStream;
        }

        /**
         * The function to specify builders encoding variable.
         *
         * @param val value to be set.
         * @return result builder.
         */
        public Builder encoding(String val) {
            encoding = val;
            return this;
        }

        /**
         * Sets the specified type of the output date format.
         *
         * @param val value to be set.
         * @return result builder.
         */
        public Builder outputDateType(OutputDateType val) {
            if (val != null) {
                outputDateType = val;
            }
            return this;
        }

        /**
         * The function to specify builders byteOutput variable.
         *
         * @param val value to be set.
         * @return result builder.
         */
        public Builder byteOutput(Boolean val) {
            byteOutput = val;
            return this;
        }

        /**
         * The function to create variable of SasFileParser class using current builder.
         *
         * @return newly built SasFileParser
         */
        public SasFileParser build() {
            return new SasFileParser(this);
        }
    }

    /**
     * The class to store subheaders pointers that contain information about the offset, length, type
     * and compression of subheaders (see {@link SasFileConstants#TRUNCATED_SUBHEADER_ID},
     * {@link SasFileConstants#COMPRESSED_SUBHEADER_ID}, {@link SasFileConstants#COMPRESSED_SUBHEADER_TYPE}
     * for details).
     */
    static class SubheaderPointer {
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
     * {@link PageType#PAGE_TYPE_MIX} type. The results are stored in {@link SasFileProperties#rowLength},
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
            int intOrLongLength = sasFileProperties.isU64() ? BYTES_IN_LONG : BYTES_IN_INT;
            Long[] offset = {subheaderOffset + ROW_LENGTH_OFFSET_MULTIPLIER * intOrLongLength,
                    subheaderOffset + ROW_COUNT_OFFSET_MULTIPLIER * intOrLongLength,
                    subheaderOffset + ROW_COUNT_ON_MIX_PAGE_OFFSET_MULTIPLIER * intOrLongLength,
                    subheaderOffset + FILE_FORMAT_OFFSET_OFFSET + 82 * intOrLongLength,
                    subheaderOffset + FILE_FORMAT_LENGTH_OFFSET + 82 * intOrLongLength,
                    subheaderOffset + DELETED_ROW_COUNT_OFFSET_MULTIPLIER * intOrLongLength,
                    subheaderOffset + COMPRESSION_METHOD_OFFSET + 82 * intOrLongLength,
                    subheaderOffset + COMPRESSION_METHOD_LENGTH_OFFSET + 82 * intOrLongLength,
            };
            Integer[] length = {intOrLongLength, intOrLongLength, intOrLongLength,
                    FILE_FORMAT_OFFSET_LENGTH, FILE_FORMAT_LENGTH_LENGTH,
                    intOrLongLength,
                    COMPRESSION_METHOD_OFFSET_LENGTH, COMPRESSION_METHOD_LENGTH_LENGTH};
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

            fileLabelOffset = bytesToShort(vars.get(3));
            fileLabelLength = bytesToShort(vars.get(4));

            if (sasFileProperties.getDeletedRowCount() == 0) {
                sasFileProperties.setDeletedRowCount(bytesToLong(vars.get(5)));
            }

            compressionMethodOffset = bytesToShort(vars.get(6));
            compressionMethodLength = bytesToShort(vars.get(7));
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
            int intOrLongLength = sasFileProperties.isU64() ? BYTES_IN_LONG : BYTES_IN_INT;
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
    static class SubheaderCountsSubheader implements ProcessingSubheader {
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
     * compression information. The results are stored in {@link SasFileParser#columnsNamesBytes} and
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
            int intOrLongLength = sasFileProperties.isU64() ? BYTES_IN_LONG : BYTES_IN_INT;
            int textBlockSize;

            Long[] offset = {subheaderOffset + intOrLongLength};
            Integer[] length = {TEXT_BLOCK_SIZE_LENGTH};
            List<byte[]> vars = getBytesFromFile(offset, length);
            textBlockSize = byteArrayToByteBuffer(vars.get(0)).getShort();

            offset[0] = subheaderOffset + intOrLongLength;
            length[0] = textBlockSize;
            vars = getBytesFromFile(offset, length);

            columnsNamesBytes.add(vars.get(0));
            if (columnsNamesBytes.size() == 1) {
                byte[] columnName = columnsNamesBytes.get(0);
                String compressionMethod = bytesToString(columnName, compressionMethodOffset, compressionMethodLength);
                if (matchCompressionMethod(compressionMethod)) {
                    sasFileProperties.setCompressionMethod(compressionMethod);
                }
                sasFileProperties.setFileLabel(bytesToString(columnName, fileLabelOffset, fileLabelLength));
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
            int intOrLongLength = sasFileProperties.isU64() ? BYTES_IN_LONG : BYTES_IN_INT;
            long columnNamePointersCount = (subheaderLength - 2 * intOrLongLength - 12) / 8;
            int i;
            for (i = 0; i < columnNamePointersCount; i++) {
                Long[] offset = {subheaderOffset + intOrLongLength + COLUMN_NAME_POINTER_LENGTH * (i + 1)
                        + COLUMN_NAME_TEXT_SUBHEADER_OFFSET, subheaderOffset + intOrLongLength
                        + COLUMN_NAME_POINTER_LENGTH * (i + 1) + COLUMN_NAME_OFFSET_OFFSET, subheaderOffset
                        + intOrLongLength + COLUMN_NAME_POINTER_LENGTH * (i + 1) + COLUMN_NAME_LENGTH_OFFSET};
                Integer[] length = {COLUMN_NAME_TEXT_SUBHEADER_LENGTH, COLUMN_NAME_OFFSET_LENGTH,
                        COLUMN_NAME_LENGTH_LENGTH};
                List<byte[]> vars = getBytesFromFile(offset, length);

                int textSubheaderIndex = bytesToShort(vars.get(0));
                int columnNameOffset = bytesToShort(vars.get(1));
                int columnNameLength = bytesToShort(vars.get(2));
                if (textSubheaderIndex < columnsNamesBytes.size()) {
                    columnsNamesList.add(bytesToString(columnsNamesBytes.get(textSubheaderIndex),
                            columnNameOffset, columnNameLength).intern());
                } else {
                    columnsNamesList.add(new String(new char[columnNameLength]));
                    columnMissingInfoList.add(new ColumnMissingInfo(i, textSubheaderIndex, columnNameOffset,
                            columnNameLength, ColumnMissingInfo.MissingInfoType.NAME));
                }
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
            int intOrLongLength = sasFileProperties.isU64() ? BYTES_IN_LONG : BYTES_IN_INT;
            long columnAttributesVectorsCount = (subheaderLength - 2 * intOrLongLength - 12) / (intOrLongLength + 8);
            for (int i = 0; i < columnAttributesVectorsCount; i++) {
                Long[] offset = {subheaderOffset + intOrLongLength + COLUMN_DATA_OFFSET_OFFSET + i
                        * (intOrLongLength + 8), subheaderOffset + 2 * intOrLongLength + COLUMN_DATA_LENGTH_OFFSET + i
                        * (intOrLongLength + 8), subheaderOffset + 2 * intOrLongLength + COLUMN_TYPE_OFFSET + i
                        * (intOrLongLength + 8)};
                Integer[] length = {intOrLongLength, COLUMN_DATA_LENGTH_LENGTH, COLUMN_TYPE_LENGTH};
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
            int intOrLongLength = sasFileProperties.isU64() ? BYTES_IN_LONG : BYTES_IN_INT;
            Long[] offset = {subheaderOffset + COLUMN_FORMAT_WIDTH_OFFSET + 3 * intOrLongLength,
                    subheaderOffset + COLUMN_FORMAT_PRECISION_OFFSET + 3 * intOrLongLength,
                    subheaderOffset + COLUMN_FORMAT_TEXT_SUBHEADER_INDEX_OFFSET + 3 * intOrLongLength,
                    subheaderOffset + COLUMN_FORMAT_OFFSET_OFFSET + 3 * intOrLongLength,
                    subheaderOffset + COLUMN_FORMAT_LENGTH_OFFSET + 3 * intOrLongLength,
                    subheaderOffset + COLUMN_LABEL_TEXT_SUBHEADER_INDEX_OFFSET + 3 * intOrLongLength,
                    subheaderOffset + COLUMN_LABEL_OFFSET_OFFSET + 3 * intOrLongLength,
                    subheaderOffset + COLUMN_LABEL_LENGTH_OFFSET + 3 * intOrLongLength};
            Integer[] length = {COLUMN_FORMAT_WIDTH_OFFSET_LENGTH, COLUMN_FORMAT_PRECISION_OFFSET_LENGTH,
                    COLUMN_FORMAT_TEXT_SUBHEADER_INDEX_LENGTH, COLUMN_FORMAT_OFFSET_LENGTH, COLUMN_FORMAT_LENGTH_LENGTH,
                    COLUMN_LABEL_TEXT_SUBHEADER_INDEX_LENGTH, COLUMN_LABEL_OFFSET_LENGTH, COLUMN_LABEL_LENGTH_LENGTH};
            List<byte[]> vars = getBytesFromFile(offset, length);

            int columnFormatWidth = bytesToShort(vars.get(0));
            int columnFormatPrecision = bytesToShort(vars.get(1));
            int textSubheaderIndexForFormat = bytesToShort(vars.get(2));
            int columnFormatOffset = bytesToShort(vars.get(3));
            int columnFormatLength = bytesToShort(vars.get(4));
            int textSubheaderIndexForLabel = bytesToShort(vars.get(5));
            int columnLabelOffset = bytesToShort(vars.get(6));
            int columnLabelLength = bytesToShort(vars.get(7));
            String columnLabel = "";
            String columnFormatName = "";
            if (textSubheaderIndexForLabel < columnsNamesBytes.size()) {
                columnLabel = bytesToString(columnsNamesBytes.get(textSubheaderIndexForLabel),
                        columnLabelOffset, columnLabelLength).intern();
            } else {
                columnMissingInfoList.add(new ColumnMissingInfo(columns.size(), textSubheaderIndexForLabel,
                        columnLabelOffset, columnLabelLength, ColumnMissingInfo.MissingInfoType.LABEL));
            }
            if (textSubheaderIndexForFormat < columnsNamesBytes.size()) {
                columnFormatName = bytesToString(columnsNamesBytes.get(textSubheaderIndexForFormat),
                        columnFormatOffset, columnFormatLength).intern();
            } else {
                columnMissingInfoList.add(new ColumnMissingInfo(columns.size(), textSubheaderIndexForFormat,
                        columnFormatOffset, columnFormatLength, ColumnMissingInfo.MissingInfoType.FORMAT));
            }
            LOGGER.debug(COLUMN_FORMAT, columnFormatName);
            ColumnFormat columnFormat = new ColumnFormat(columnFormatName, columnFormatWidth, columnFormatPrecision);
            columns.add(new Column(currentColumnNumber + 1, columnsNamesList.get(columns.size()),
                    columnLabel, columnFormat, columnsTypesList.get(columns.size()),
                    columnsDataLength.get(currentColumnNumber++)));
        }
    }

    /**
     * The class to process subheaders of the ColumnListSubheader type that do not store any information relevant
     * to the current tasks.
     */
    static class ColumnListSubheader implements ProcessingSubheader {
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
    class DataSubheader implements ProcessingDataSubheader {
        /**
         * The method to read compressed or uncompressed data from the subheader. The results are stored as a row
         * in {@link SasFileParser#currentRow}. The {@link SasFileParser#processByteArrayWithData(long, long, List)}
         * function converts the array of bytes into a list of objects.
         *
         * @param subheaderOffset the offset at which the subheader is located.
         * @param subheaderLength the subheader length.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        @Override
        public void processSubheader(long subheaderOffset, long subheaderLength) throws IOException {
            currentRow = processByteArrayWithData(subheaderOffset, subheaderLength, null);
        }

        /**
         * The method to read compressed or uncompressed data from the subheader. The results are stored as a row
         * in {@link SasFileParser#currentRow}. The {@link SasFileParser#processByteArrayWithData(long, long, List)}
         * function converts the array of bytes into a list of objects.
         *
         * @param subheaderOffset the offset at which the subheader is located.
         * @param subheaderLength the subheader length.
         * @throws IOException if reading from the {@link SasFileParser#sasFileStream} stream is impossible.
         */
        @Override
        public void processSubheader(long subheaderOffset, long subheaderLength,
                                     List<String> columnNames) throws IOException {
            currentRow = processByteArrayWithData(subheaderOffset, subheaderLength, columnNames);
        }
    }
}
