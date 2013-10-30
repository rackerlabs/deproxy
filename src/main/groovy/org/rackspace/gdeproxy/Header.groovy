/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.gdeproxy;

/**
 *
 * @author richard-sartor
 *
 * A simple name-value pair.
 *
 */
public class Header {

    final String name;
    final String value;

    public Header(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return this.name + ": " + this.value;
    }
}

