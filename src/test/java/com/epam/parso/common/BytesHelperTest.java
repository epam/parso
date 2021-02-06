package com.epam.parso.common;

import static org.junit.Assert.assertEquals;

import java.util.List;
import javafx.util.Pair;
import org.assertj.core.util.Lists;
import org.junit.Test;

public class BytesHelperTest {

    @Test
    public void testDouble() {

        List<Pair<byte[], Object>> valuesToCheck = Lists.newArrayList(
            new Pair<>(new byte[] {65, 16, 0, 0, 0, 0, 0, 0}, 1L),
            new Pair<>(new byte[] {-63, 16, 0, 0, 0, 0, 0, 0}, -1L),
            new Pair<>(new byte[] {65, 32, 0, 0, 0, 0, 0, 0}, 2L),
            new Pair<>(new byte[] {-63, 32, 0, 0, 0, 0, 0, 0}, -2L),
            new Pair<>(new byte[] {-63, 80, 0, 0, 0, 0, 0, 0}, -5L),
            new Pair<>(new byte[] {65, -16, 0, 0, 0, 0, 0, 0}, 15L),
            new Pair<>(new byte[] {66, 112, -128, 0, 0, 0, 0, 0}, 112.5),
            new Pair<>(new byte[] {66, 56, -128, 0, 0, 0, 0, 0}, 56.5),
            new Pair<>(new byte[] {66, 65, 76, -52, -52, -52, -52, -52}, 65.3));

        valuesToCheck.forEach(pair ->
            assertEquals(BytesHelper.convertIbmByteArrayToNumber(pair.getKey()), pair.getValue())
        );
    }

}