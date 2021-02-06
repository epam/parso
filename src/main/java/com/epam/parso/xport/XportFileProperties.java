/**
 * *************************************************************************
 * Copyright (C) 2015 EPAM
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * *************************************************************************
 */

package com.epam.parso.xport;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A class to store all the XPORT file metadata.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class XportFileProperties {

    /**
     * The name of the xpt character encoding.
     */
    private final String encoding = "ASCII";

    /**
     * The version of the SAS(r) System under which the file was created.
     */
    private String sasVersion;

    /**
     * The name of the operating system that creates the record.
     */
    private String sasOs;

    /**
     * The date and time created, formatted as
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
     * XPORT file may contain several datasets ("members" in terms of the XPORT documentation).
     */
    private List<XportDatasetProperties> datasetProperties = new ArrayList<>();

}
