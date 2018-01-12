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

    public static List<File> getSas7bdatFilesList(String fileOrFolderName) {
        List<File> filesList = new ArrayList<File>();
        try {
            File fileOrFolder = new File(fileOrFolderName);
            if (fileOrFolder.isFile()) {
                filesList.add(fileOrFolder);
            } else {
                if (fileOrFolder.isDirectory()) {
                    File[] files = fileOrFolder.listFiles();
                    if (files != null) {
                        for (File currentFile : files) {
                            if (currentFile.getName().toLowerCase().endsWith(".sas7bdat")) {
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
}
