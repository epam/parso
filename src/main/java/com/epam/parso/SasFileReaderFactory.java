package com.epam.parso;

import java.io.InputStream;

/**
 * Factory for creating {@link SasFileReader}s using {@link InputStream}s.
 *
 * @author Gabor Bakos
 * @since 2.1
 */
public interface SasFileReaderFactory {
    /**
     * Creates a new {@link SasFileReader} using {@code stream}.
     *
     * @param stream
     *            an input stream which should contain a correct sas7bdat file.
     * @return An {@link SasFileReader} instance.
     */
    SasFileReader create(InputStream stream);

    /**
     * Creates a new {@link SasFileReader} using {@code stream}.
     *
     * @param stream
     *            an input stream which should contain a correct sas7bdat file.
     * @param encoding
     *            the string containing the encoding to use in strings output
     * @return An {@link SasFileReader} instance.
     */
    SasFileReader create(InputStream stream, String encoding);

    /**
     * Creates a new {@link SasFileReader} using {@code stream}.
     *
     * @param stream
     *            an input stream which should contain a correct sas7bdat file.
     * @param byteOutput
     *            the flag of data output in binary or string format
     * @return An {@link SasFileReader} instance.
     */
    SasFileReader create(InputStream stream, Boolean byteOutput);
}
