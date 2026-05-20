package com.epam.parso.date;

import org.junit.Test;

import static com.epam.parso.date.SasTemporalUtils.roundSeconds;
import static org.assertj.core.api.Assertions.assertThat;

public class SasTemporalUtilsTest {


    @Test
    public void test() {
        assertThat(roundSeconds(0., 0)).isEqualTo("0");
        assertThat(roundSeconds(0.001, 0)).isEqualTo("0");
        assertThat(roundSeconds(0.001, 1)).isEqualTo("0.0");
        assertThat(roundSeconds(0.001, 2)).isEqualTo("0.00");
        assertThat(roundSeconds(0.954, 0)).isEqualTo("1");
        assertThat(roundSeconds(0.954, 1)).isEqualTo("1.0");
        assertThat(roundSeconds(0.954, 2)).isEqualTo("0.95");

        assertThat(roundSeconds(86398.0, 0)).isEqualTo("86398");
        assertThat(roundSeconds(86398.0, 1)).isEqualTo("86398.0");
        assertThat(roundSeconds(86398.4, 0)).isEqualTo("86398");
        assertThat(roundSeconds(86398.4, 1)).isEqualTo("86398.4");
        assertThat(roundSeconds(86398.5, 0)).isEqualTo("86399");
        assertThat(roundSeconds(86398.5, 1)).isEqualTo("86398.5");
        assertThat(roundSeconds(86398.94, 0)).isEqualTo("86399");
        assertThat(roundSeconds(86398.94, 1)).isEqualTo("86398.9");
        assertThat(roundSeconds(86398.95, 0)).isEqualTo("86399");
        assertThat(roundSeconds(86398.95, 1)).isEqualTo("86398.9"); //exceptional case due to IEEE 754
        assertThat(roundSeconds(86398.999, 2)).isEqualTo("86399.00");

        assertThat(roundSeconds(86399.0, 0)).isEqualTo("86399");
        assertThat(roundSeconds(86399.0, 1)).isEqualTo("86399.0");
        assertThat(roundSeconds(86399.4, 0)).isEqualTo("86399");
        assertThat(roundSeconds(86399.4, 1)).isEqualTo("86399.4");
        assertThat(roundSeconds(86399.5, 0)).isEqualTo("86399");
        assertThat(roundSeconds(86399.5, 1)).isEqualTo("86399.5");
        assertThat(roundSeconds(86399.94, 0)).isEqualTo("86399");
        assertThat(roundSeconds(86399.94, 1)).isEqualTo("86399.9");
        assertThat(roundSeconds(86399.95, 0)).isEqualTo("86399");
        assertThat(roundSeconds(86399.95, 1)).isEqualTo("86399.9");
        assertThat(roundSeconds(86399.999, 2)).isEqualTo("86399.99");

        assertThat(roundSeconds(86400.0, 0)).isEqualTo("86400");
        assertThat(roundSeconds(86400.5, 0)).isEqualTo("86401");
        assertThat(roundSeconds(86400.5, 1)).isEqualTo("86400.5");

        assertThat(roundSeconds(-0.01, 0)).isEqualTo("-1");
        assertThat(roundSeconds(-0.01, 1)).isEqualTo("-0.1");
        assertThat(roundSeconds(-0.01, 2)).isEqualTo("-0.01");
        assertThat(roundSeconds(-0.99, 0)).isEqualTo("-1");
        assertThat(roundSeconds(-0.99, 1)).isEqualTo("-1.0");
        assertThat(roundSeconds(-0.99, 2)).isEqualTo("-0.99");
    }
}
