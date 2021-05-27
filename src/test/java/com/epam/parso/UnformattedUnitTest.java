package com.epam.parso;

import static com.epam.parso.TestUtils.getSas7bdatFilesList;

import java.io.File;
import java.net.URL;
import java.util.List;
import org.junit.Test;

public class UnformattedUnitTest {

    private static final String FOLDER_NAME = "sas7bdat/unformatted";

    @Test
    public void testUnformatted() {
        URL resourcesPath = this.getClass().getClassLoader().getResource("");
        if (resourcesPath != null) {
            List<File> files = getSas7bdatFilesList(resourcesPath.getFile() + "//" + FOLDER_NAME);
            for (File currentFile : files) {
                SasFileReaderUnitTest sasFileReaderUnitTest = new SasFileReaderUnitTest();
                sasFileReaderUnitTest.setFileName(FOLDER_NAME + "//" + currentFile.getName());
                sasFileReaderUnitTest.testUnformatted();
            }
        }
    }

}
