package com.epam.parso.xport;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class contains info about XPORT dataset.
 */
@Data
@AllArgsConstructor
public class XportDataset {

    /**
     * Dataset metadata (including columns metadata).
     */
    private XportDatasetProperties metadata;

    /**
     * Dataset contents as a table, row by row.
     */
    private Object[][] data;
}
