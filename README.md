![Build Status](https://github.com/epam/parso/workflows/Parso%20CI/badge.svg?branch=master&event=push)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Version](https://img.shields.io/maven-central/v/com.epam/parso)

# Parso Java library
## Parso 2.0.14
***19 February 2021***

* Extended support for additional date/time formats ([#86][i86])
* Fixed incorrect date calculation for years > 4000 ([#81][i81])
* Fixed infinite loop on fuzzed file bug ([#58][i58])

[i58]: https://github.com/epam/parso/issues/58
[i81]: https://github.com/epam/parso/issues/81
[i86]: https://github.com/epam/parso/pull/86

## Parso 2.0.13
***17 December 2020***

* Integrated with github actions ([#66][i66])
* Fixed format and precision reading for 64 bit files ([#54][i54])
* Fixed encoding issues in compressed files ([#55][i55])
* Code improvements, switching to assertj from fest-assert ([#71][i71], [#72][i72])
* Fixed offset calculation for deleted records ([#77][i77])

[i54]: https://github.com/epam/parso/issues/54
[i55]: https://github.com/epam/parso/issues/55
[i66]: https://github.com/epam/parso/issues/66
[i71]: https://github.com/epam/parso/issues/71
[i72]: https://github.com/epam/parso/issues/72
[i77]: https://github.com/epam/parso/issues/77

## Parso 2.0.12
***19 August 2020***

* Added support for deleted rows ([#38][i38])
* Added more various time formatters and moved them to interface ([#48][i48])
* Made SASFileParser API public ([#51][i51])
* Added sanity check to page length ([#60][i60])

[i38]: https://github.com/epam/parso/issues/38
[i48]: https://github.com/epam/parso/issues/48
[i51]: https://github.com/epam/parso/issues/51
[i60]: https://github.com/epam/parso/issues/60

## Parso 2.0.11
***25 March 2019***

* Added ability to override file encoding ([#30][i30])
* Added ability to get file label ([#34][i34])
* Decimal separator used to convert percentage elements was fixed ([#35][i35])

[i30]: https://github.com/epam/parso/issues/30
[i34]: https://github.com/epam/parso/issues/34
[i35]: https://github.com/epam/parso/issues/35

## Parso 2.0.10
***28 September 2018***

* Fixed processing of columns formats and labels
* Fixed formatting of percentage values ([#28][i28])

[i28]: https://github.com/epam/parso/issues/28

## Parso 2.0.9
***26 April 2018***

* Added ability to read precision information ([#22][i22])
* Exposed formatting API for reuse ([#24][i24])
* Added ability to read only specified columns to improve performance
* Fixed bug [#26][i26]

[i22]: https://github.com/epam/parso/issues/22
[i24]: https://github.com/epam/parso/issues/24
[i26]: https://github.com/epam/parso/issues/26

## Parso 2.0.8
***12 January 2018***

* Changed license to ALv2

## Parso 2.0.7
***24 March 2017***

* Fixed handling of non-seekable binary channel.
* Added ability to set locale used for dates in csv file.

## Parso 2.0.6
***14 March 2017***

* Added handling of many additional date and datetime format patterns.

## Parso 2.0.5
***10 March 2017***

* Fixed handling of ISO8601 date format. Added new date pattern.

## Parso 2.0.4
***8 March 2017***

* Added processing column names, formats and labels on page type 'amd'.
* Added handling of ISO8601 date format.

## Parso 2.0.3
***15 December 2016***

* Added handling of empty or corrupted input stream.

## Parso 2.0.2
***24 October 2016***

* Character encoding support added
* The subheaders signatures list updated
* Correct double-byte character encoding processing added

## Parso 2.0.1
***28 September 2016***

* Double values processing corrected

## Parso 2.0
***28 March 2016***

* Binary compression algorithm improved
* Char compression algorithm improved
* Correct processing of numbers larger than long
* Codestyle improvements

## Parso 1.2.1
***13 February 2016***

* Data streams processing
* Converting SAS7BDAT datasets into CSV format
* Reading and converting metadata of SAS7BDAT datasets into CSV format
* Extracting all properties of SAS7BDAT files (creation date, modification date, etc.)
* Two ways to read the file data: in one go and row by row

## Overview
Parso is a lightweight Java library that is designed to read SAS7BDAT datasets. The Parso interfaces are analogous to
 those that belong to libraries designed to read table-storing files, for example, CSVReader library. Despite its
 small size, the Parso library is the only full-featured open-source solution to process SAS7BDAT datasets
 (uncompressed, CHAR-compressed or BINARY-compressed). It is effective in processing clinical and statistical data,
 which is often stored in SAS7BDAT format. In addition, the Parso library allows users to convert data into CSV format.

## Why Select Parso as your SAS7BDAT File Reader?
* Stability: Successful processing in difficult cases (partially corrupt files, compression, etc.)
* High processing speed
* Easy-to-use API: You need a couple of lines of program code to convert your SAS7BDAT file into a model or CSV format
* JavaDoc for exposed API
* Algorithm descriptions enclosed
* Evolving project: EPAM continues to develop the library by adding new functionality
* On-going technical support: EPAM welcomes any comments and concerns about this product and will address them on a regular basis
* Possibility of integration with existing client projects
* Row-by-row reading of data allows you to optimally organize the storing of results

## Features
* Data streams processing: no need to save the files to process
* Converting SAS7BDAT datasets into CSV format
* Reading and converting metadata of SAS7BDAT datasets into CSV format
* Extracting all properties of SAS7BDAT files (creation date, modification date, etc.)
* Two ways to read the file data: in one go and row by row

## Support and Development
EPAM has wide experience in integration services and will help you integrate Parso into your project. We will price those services based on your individual needs.

## How to Use
If you use Maven, add the following dependency into the pom.xml file:

```xml
<dependency>
    <groupId>com.epam</groupId>
    <artifactId>parso</artifactId>
    <version>2.0.14</version>
</dependency>
```

Create a variable of the SasFileReader class and indicate your InputStream that contains the SAS7BDAT file as a parameter in the SasFileReader constructor:
```java
com.epam.parso.SasFileReader sasFileReader = new SasFileReaderImpl(is);
```

To get the properties of a SAS7BDAT file, use:
```java
sasFileReader.getSasFileProperties();
```

To get the description of the SAS7BDAT columns, use:

```java
sasFileReader.getColumns();
```
To get the data of the SAS7BDAT file, use:

```java
sasFileReader.readAll(); //to read all rows at once

sasFileReader.readNext(); //to read rows one by one
```

To convert the metadata of the file into CSV format, use:

```java
Writer writer = new StringWriter();
CSVMetadataWriter csvMetadataWriter = new CSVMetadataWriterImpl(writer);
csvMetadataWriter.writeMetadata(sasFileReader.getColumns());
```
To convert the data of the file into CSV format, use:

```java
Writer writer = new StringWriter();
CSVDataWriter csvDataWriter = new CSVDataWriterImpl(writer);
csvDataWriter.writeColumnNames(sasFileReader.getColumns());
```

To write all rows at once to the ‘writer’ variable:

```java
csvDataWriter.writeRowsArray(sasFileReader.getColumns(), sasFileReader.readAll());
```
To write rows one by one to the ‘writer’ variable:

```java
csvDataWriter.writeRow(sasFileReader.getColumns(), sasFileReader.readNext());
```

## License
 Copyright (C) 2015 EPAM

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

## Commercial Availability
If the ALv2-licensed Parso does not satisfy your needs, please contact us at lifescience.opensource@epam.com to discuss the possibility of a commercial license.

We hope that you decide to use the Parso library. At EPAM, we are available to help you use, integrate, and support Parso.
