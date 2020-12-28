/**
 * *************************************************************************
 *
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

import com.epam.parso.impl.SasFileReaderImpl;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BugsTest {


    @Test
    public void testOOM() throws Exception {
        try (InputStream is = this.getClass().getResourceAsStream("/bugs/mixed_data_one.sas7bdat.oom")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is);
            long rowCount = sasFileReader.getSasFileProperties().getRowCount();
            assertThat(rowCount).isEqualTo(1);
        }
    }

    /**
     * See reported comment at https://github.com/epam/parso/issues/54#issue-492745901
     */
    @Test
    public void testColumnFormat64BitIssue54() throws Exception {
        try (InputStream is = this.getClass().getResourceAsStream("/bugs/54-class.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is);

            SasFileProperties properties = sasFileReader.getSasFileProperties();
            List<Column> columns = sasFileReader.getColumns();

            assertThat(properties.isU64()).isTrue();

            assertThat(columns.get(0).getName()).isEqualTo("Name");
            assertThat(columns.get(0).getLength()).isEqualTo(8);
            assertThat(columns.get(0).getFormat().getWidth()).isEqualTo(0);
            assertThat(columns.get(0).getFormat().getPrecision()).isEqualTo(0);

            assertThat(columns.get(1).getName()).isEqualTo("Sex");
            assertThat(columns.get(1).getLength()).isEqualTo(1);
            assertThat(columns.get(1).getFormat().getWidth()).isEqualTo(0);
            assertThat(columns.get(1).getFormat().getPrecision()).isEqualTo(0);

            assertThat(columns.get(2).getName()).isEqualTo("Age");
            assertThat(columns.get(2).getLength()).isEqualTo(8);
            assertThat(columns.get(2).getFormat().getWidth()).isEqualTo(4);
            assertThat(columns.get(2).getFormat().getPrecision()).isEqualTo(0);

            assertThat(columns.get(3).getName()).isEqualTo("Height");
            assertThat(columns.get(3).getLength()).isEqualTo(8);
            assertThat(columns.get(3).getFormat().getWidth()).isEqualTo(8);
            assertThat(columns.get(3).getFormat().getPrecision()).isEqualTo(2);

            assertThat(columns.get(4).getName()).isEqualTo("Weight");
            assertThat(columns.get(4).getLength()).isEqualTo(8);
            assertThat(columns.get(4).getFormat().getWidth()).isEqualTo(0);
            assertThat(columns.get(4).getFormat().getPrecision()).isEqualTo(0);
        }
    }

    /**
     * See dataset properties at https://github.com/epam/parso/issues/54#issuecomment-737872522
     */
    @Test
    public void testColumnFormat32BitIssue54() throws Exception {
        try (InputStream is = this.getClass().getResourceAsStream("/bugs/54-cookie.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is);

            SasFileProperties properties = sasFileReader.getSasFileProperties();
            List<Column> columns = sasFileReader.getColumns();

            assertThat(properties.isU64()).isFalse();

            assertThat(columns.get(15).getName()).isEqualTo("AROMA");
            assertThat(columns.get(15).getLength()).isEqualTo(8);
            assertThat(columns.get(15).getFormat().getWidth()).isEqualTo(4);
            assertThat(columns.get(15).getFormat().getPrecision()).isEqualTo(1);

            assertThat(columns.get(16).getName()).isEqualTo("SWEET");
            assertThat(columns.get(16).getLength()).isEqualTo(8);
            assertThat(columns.get(16).getFormat().getWidth()).isEqualTo(3);
            assertThat(columns.get(16).getFormat().getPrecision()).isEqualTo(1);

            assertThat(columns.get(25).getName()).isEqualTo("INSTRON");
            assertThat(columns.get(25).getLength()).isEqualTo(4);
            assertThat(columns.get(25).getFormat().getWidth()).isEqualTo(0);
            assertThat(columns.get(25).getFormat().getPrecision()).isEqualTo(0);

            assertThat(columns.get(26).getName()).isEqualTo("L");
            assertThat(columns.get(26).getLength()).isEqualTo(8);
            assertThat(columns.get(26).getFormat().getWidth()).isEqualTo(5);
            assertThat(columns.get(26).getFormat().getPrecision()).isEqualTo(2);
        }
    }

    @Test
    public void testCompressionMethodsIssue55() throws Exception {
        try (InputStream is = this.getClass().getResourceAsStream("/sas7bdat/mix_data_misc.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is);
            assertThat(sasFileReader.getSasFileProperties().getCompressionMethod()).isEqualTo("SASYZCRL");
        }

        try (InputStream is = this.getClass().getResourceAsStream("/sas7bdat/comp_deleted.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is);
            assertThat(sasFileReader.getSasFileProperties().getCompressionMethod()).isEqualTo("SASYZCRL");
        }

        try (InputStream is = this.getClass().getResourceAsStream("/sas7bdat/tmp868_14.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is);
            assertThat(sasFileReader.getSasFileProperties().getCompressionMethod()).isEqualTo("SASYZCRL");
        }

        try (InputStream is = this.getClass().getResourceAsStream("/sas7bdat/all_rand_normal_with_deleted.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is);
            assertThat(sasFileReader.getSasFileProperties().getCompressionMethod()).isNull();
        }

        try (InputStream is = this.getClass().getResourceAsStream("/sas7bdat/charset_sjis.sas7bdat")) {
            SasFileReader sasFileReader = new SasFileReaderImpl(is);
            assertThat(sasFileReader.getSasFileProperties().getCompressionMethod()).isNull();
        }
    }
}
