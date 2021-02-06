package com.epam.parso.xport.impl;

import com.epam.parso.xport.XportDatasetProperties;
import com.epam.parso.xport.XportFileProperties;
import com.epam.parso.xport.XportVersion;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Pattern;

/**
 * Implementation of {@link XportMetadataParser} for XPORT version 5.
 */
class XportMetadataParserV5 extends XportMetadataParser {

    /**
     * First header constant. First record in the file.
     */
    private static final String FIRST_HEADER = "HEADER RECORD*******LIBRARY HEADER "
        + "RECORD!!!!!!!000000000000000000000000000000  ";

    /**
     * Observation header constant. Observation header marks end of metadata and start of data (observations).
     */
    private static final  String OBSERVATION_HEADER = "HEADER RECORD*******OBS     HEADER "
        + "RECORD!!!!!!!000000000000000000000000000000  ";

    /**
     * Member (dataset) header regexp pattern.
     */
    private static final Pattern MEMBER_HEADER_PATTERN = Pattern.compile(
//       Header line 1
        "HEADER RECORD\\*{7}MEMBER  HEADER RECORD!{7}0{17}160{8}(?<descriptorSize>140|136)  "
//       Header line 2
            + "HEADER RECORD\\*{7}DSCRPTR HEADER RECORD!{7}0{30} {2}"
//       Header line 3
            + "SAS {5}(?<name>.{8})SASDATA (?<version>.{8})(?<os>.{8}) {24}(?<created>.{16})"
//       Header line 4
            + "(?<modified>.{16}) {16}(?<label>.{40})(?<type>    DATA|    VIEW| {8})"
//       Namestr header
            + "HEADER RECORD\\*{7}NAMESTR HEADER RECORD!{7}0{6}(?<variableCount>.{4})0{20} {2}");

    /**
     * Constructor to create metadata parser for XPORT version 5.
     * @param xportFile source file.
     * @param xportFileProperties properties to populate.
     */
    XportMetadataParserV5(File xportFile, XportFileProperties xportFileProperties) {
        super(xportFile, xportFileProperties);
    }

    @Override
    protected XportVersion getVersion() {
        return XportVersion.VERSION_5;
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

    /**
     * Not relevant for XPORT V5, just skipping.
     */
    @Override
    protected void populateLabelHeaders(RandomAccessFile randomAccessFile,
                                        XportDatasetProperties datasetProperties) {

    }
}
