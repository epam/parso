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

package com.epam.parso;

import java.util.Date;

/**
 * A class to store all the sas7bdat file metadata.
 */
public class SasFileProperties {
    /**
     * The flag of the 64-bit version of SAS in which the sas7bdat file was created; false means the 32-bit version,
     * true means the 64-bit version.
     */
    private boolean u64;

    /**
     * Compression method used for the sas7bdat file.
     */
    private String compressionMethod;

    /**
     * The bytes sequence; 1 sets the little-endian sequence (Intel), 0 sets big-endian.
     */
    private int endianness;

    /**
     * The name of the sas7bdat character encoding.
     */
    private String encoding;

    /**
     * The name of the sas7bdat character encoding.
     */
    private String sessionEncoding;

    /**
     * The name of the sas7bdat file table .
     */
    private String name;

    /**
     * The type of the sas7bdat file.
     */
    private String fileType;

    /**
     * The label of the sas7bdat file.
     */
    private String fileLabel;

    /**
     * The date of the sas7bdat file creation.
     */
    private Date dateCreated;

    /**
     * The date of the last modification of the sas7bdat file.
     */
    private Date dateModified;

    /**
     * The version of SAS in which the sas7bdat was created.
     */
    private String sasRelease;

    /**
     * The version of the server on which the sas7bdat was created.
     */
    private String serverType;

    /**
     * The name of the OS in which the sas7bdat file was created.
     */
    private String osName;

    /**
     * The version of the OS in which the sas7bdat file was created.
     */
    private String osType;

    /**
     * The number of bytes the sas7bdat file metadata takes.
     */
    private int headerLength;

    /**
     * The number of bytes each page of the sas7bdat file takes.
     */
    private int pageLength;

    /**
     * The number of pages in the sas7bdat file.
     */
    private long pageCount;

    /**
     * The number of bytes every row of the table takes.
     */
    private long rowLength;

    /**
     * The number of rows in the table.
     */
    private long rowCount;

    /**
     * The number of rows in the table.
     */
    private long deletedRowCount;

    /**
     * The number of rows on the page containing both data and metadata.
     */
    private long mixPageRowCount;

    /**
     * The number of columns in the table.
     */
    private long columnsCount;

    /**
     * SasFileProperties constructor which create class variable.
     *
     */
    public SasFileProperties() {
    }

    /**
     * The function to get name.
     *
     * @return the name of the sas7bdat file table.
     */
    public String getName() {
        return name;
    }

    /**
     * The method to specify name.
     *
     * @param name the name of the sas7bdat file table.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The function to get fileType.
     *
     * @return the type of the sas7bdat file.
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * The method to specify fileType.
     *
     * @param fileType the type of the sas7bdat file.
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * The function to get fileLabel.
     *
     * @return the label of the sas7bdat file.
     */
    public String getFileLabel() {
        return fileLabel;
    }

    /**
     * The method to specify fileLabel.
     *
     * @param fileLabel the label of the sas7bdat file.
     */
    public void setFileLabel(String fileLabel) {
        this.fileLabel = fileLabel;
    }

    /**
     * The function to get dateCreated.
     *
     * @return the date of the sas7bdat creation.
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * The method to specify dateCreated.
     *
     * @param dateCreated the date of the sas7bdat file creation.
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * The function to get dateModified.
     *
     * @return the date of the last modification of the sas7bdat file.
     */
    public Date getDateModified() {
        return dateModified;
    }

    /**
     * The method to specify dateModified.
     *
     * @param dateModified the date of the last modification of the sas7bdat file.
     */
    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    /**
     * The function to get sasRelease.
     *
     * @return the version of SAS in which the sas7bdat file was created.
     */
    public String getSasRelease() {
        return sasRelease;
    }

    /**
     * The method to specify sasRelease.
     *
     * @param sasRelease the version of SAS in which the sas7bdat file was created.
     */
    public void setSasRelease(String sasRelease) {
        this.sasRelease = sasRelease;
    }

    /**
     * The function to get serverType.
     *
     * @return the version of the server on which the sas7bdat file was created.
     */
    public String getServerType() {
        return serverType;
    }

    /**
     * The method to specify serverType.
     *
     * @param serverType the version of the server on which the sas7bdat file was created.
     */
    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    /**
     * The function to get osName.
     *
     * @return the name of the OS in which the sas7bdat file was created.
     */
    public String getOsName() {
        return osName;
    }

    /**
     * The method to specify osName.
     *
     * @param osName the name of the OS in which the sas7bdat file was created.
     */
    public void setOsName(String osName) {
        this.osName = osName;
    }

    /**
     * The function to get osType.
     *
     * @return the version of the OS in which the sas7bdat file was created.
     */
    public String getOsType() {
        return osType;
    }

    /**
     * The method to specify osType.
     *
     * @param osType the version of the OS in which the sas7bdat file was created.
     */
    public void setOsType(String osType) {
        this.osType = osType;
    }

    /**
     * The function to get endianness.
     *
     * @return the sequence of bytes; 1 means the little-endian sequence (Intel), 0 means big-endian.
     */
    public int getEndianness() {
        return endianness;
    }

    /**
     * The method to specify endianness.
     *
     * @param endianness the sequence of bytes; 1 sets the little-endian sequence (Intel), 0 sets big-endian.
     */
    public void setEndianness(int endianness) {
        this.endianness = endianness;
    }

    /**
     * The function to get encoding.
     *
     * @return the character encoding in which the sas7bdat file was created.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * The method to specify encoding.
     *
     * @param encoding the character encoding in which the sas7bdat file was created.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * The function to get session encoding.
     *
     * @return the character encoding in which the sas7bdat file was created.
     */
    public String getSessionEncoding() {
        return sessionEncoding;
    }

    /**
     * The method to specify session encoding.
     *
     * @param sessionEncoding the character encoding in which the sas7bdat file was created.
     */
    public void setSessionEncoding(String sessionEncoding) {
        this.sessionEncoding = sessionEncoding;
    }

    /**
     * The function to get u64.
     *
     * @return the flag of the 64-bit version of SAS in which the sas7bdat file was created,
     * false means the 32-bit version, true means the 64-bit version.
     */
    public boolean isU64() {
        return u64;
    }

    /**
     * The method to specify u64.
     *
     * @param u64 the flag of the 64-bit version of SAS in which the sas7bdat file was created,
     *            false sets the 32-bit version, true sets the 64-bit version.
     */
    public void setU64(boolean u64) {
        this.u64 = u64;
    }

    /**
     * Getter for the compression method.
     *
     * @return The file-level compression method used for the sas7bdat file.
     */
    public String getCompressionMethod() {
        return compressionMethod;
    }

    /**
     * The method to specify compressed.
     *
     * @param compressionMethod the flag of CHAR compression, false means the sas7bdat file is not CHAR compressed,
     *                          true means the sas7bdat is CHAR compressed.
     */
    public void setCompressionMethod(String compressionMethod) {
        this.compressionMethod = compressionMethod;
    }

    /**
     * @return true if the file is compressed by any of the supported compression methods, false othewise.
     */
    public boolean isCompressed() {
        return compressionMethod != null;
    }

    /**
     * The function to get headerLength.
     *
     * @return the number of bytes the sas7bdat file metadata takes.
     */
    public int getHeaderLength() {
        return headerLength;
    }

    /**
     * The method to specify headerLength.
     *
     * @param headerLength the number of bytes the sas7bdat file metadata takes.
     */
    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    /**
     * The function to get pageLength.
     *
     * @return the number of bytes each page of the sas7bdat file takes.
     */
    public int getPageLength() {
        return pageLength;
    }

    /**
     * The method to specify pageLength.
     *
     * @param pageLength the number of bytes each page of the sas7bdat file takes.
     */
    public void setPageLength(int pageLength) {
        this.pageLength = pageLength;
    }

    /**
     * The function to get pageCount.
     *
     * @return the number of pages in the sas7bdat file.
     */
    public long getPageCount() {
        return pageCount;
    }

    /**
     * The method to specify pageCount.
     *
     * @param pageCount the number of pages in the sas7bdat file.
     */
    public void setPageCount(long pageCount) {
        this.pageCount = pageCount;
    }

    /**
     * The function to get rowLength.
     *
     * @return the number of bytes each row of the table takes.
     */
    public long getRowLength() {
        return rowLength;
    }

    /**
     * The method to specify rowLength.
     *
     * @param rowLength the number of bytes each row of the table takes.
     */
    public void setRowLength(long rowLength) {
        this.rowLength = rowLength;
    }

    /**
     * The function to get rowCount.
     *
     * @return the number of rows in the table.
     */
    public long getRowCount() {
        return rowCount;
    }

    /**
     * The method to specify rowCount.
     *
     * @param rowCount the number of rows in the table.
     */
    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * The function to get deletedRowCount.
     *
     * @return the number of deleted rows in the table.
     */
    public long getDeletedRowCount() {
        return deletedRowCount;
    }

    /**
     * The method to specify deletedRowCount.
     *
     * @param deletedRowCount the number of deleted rows in the table.
     */
    public void setDeletedRowCount(long deletedRowCount) {
        this.deletedRowCount = deletedRowCount;
    }

    /**
     * The function to get mixPageRowCount.
     *
     * @return the number of rows on the page containing both data and metadata.
     */
    public long getMixPageRowCount() {
        return mixPageRowCount;
    }

    /**
     * The method to specify mixPageRowCount.
     *
     * @param mixPageRowCount the number of rows on the page containing both data and metadata.
     */
    public void setMixPageRowCount(long mixPageRowCount) {
        this.mixPageRowCount = mixPageRowCount;
    }

    /**
     * The function to get columnsCount.
     *
     * @return the number of columns in the table.
     */
    public long getColumnsCount() {
        return columnsCount;
    }

    /**
     * The method to specify columnsCount.
     *
     * @param columnsCount the number of columns in the table.
     */
    public void setColumnsCount(long columnsCount) {
        this.columnsCount = columnsCount;
    }
}
