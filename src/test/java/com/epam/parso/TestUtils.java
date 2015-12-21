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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

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
