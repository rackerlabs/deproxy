/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rackspace.gdeproxy;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author richard-sartor
 */
public class CountingOutputStream extends OutputStream {

    public int Count = 0;
    OutputStream _source;

    public CountingOutputStream(OutputStream source) {
        _source = source;
    }

    @Override
    public void write(int i) throws IOException {
        _source.write(i);
        Count++;
    }

}
