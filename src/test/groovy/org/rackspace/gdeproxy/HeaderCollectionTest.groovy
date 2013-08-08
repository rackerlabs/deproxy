/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rackspace.gdeproxy

import static org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 *
 * @author richard-sartor
 */
class HeaderCollectionTest {

    HeaderCollection headers

    @Before
    void setUp() {
        this.headers = new HeaderCollection()
    }

    @Test
    void testSize() {
        assertEquals(0, this.headers.size())

        this.headers.add('Name', 'Value')
        assertEquals(1, this.headers.size())

        this.headers.add('Name', 'Value')
        assertEquals(2, this.headers.size())

        this.headers.add('Name2', 'Value')
        assertEquals(3, this.headers.size())
    }

    @Test
    void testContains() {
        this.headers.add('Name', 'Value')

        assertEquals(1, this.headers.size())
        assertTrue(this.headers.contains('Name'))
    }

    @Test
    void testContainsCase() {
        this.headers.add('Name', 'Value')

        assertEquals(1, this.headers.size())
        assertTrue(this.headers.contains('Name'))
        assertTrue(this.headers.contains('name'))
        assertTrue(this.headers.contains('NAME'))
        assertTrue(this.headers.contains('nAmE'))
    }

    @Test
    void testFindAll() {
        this.headers.add('A', 'qwerty')
        this.headers.add('B', 'asdf')
        this.headers.add('C', 'zxcv')
        this.headers.add('A', 'uiop')
        this.headers.add('A', 'jkl;')

        assertArrayEquals(['qwerty', 'uiop', 'jkl;'] as String[],
                          this.headers.findAll('A') as String[])

        assertArrayEquals(['asdf'] as String[],
                          this.headers.findAll('B') as String[])

        assertArrayEquals(['zxcv'] as String[],
                          this.headers.findAll('C') as String[])
    }

    @Test
    void testGetFirstValue() {
        this.headers.add('Name', 'Value')

        assertEquals(1, this.headers.size())
        assertEquals("Value", this.headers.getFirstValue('Name'))
        assertEquals("Value", this.headers.getFirstValue('name'))
        assertEquals("Value", this.headers.getFirstValue('NAME'))
        assertEquals("Value", this.headers.getFirstValue('nAmE'))
    }

    @Test
    void testGetFirstValueWithDefault() {
        this.headers.add('Name', 'Value')

        assertEquals(1, this.headers.size())
        assertEquals("Value", this.headers.getFirstValue('Name', "Other"))
        assertEquals("Value", this.headers.getFirstValue('name', "Other"))
        assertEquals("Value", this.headers.getFirstValue('NAME', "Other"))
        assertEquals("Value", this.headers.getFirstValue('nAmE', "Other"))

        assertEquals("Something", this.headers.getFirstValue('Other Name', "Something"))
    }

    @Test
    void testGetAt() {
        this.headers.add('Name', 'Value')

        assertEquals(1, this.headers.size())
        assertEquals("Value", this.headers.getAt('Name'))
        assertEquals("Value", this.headers.getAt('name'))
        assertEquals("Value", this.headers.getAt('NAME'))
        assertEquals("Value", this.headers.getAt('nAmE'))
    }

    @Test
    void testMapNotation() {
        this.headers.add('Name', 'Value')

        assertEquals(1, this.headers.size())
        assertEquals("Value", this.headers['Name'])
        assertEquals("Value", this.headers['name'])
        assertEquals("Value", this.headers['NAME'])
        assertEquals("Value", this.headers['nAmE'])
    }

    @Test
    void testGetCountByName() {

        this.headers.add('A', 'qwerty')
        this.headers.add('B', 'asdf')
        this.headers.add('C', 'zxcv')
        this.headers.add('A', 'uiop')
        this.headers.add('A', 'jkl;')

        assertEquals(3, this.headers.getCountByName('A'))
        assertEquals(3, this.headers.getCountByName('a'))
        assertEquals(1, this.headers.getCountByName('B'))
        assertEquals(1, this.headers.getCountByName('C'))
    }
}


