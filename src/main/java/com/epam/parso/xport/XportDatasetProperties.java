package com.epam.parso.xport;

import static com.epam.parso.common.ParserMessageConstants.UNKNOWN_COLUMN_NAME;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.epam.parso.Column;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import lombok.Builder;
import lombok.Data;

/**
 * Class contains info about XPORT dataset (dataset metadata).
 */
@Data
@Builder
public class XportDatasetProperties {

    /**
     * The size of the variable descriptor (NAMESTR) record. On the VAX/VMS operating system, the
     * value will be 0136 instead of 0140. This means that the descriptor will be
     * only 136 bytes instead of 140.
     */
    private int namestrLength;

    /**
     * The data set name.
     */
    private String datasetName;

    /**
     * The data set label.
     * Note that data set labels can be up to 256
     * characters as of SAS 8, but only the first 40 characters are stored in the
     * second header record
     */
    private String datasetLabel;

    /**
     * The data set type.
     */
    private String datasetType;

    /**
     * The version of the SAS(r) System under which the file was created.
     */
    private String sasVersion;

    /**
     * The name of the operating system that creates the record.
     */
    private String sasOs;

    /**
     * The date and time dataset is created, formatted as
     * ddMMMyy:hh:mm:ss. Note that only a 2-digit year appears. If any program
     * needs to read in this 2-digit year, be prepared to deal with dates in the
     * 1900s or the 2000s.
     */
    private String dateCreated;

    /**
     * The date and time modified, formatted as
     * ddMMMyy:hh:mm:ss. Note that only a 2-digit year appears. If any program
     * needs to read in this 2-digit year, be prepared to deal with dates in the
     * 1900s or the 2000s.
     */
    private String dateModified;

    /**
     * Number of columns (eq. variables, eq. NAMESTR records) in the data set.
     */
    private int columnsCount;

    /**
     * Offset (in bytes) from the beginning of the file to the first data line.
     */
    private long dataOffset;

    /**
     * The length (in bytes) of a data (observation) record.
     */
    private int rowLength;

    /**
     * The number of rows in the dataset.
     */
    private long rowCount;

    /**
     * The index of the dataset (starting from 0).
     */
    private int datasetIndex;

    /**
     * List of variables (eq. columns, eq. NAMESTR records) properties
     */
    @Builder.Default
    private List<XportVariableProperties> variableProperties = new ArrayList<>();

    /**
     * Get columns info in format compatible with csv writer. Only basic info is returned.
     * @return list of basic column info
     */
    public List<Column> getColumns() {
        return variableProperties.stream().map(XportVariableProperties::toColumn).collect(toList());
    }

    /**
     * Get columns info in format compatible with csv writer according to the columnNames. Only basic info is returned.
     *
     * @param columnNames - list of column names which should be returned.
     * @return a list of columns.
     */
    public List<Column> getColumns(List<String> columnNames) {
        Map<String, XportVariableProperties> columnsByName = variableProperties.stream()
            .collect(toMap(XportVariableProperties::getName, Function.identity()));
        return columnNames.stream().map(name -> columnsByName.computeIfAbsent(name,
            key -> {
                throw new NoSuchElementException(UNKNOWN_COLUMN_NAME);
            }))
            .map(XportVariableProperties::toColumn).collect(toList());
    }
}
