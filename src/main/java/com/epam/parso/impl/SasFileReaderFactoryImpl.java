/**
 *
 */
package com.epam.parso.impl;

import java.io.InputStream;

import com.epam.parso.SasFileReader;
import com.epam.parso.SasFileReaderFactory;

/**
 * @author Gabor Bakos
 *
 */
public class SasFileReaderFactoryImpl implements SasFileReaderFactory {

    /**
     *
     */
    public SasFileReaderFactoryImpl() {
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     *
     * @see com.epam.parso.SasFileReaderFactory#create(java.io.InputStream)
     */
    @Override
    public SasFileReader create(final InputStream stream) {
        return new SasFileReaderImpl(stream);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.epam.parso.SasFileReaderFactory#create(java.io.InputStream,
     * java.lang.String)
     */
    @Override
    public SasFileReader create(final InputStream stream, final String encoding) {
        return new SasFileReaderImpl(stream, encoding);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.epam.parso.SasFileReaderFactory#create(java.io.InputStream,
     * java.lang.Boolean)
     */
    @Override
    public SasFileReader create(final InputStream stream, final Boolean byteOutput) {
        return new SasFileReaderImpl(stream, byteOutput);
    }

}
