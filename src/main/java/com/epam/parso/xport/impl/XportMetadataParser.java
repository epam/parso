package com.epam.parso.xport.impl;

import com.epam.parso.xport.XportDatasetProperties;
import com.epam.parso.xport.XportFileProperties;
import com.epam.parso.xport.XportVariableProperties;
import com.epam.parso.xport.XportVersion;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.riversun.bigdoc.bin.BigFileSearcher;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.epam.parso.common.BytesHelper.bytesToString;
import static com.epam.parso.common.ParserMessageConstants.XPORT_FILE_NOT_VALID;
import static com.epam.parso.xport.impl.XportFileConstants.*;

/**
 * Basic class for XPORT metadata parsing. Metadata format differs for various XPORT versions. The differences are
 * handled in the subclasses.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class XportMetadataParser {

    /**
     * Source XPORT file to parse (used for datasets metadata processing since we don't know neither dataset count
     * not datasets size in advance).
     */
    private final File xportFile;

    /**
     * XPORT file properties to populate.
     */
    private final XportFileProperties xportFileProperties;

    /**
     * Factory method to instantiate metadata parser depending on XPORT version.
     * @param xportFile source file.
     * @param xportFileProperties properties to populate.
     * @param version XPORT file version.
     * @return instance of corresponding metadata parser subclass.
     */
    public static XportMetadataParser getInstance(File xportFile,
                                                  XportFileProperties xportFileProperties,
                                                  XportVersion version) {
        if (version == XportVersion.VERSION_8) {
            return new XportMetadataParserV8(xportFile, xportFileProperties);
        }
        return new XportMetadataParserV5(xportFile, xportFileProperties);
    }

    /**
     * The method that reads and parses metadata from the XPORT and puts the results in
     * {@link XportMetadataParser#xportFileProperties}.
     *
     * @return current offset of the input stream (in bytes). Required to update offset counter in the upstream reader.
     */
    @SneakyThrows
    public long populateMetadataFromXportFile() {

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(xportFile, "r")) {
            long position = processXportFileHeader(randomAccessFile);
            processMemberHeaders(randomAccessFile);
            return position;
        }
    }

    /**
     * The method to read and parse metadata from the XPORT file`s header in
     * {@link XportMetadataParser#xportFileProperties}.
     *
     * @param randomAccessFile source file
     * @throws IOException if reading from file is impossible.
     * @return current stream position which is at the end of the header whose length is stored at the
     * {@link XportFileConstants#HEADER_SIZE_OFFSET} offset.
     */
    private long processXportFileHeader(RandomAccessFile randomAccessFile) throws IOException {

        List<byte[]> headers = readRecords(randomAccessFile);

        String firstHeader = bytesToString(headers.get(0));
        validate(firstHeader, this::isFirstHeaderValid);

        Header firstRealHeader = new Header(bytesToString(headers.get(1)));
        validate(firstRealHeader, Header::isValid);

        if (!isFirstHeaderValid(firstHeader) || !firstRealHeader.isValid()) {
            throw new IOException(XPORT_FILE_NOT_VALID);
        }
        xportFileProperties.setSasVersion(firstRealHeader.getSasVersion());
        xportFileProperties.setSasOs(firstRealHeader.getSasOs());
        xportFileProperties.setDateCreated(firstRealHeader.getSasCreate());

        String dateModified = bytesToString(headers.get(2)).trim();

        xportFileProperties.setDateModified(dateModified);
        return HEADER_SIZE_OFFSET;
    }

    /**
     * General method to validate some part of XPORT file.
     * @param objectToValidate part of file to validate.
     * @param validationFunction function to validate particular file part.
     * @param <T> type of object to validate.
     */
    @SneakyThrows
    private <T> void validate(T objectToValidate, Predicate<T> validationFunction) {
        if (!validationFunction.test(objectToValidate)) {
            throw new IOException(XPORT_FILE_NOT_VALID);
        }
    }

    /**
     * Read header records to list of byte arrays. Each record corresponds to a byte array.
     * @param randomAccessFile source file.
     * @return list of byte arrays read.
     */
    private List<byte[]> readRecords(RandomAccessFile randomAccessFile) {

        return IntStream.range(0, HEADER_RECORDS_COUNT).mapToObj(i -> {
            byte[] temp = new byte[XportFileConstants.RECORD_LENGTH];
            try {
                randomAccessFile.read(temp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return temp;
        }).collect(Collectors.toList());
    }

    /**
     * Process members (datasets) meta information and populate {@link XportMetadataParser#xportFileProperties}.
     * There is no information in metadata about number of datasets in a file and about row count in each dataset,
     * so {@link BigFileSearcher} is used for preprocessing to find dataset start marker positions and avoid scanning
     * the whole file line by line.
     * @param randomAccessFile source file.
     */
    @SneakyThrows
    private void processMemberHeaders(RandomAccessFile randomAccessFile) {

        BigFileSearcher searcher = new BigFileSearcher();

        // byte offset from the beginning of the file to the start of each dataset
        List<Long> memberHeaderPositions = searcher.searchBigFile(xportFile,
                MEMBER_HEADER.getBytes(StandardCharsets.US_ASCII));

        for (int i = 0; i < memberHeaderPositions.size(); ++i) {
            Long position = memberHeaderPositions.get(i);
            randomAccessFile.seek(position);
            byte[] memberHeaders = new byte[MEMBER_HEADERS_OFFSET];
            randomAccessFile.read(memberHeaders, 0, MEMBER_HEADERS_OFFSET);

            String memberHeaderStr = bytesToString(memberHeaders);
            Matcher matcher = getMemberHeaderPattern().matcher(memberHeaderStr);
            if (matcher.matches()) {
                XportDatasetProperties datasetProperties = buildXportDatasetProperties(matcher);
                xportFileProperties.getDatasetProperties().add(datasetProperties);
                populateDatasetVariableProperties(randomAccessFile, position, datasetProperties);
                populateLabelHeaders(randomAccessFile, datasetProperties);
                readObservationHeader(randomAccessFile);
                populateDatasetRowCount(memberHeaderPositions, randomAccessFile, i, datasetProperties);
            } else {
                throw new IOException(XPORT_FILE_NOT_VALID);
            }
        }
    }

    /**
     * Populate dataset variable (column) properties.
     * @param randomAccessFile source file.
     * @param position offset from the beginning of the file to start reading (in bytes)
     * @param datasetProperties properties object to populate.
     */
    @SneakyThrows
    private void populateDatasetVariableProperties(RandomAccessFile randomAccessFile, Long position,
                                                   XportDatasetProperties datasetProperties) {
        int namestrLength = datasetProperties.getNamestrLength();
        int variablesCount = datasetProperties.getColumnsCount();

        int namestrBlockLength = getNamestrBlockLength(namestrLength, variablesCount);
        byte[] namestrs = new byte[namestrBlockLength];
        randomAccessFile.read(namestrs);

        for (int i = 0; i < variablesCount; ++i) {
            XportVariableProperties variableProperties = new XportVariableProperties(Arrays
                .copyOfRange(namestrs, i * namestrLength,
                    (i + 1) * namestrLength), getVersion());
            datasetProperties.getVariableProperties().add(variableProperties);
        }

        long dataOffset = position + MEMBER_HEADERS_OFFSET + namestrBlockLength + RECORD_LENGTH;
        datasetProperties.setDataOffset(dataOffset);

        int variableOffset = 0;
        for (int j = 0; j < datasetProperties.getVariableProperties().size(); ++j) {
            XportVariableProperties variableProperties = datasetProperties.getVariableProperties().get(j);
            variableProperties.setVariableOffset(variableOffset);
            variableOffset += variableProperties.getVariableLength();
        }

        datasetProperties.setRowLength(variableOffset);
    }

    /**
     * Read and validate observation header. Observation header just marks start of data records ("observations").
     * @param randomAccessFile source file.
     */
    @SneakyThrows
    private void readObservationHeader(RandomAccessFile randomAccessFile) {
        byte[] observationHeader = new byte[RECORD_LENGTH];
        randomAccessFile.read(observationHeader);
        String observationHeaderStr = bytesToString(observationHeader);
        validate(observationHeaderStr, this::isObservationHeaderValid);
    }

    /**
     * Build dataset properties from regexp matcher.
     * @param matcher dataset ("member") header regexp matcher.
     * @return dataset properties object.
     */
    private XportDatasetProperties buildXportDatasetProperties(Matcher matcher) {
        int descriptorSize = Integer.parseInt(matcher.group("descriptorSize"));
        String datasetName = matcher.group("name").trim();
        String sasVersion = matcher.group("version").trim();
        String sasOs = matcher.group("os").trim();
        String created = matcher.group("created");
        String modified = matcher.group("modified");
        String label = matcher.group("label").trim();
        String type = matcher.group("type").trim();
        int variableCount = Integer.parseInt(matcher.group("variableCount"));

        return XportDatasetProperties.builder()
            .datasetName(datasetName)
            .sasVersion(sasVersion)
            .sasOs(sasOs)
            .dateCreated(created)
            .dateModified(modified)
            .datasetLabel(label)
            .datasetType(type)
            .namestrLength(descriptorSize)
            .columnsCount(variableCount)
            .build();
    }

    /**
     * Populate row count for specified dataset. Row count is calculated based on the difference between
     *  dataset end position (calculated as a position of the first byte of the next dataset or file length for the
     * last dataset)
     * and
     *  start position of the data records in the specified dataset
     * devided by row length.
     * @param memberHeaderPositions list of offsets (in bytes) of dataset (member) header start positions.
     * @param randomAccessFile source file.
     * @param datasetIndex sequential index of the dataset in the file, starting from 0.
     * @param datasetProperties properties object to populate.
     */
    @SneakyThrows
    private void populateDatasetRowCount(List<Long> memberHeaderPositions, RandomAccessFile randomAccessFile,
                                         int datasetIndex, XportDatasetProperties datasetProperties) {
        long lastDatasetPosition;
        if (datasetIndex < memberHeaderPositions.size() - 1) {
            lastDatasetPosition = memberHeaderPositions.get(datasetIndex + 1);

        } else {
            lastDatasetPosition = randomAccessFile.length();
        }
        long rowCount = (lastDatasetPosition - datasetProperties.getDataOffset()) / datasetProperties.getRowLength();
        datasetProperties.setRowCount(rowCount);
        datasetProperties.setDatasetIndex(datasetIndex);
    }

    /**
     * Each namestr field is 'descriptorSize' bytes long, but the fields are streamed together
     * and broken in 80-byte pieces. If the last byte of the last namestr field
     * does not fall in the last byte of the 80-byte record, the record is padded
     * with ASCII blanks to 80 bytes.
     *
     * @param descriptorSize column length
     * @param variablesCount number of columns (variables)
     * @return total length of all numstr records for the dataset
     */
    private int getNamestrBlockLength(int descriptorSize, int variablesCount) {
        int namestrFieldsLength = descriptorSize * variablesCount;
        return getBlockLength(namestrFieldsLength);
    }

    /**
     * The fields are streamed together and broken in 80-byte pieces. If the last byte of the last data
     * does not fall in the last byte of the 80-byte record, the record is padded
     * with ASCII blanks to 80 bytes.
     *
     * @param dataLength data size in bytes
     * @return total length of the data in a block padded to RECORD_LENGTH size
     */
    int getBlockLength(double dataLength) {
        int numberOfRecords = (int) Math.ceil(dataLength / RECORD_LENGTH);
        return numberOfRecords * RECORD_LENGTH;
    }

    /**
     * Process label headers (if present) populate corresponding variable (column) properties.
     * @param randomAccessFile source file.
     * @param datasetProperties properties object to populate.
     */
    protected abstract void populateLabelHeaders(RandomAccessFile randomAccessFile,
                                                 XportDatasetProperties datasetProperties);

    /**
     * Validate first header.
     * @param firstHeader first header in XPORT file.
     * @return true if header is valid.
     */
    abstract boolean isFirstHeaderValid(String firstHeader);

    /**
     * Validate observation header.
     * @param observationHeader observation header in XPORT file.
     * @return true if header is valid.
     */
    abstract boolean isObservationHeaderValid(String observationHeader);

    /**
     * Get XPORT format version. Base field to distinguish metadata parser implementations.
     * @return XPORT format version.
     */
    protected abstract XportVersion getVersion();

    /**
     * Get dataset (member) header regexp pattern. Differs in various XPORT format versions, so need to be specified
     * in subclasses.
     * @return dataset (member) header regexp pattern for specific XPORT format version.
     */
    protected abstract Pattern getMemberHeaderPattern();

    /**
     * Class to handle XPORT general header data.
     */
    @Getter
    private static class Header {

        /**
         * SAS constant string.
         */
        private static final String SAS = "SAS     ";

        /**
         * SASLIB constant string.
         */
        private static final String SASLIB = "SASLIB  ";

        /**
         * Field contains SAS constant.
         */
        private final String sasSymbol1;

        /**
         * Field contains SAS constant.
         */
        private final String sasSymbol2;

        /**
         * Field contains SASLIB constant.
         */
        private final String sasLib;

        /**
         * The version of the SAS(r) System under which the file was created.
         */
        private final String sasVersion;

        /**
         * The name of the operating system that creates the record.
         */
        private final String sasOs;

        /**
         * Blank symbols.
         */
        private final String blanks;

        /**
         * The date and time created, formatted as
         * ddMMMyy:hh:mm:ss. Note that only a 2-digit year appears. If any program
         * needs to read in this 2-digit year, be prepared to deal with dates in the
         * 1900s or the 2000s.
         */
        private final String sasCreate;

        /**
         * Constructor that builds header instance from header string.
         * @param headerString source string.
         */
        Header(String headerString) {
            sasSymbol1 = headerString.substring(0, BYTES_IN_VARIABLE);
            sasSymbol2 = headerString.substring(BYTES_IN_VARIABLE, BYTES_IN_VARIABLE * 2);
            sasLib = headerString.substring(BYTES_IN_VARIABLE * 2, BYTES_IN_VARIABLE * 3);
            sasVersion = headerString.substring(BYTES_IN_VARIABLE * 3, BYTES_IN_VARIABLE * 4).trim();
            sasOs = headerString.substring(BYTES_IN_VARIABLE * 4, BYTES_IN_VARIABLE * 5).trim();
            blanks = headerString.substring(BYTES_IN_VARIABLE * 5, BYTES_IN_VARIABLE * 8);
            sasCreate = headerString.substring(BYTES_IN_VARIABLE * 8, BYTES_IN_VARIABLE * 8 + BYTES_IN_TIMESTAMP);
        }

        /**
         * Validates header.
         * @return true if the header is valid XPORT header, false otherwise.
         */
        boolean isValid() {
            return SAS.equals(sasSymbol1) && SAS.equals(sasSymbol2) && SASLIB.equals(sasLib)
                && blanks.codePoints().allMatch(Character::isSpaceChar);
        }
    }
}
