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

import static org.assertj.core.api.Assertions.assertThat;

import au.com.bytecode.opencsv.CSVReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

    public static InputStream getResourceAsStream(String fileName) {
        return TestUtils.class.getClassLoader().getResourceAsStream(fileName);
    }

    @SneakyThrows
    public static File getResourceAsFile(String fileName) {
        URL resource = TestUtils.class.getClassLoader().getResource(fileName);
        return new File(resource.toURI());
    }

    public static List<File> getSas7bdatFilesList(String fileOrFolderName) {
        return getInputFilesList(fileOrFolderName, ".sas7bdat");
    }

    public static List<File> getXportFilesList(String fileOrFolderName) {
        return getInputFilesList(fileOrFolderName, ".xpt");
    }

    public static List<File> getInputFilesList(String fileOrFolderName, String extension) {
        List<File> filesList = new ArrayList<>();
        try {
            File fileOrFolder = new File(fileOrFolderName);
            if (fileOrFolder.isFile()) {
                filesList.add(fileOrFolder);
            } else {
                if (fileOrFolder.isDirectory()) {
                    File[] files = fileOrFolder.listFiles();
                    if (files != null) {
                        for (File currentFile : files) {
                            if (currentFile.getName().toLowerCase().endsWith(extension)) {
                                filesList.add(currentFile);
                            }
                        }
                    }
                } else {
                    logger.error("Wrong file name {}", fileOrFolderName);
                }
            }
        } catch (NullPointerException e) {
            logger.error(e.getMessage(), e);
        }
        return filesList;
    }

    public static void compareResultWithControl(CSVReader controlReader, Writer writer, int lineNumber,
                                          List<Column> columns) {
        lineNumber++;
        try (CSVReader resultReader = new CSVReader(new StringReader(writer.toString()))) {
            String[] controlLine;
            String[] resultLine;
            while ((resultLine = resultReader.readNext()) != null && (controlLine = controlReader.readNext()) != null) {
                assertThat(resultLine.length).isEqualTo(controlLine.length);
                for (int i = 0; i < controlLine.length && i < columns.size(); i++) {
                    assertThat("Element in line number " + lineNumber + " and column " + columns.get(i).getName() +
                        " number " + (i + 1) + " : " + resultLine[i]).isEqualTo("Element in line number " +
                        lineNumber + " and column " + columns.get(i).getName() + " number " + (i + 1) + " : " +
                        controlLine[i]);
                }
                lineNumber++;
            }
            assertThat(resultReader.readNext()).isNull();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void compareResultWithControl(CSVReader controlReader, Writer writer) {

        int lineNumber = 1;
        try (CSVReader resultReader = new CSVReader(new StringReader(writer.toString()))) {
            String[] controlLine;
            String[] resultLine;
            while ((resultLine = resultReader.readNext()) != null && (controlLine = controlReader.readNext()) != null) {
                assertThat(resultLine.length).isEqualTo(controlLine.length);
                for (int i = 0; i < controlLine.length; i++) {
                    assertThat("Element in line number " + lineNumber + " : " + resultLine[i])
                        .isEqualTo("Element in line number " + lineNumber + " : " + controlLine[i]);
                }
                lineNumber++;
            }
            assertThat(resultReader.readNext()).isNull();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
