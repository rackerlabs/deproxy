/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rackspace.gdeproxy;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author richard-sartor
 */
public class CountingInputStream extends InputStream {

    public int Count = 0;
    InputStream _source;

    public CountingInputStream(InputStream source) {
        super();
        _source = source;
    }

    @Override
    public int read() throws IOException {
        int c = _source.read();

        if (c >= 0) {
            Count++;
        }

        return c;
    }


}
