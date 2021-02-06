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

package com.epam.parso;

import com.epam.parso.xport.XportFileReaderTest;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

import static com.epam.parso.TestUtils.getSas7bdatFilesList;
import static com.epam.parso.TestUtils.getXportFilesList;

public class CSVDataWriterUnitTest {
    private static final String FOLDER_NAME = "sas7bdat";
    private static final String XPORT_FOLDER_NAME = "xport/v5_6/input";

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

    @Test
    public void testXportData() throws Exception {
        URL resourcesPath = this.getClass().getClassLoader().getResource("");
        if (resourcesPath != null) {
            List<File> files = getXportFilesList(resourcesPath.getFile() + "//" + XPORT_FOLDER_NAME);
            for (File currentFile : files) {
                XportFileReaderTest xportFileReaderTest = new XportFileReaderTest();
                xportFileReaderTest.setFileName(XPORT_FOLDER_NAME + "//" + currentFile.getName());
                xportFileReaderTest.testReadDatasetRowByRow();
            }
        }
    }
}
