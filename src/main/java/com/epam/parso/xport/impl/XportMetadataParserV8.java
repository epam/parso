package com.epam.parso.xport.impl;

import com.epam.parso.common.BytesHelper;
import com.epam.parso.xport.XportDatasetProperties;
import com.epam.parso.xport.XportFileProperties;
import com.epam.parso.xport.XportVersion;
import lombok.SneakyThrows;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.epam.parso.common.BytesHelper.bytesToString;
import static com.epam.parso.xport.impl.XportFileConstants.RECORD_LENGTH;

// todo test this implementation with real XPORT V8 file
/**
 * Implementation of {@link XportMetadataParser} for XPORT version 5.
 */
class XportMetadataParserV8 extends XportMetadataParser {

    // todo check this line with a real file, some blanks may be missing.
    /**
     * First header constant. First record in the file.
     */
    private static final String FIRST_HEADER = "HEADER RECORD*******LIBV8 HEADER RECORD!!!!!!!"
        + "000000000000000000000000000000    ";

    // todo check this line with a real file, some blanks may be missing.
    /**
     * Observation header constant. Observation header marks end of metadata and start of data (observations).
     */
    private static final String OBSERVATION_HEADER = "HEADER RECORD*******OBSV8   HEADER "
        + "RECORD!!!!!!!000000000000000000000000000000  ";

    /**
     * Both of these occur for every member in the transport file.
     * HEADER RECORD*******MEMBV8 HEADER RECORD!!!!!!!000000000000000001600000000140
     * HEADER RECORD*******DSCPTV8 HEADER RECORD!!!!!!!000000000000000000000000000000
     * Note the 0140 that appears in the member header record above. That value is the size of
     * the variable descriptor (NAMESTR) record that is described later in this document.
     * <p>
     * Member header data:
     * aaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbccccccccddddddddeeeeeeeeffffffffffffffff
     * where aaaaaaaa is 'SAS ', bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb is the data set name,
     * cccccccc is SASDATA (if a SAS data set is being created), dddddddd is the version of
     * the SAS System under which the file was created, and eeeeeeee is the operating system
     * name. ffffffffffffffff is the datetime created, formatted as in previous headers. Consider
     * this C structure:
     * struct REAL_HEADER {
     * char sas_symbol[8];
     * char sas_dsname[32];
     * char sasdata[8];
     * char sasver[8];
     * char sas_osname[8];
     * char sas_create[16];
     * };
     * The second header record is
     * ddMMMyy:hh:mm:ss aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbb
     * where the datetime modified appears using DATETIME16. format, followed by blanks
     * up to column 33, where the a's above correspond to a blank-padded data set label, and
     * bbbbbbbb is the blank-padded data set type. Note that data set labels can be up to 256
     * characters as of Version 8 of the SAS System, but only up to the first 40 characters are
     * stored in the second header record. Note also that only a 2-digit year appears in the
     * datetime modified value. If any program needs to read in this 2-digit year, be prepared to
     * deal with dates in the 1900s or the 2000s.
     * Consider the following C structure:
     * TS-140_2 2
     * struct SECOND_HEADER {
     * char dtmod_day[2];
     * char dtmod_month[3];
     * char dtmod_year[2];
     * char dtmod_colon1[1];
     * char dtmod_hour[2];
     * char dtmod_colon2[1];
     * char dtmod_minute[2];
     * char dtmod_colon2[1];
     * char dtmod_second[2];
     * char padding[16];
     * char dslabel[40];
     * char dstype[8];
     * };
     * <p>
     * Namestr header record:
     * One for each member.
     * HEADER RECORD*******NAMSTV8 HEADER RECORD!!!!!!!000000xxxxxx000000000000000000
     */
    // todo check this regexp with a real file, some blanks may be missing
    /**
     * Member (dataset) header regexp pattern.
     */
    private static final Pattern MEMBER_HEADER_PATTERN = Pattern.compile(
//       Header line 1
        "HEADER RECORD\\*{7}MEMBV8 HEADER RECORD!{7}0{17}160{7}(?<descriptorSize>.{4})  "
//       Header line 2
            + "HEADER RECORD\\*{7}DSCPTV8 HEADER RECORD!{7}0{30} {2}"
//       Header line 3
            + "SAS {5}(?<name>.{32})SASDATA (?<version>.{8})(?<os>.{8})(?<created>.{16})"
//       Header line 4
            + "(?<modified>.{16}) {16}(?<label>.{40})(?<type>    DATA|    VIEW| {8})"
//       Namestr header
            + "HEADER RECORD\\*{7}NAMSTV8 HEADER RECORD!{7}0{6}(?<variableCount>.{6})0{18} {2}");

    // todo check this regexp with a real file, some blanks may be missing.
    /**
     * If you have any labels that exceed 40 characters, they can be placed in this section. The
     * label records section starts with this header:
     * HEADER RECORD*******LABELV8 HEADER RECORD!!!!!!!nnnnn
     * where nnnnn is the number of variables for which long labels will be defined
     * <p>
     * Supposing that the label header length is 80 bytes, and label records will follow afterwards
     */
    private static final Pattern LABEL_HEADER_PATTERN = Pattern.compile("HEADER RECORD\\*{7}LABELV8 HEADER "
        + "RECORD!{7}(?<labelCount>.{5}) {27}");

    // todo check this regexp with a real file, some blanks may be missing.
    /**
     * If you have any format or informat names that exceed 8 characters, regardless of the
     * label length, a different form of label record header is used:
     * HEADER RECORD*******LABELV9 HEADER RECORD!!!!!!!nnnnn
     * where nnnnn is the number of variables for which long format names and any labels will be defined.
     *
     * Supposing that the label header length is 80 bytes, and label records will follow afterwards
     */
    private static final Pattern LABEL_LONG_FORMAT_HEADER_PATTERN = Pattern.compile("HEADER RECORD\\*{7}LABELV9 "
        + "HEADER RECORD!{7}(?<labelCount>.{5}) {27}");

    /**
     * Constructor to create metadata parser for XPORT version 5.
     * @param xportFile source file.
     * @param xportFileProperties properties to populate.
     */
    XportMetadataParserV8(File xportFile, XportFileProperties xportFileProperties) {
        super(xportFile, xportFileProperties);
    }

    @Override
    protected XportVersion getVersion() {
        return XportVersion.VERSION_8;
    }

    @Override
    protected Pattern getMemberHeaderPattern() {
        return MEMBER_HEADER_PATTERN;
    }

    @Override
    boolean isFirstHeaderValid(String firstHeader) {
        return FIRST_HEADER.equals(firstHeader);
    }

    @Override
    boolean isObservationHeaderValid(String observationHeader) {
        return OBSERVATION_HEADER.equals(observationHeader);
    }

    @SneakyThrows
    @Override
    protected void populateLabelHeaders(RandomAccessFile randomAccessFile,
                                        XportDatasetProperties datasetProperties) {
        byte[] labelHeader = new byte[RECORD_LENGTH];
        randomAccessFile.read(labelHeader);
        String labelHeaderStr = bytesToString(labelHeader);
        Matcher matcher = LABEL_HEADER_PATTERN.matcher(labelHeaderStr);
        int offset = 0;
        if (matcher.matches()) {
            offset = labelHeader.length + populateLabels(randomAccessFile, datasetProperties, matcher);
        } else {
            matcher = LABEL_LONG_FORMAT_HEADER_PATTERN.matcher(labelHeaderStr);
            if (matcher.matches()) {
                offset = labelHeader.length + populateLabelsLongFormat(randomAccessFile, datasetProperties,
                matcher);
            }
        }
        datasetProperties.setDataOffset(datasetProperties.getDataOffset() + getBlockLength(offset));
    }

    /**
     * Read specified number of variable (column) labels from file and populate properties with this info.
     * @param randomAccessFile source file.
     * @param datasetProperties properties to populate.
     * @param matcher label header regexp matcher.
     * @return offset - length in bytes of label data.
     */
    @SneakyThrows
    private int populateLabels(RandomAccessFile randomAccessFile, XportDatasetProperties datasetProperties,
                               Matcher matcher) {
        int offset = 0;
        int labelCount = Integer.parseInt(matcher.group("labelCount"));
        for (int i = 0; i < labelCount; ++i) {
            short variableNumber = randomAccessFile.readShort();
            short variableNameLength = randomAccessFile.readShort();
            short variableLabelLength = randomAccessFile.readShort();
            byte[] labelData = new byte[variableNameLength + variableLabelLength];
            randomAccessFile.read(labelData);
            // what's the difference between this value and longName from NAMESTR?
            String variableName = BytesHelper.bytesToString(labelData, 0, variableNameLength);
            String variableLabel = BytesHelper.bytesToString(labelData, variableNameLength, variableLabelLength);

            datasetProperties.getVariableProperties().stream()
                .filter(p -> p.getVarnum() == variableNumber)
                .findFirst().ifPresent(variable -> variable.setLongLabel(variableLabel));

            offset += Short.BYTES * 3 + labelData.length;
        }
        return offset;
    }

    /**
     * If you have any format or informat names that exceed 8 characters, regardless of the
     * label length, a different form of label record header is used:
     * HEADER RECORD*******LABELV9 HEADER RECORD!!!!!!!nnnnn
     * where nnnnn is the number of variables for which long format names and any labels will be defined.
     *
     * Supposing that the label header length is 80 bytes, and label records will follow afterwards
     *
     * @param randomAccessFile source file.
     * @param datasetProperties properties to populate.
     * @param matcher label header regexp matcher.
     * @return offset - length in bytes of label data.
     */
    @SneakyThrows
    private int populateLabelsLongFormat(RandomAccessFile randomAccessFile, XportDatasetProperties datasetProperties,
                                         Matcher matcher) {
        int offset;
        offset = 0;
        int labelCount = Integer.parseInt(matcher.group("labelCount"));
        for (int i = 0; i < labelCount; ++i) {
            short variableNumber = randomAccessFile.readShort();
            short variableNameLength = randomAccessFile.readShort();
            short variableLabelLength = randomAccessFile.readShort();
            short formatDescriptionLength = randomAccessFile.readShort();
            short informatDescriptionLength = randomAccessFile.readShort();
            byte[] labelData = new byte[variableNameLength + variableLabelLength
                + formatDescriptionLength + informatDescriptionLength];
            randomAccessFile.read(labelData);
            // what's the difference between this value and longName from NAMESTR?
            String variableName = BytesHelper.bytesToString(labelData, 0, variableNameLength);
            String variableLabel =
                BytesHelper.bytesToString(labelData, variableNameLength, variableLabelLength);
            String formatDescription = BytesHelper.bytesToString(labelData,
                variableNameLength + variableLabelLength, formatDescriptionLength);
            String informatDescription = BytesHelper.bytesToString(labelData,
                variableNameLength + variableLabelLength + formatDescriptionLength,
                informatDescriptionLength);

            datasetProperties.getVariableProperties().stream()
                .filter(p -> p.getVarnum() == variableNumber)
                .findFirst().ifPresent(variable -> {
                variable.setLongLabel(variableLabel);
                variable.setFormatDescription(formatDescription);
                variable.setInputFormatDescription(informatDescription);
            });

            offset += Short.BYTES * 5 + labelData.length;
        }
        return offset;
    }
}
