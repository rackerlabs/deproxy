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
class Header {

    public String name;
    public String value;

  public Header(String name, String value) {
        this.name = name;
        this.value = value;
    }
}

