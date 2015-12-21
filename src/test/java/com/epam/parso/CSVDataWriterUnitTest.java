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

package com.epam.parso;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

import static com.epam.parso.TestUtils.getSas7bdatFilesList;

public class CSVDataWriterUnitTest {
    private static final String FOLDER_NAME = "sas7bdat";

    @Test
    public void testData() {
        URL resourcesPath = this.getClass().getClassLoader().getResource("");
        if (resourcesPath != null) {
            List<File> files = getSas7bdatFilesList(resourcesPath.getFile() + "//" + FOLDER_NAME);
            for (File currentFile : files) {
                SasFileReaderUnitTest sasFileReaderUnitTest = new SasFileReaderUnitTest();
                sasFileReaderUnitTest.setFileName(FOLDER_NAME + "//" + currentFile.getName());
                sasFileReaderUnitTest.testData();
            }
        }
    }


}
