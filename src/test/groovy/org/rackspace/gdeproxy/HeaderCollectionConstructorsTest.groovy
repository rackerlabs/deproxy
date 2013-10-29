package org.rackspace.gdeproxy

import spock.lang.Specification

class HeaderCollectionConstructorsTest extends Specification {

    def "no arguments to the constructor"() {

        when: "call the constructor with no arguments"
        def hc = new HeaderCollection()

        then: "should be valid, empty collection"
        hc != null
        hc instanceof HeaderCollection
        hc.size() == 0
    }

    def "null argument to the constructor"() {

        when: "call the constructor with a single null argument"
        def hc = new HeaderCollection(null)

        then: "should be valid, empty collection"
        hc != null
        hc instanceof HeaderCollection
        hc.size() == 0
    }

    def "Header argument"() {

        when:
        HeaderCollection hc = new HeaderCollection(new Header("n1", "v1"))

        then:
        hc != null
        hc.size() == 1
        hc[0].name == "n1"
        hc[0].value == "v1"

    }

    def "string argument"() {

        when:
        HeaderCollection hc = new HeaderCollection("n1:v1")

        then:
        hc != null
        hc.size() == 1
        hc[0].name == "n1"
        hc[0].value == "v1"

    }

    def "string argument, leading space in name"() {

        when:
        HeaderCollection hc = new HeaderCollection("  n1:v1")

        then:
        hc != null
        hc.size() == 1
        hc[0].name == "n1"
        hc[0].value == "v1"

    }

    def "string argument, trailing space in name"() {

        when:
        HeaderCollection hc = new HeaderCollection("n1  :v1")

        then:
        hc != null
        hc.size() == 1
        hc[0].name == "n1"
        hc[0].value == "v1"

    }

    def "string argument, leading space in value"() {

        when:
        HeaderCollection hc = new HeaderCollection("n1:  v1")

        then:
        hc != null
        hc.size() == 1
        hc[0].name == "n1"
        hc[0].value == "v1"

    }

    def "string argument, trailing space in value"() {

        when:
        HeaderCollection hc = new HeaderCollection("n1:v1  ")

        then:
        hc != null
        hc.size() == 1
        hc[0].name == "n1"
        hc[0].value == "v1"

    }

    def "string argument, single part"() {

        when:
        HeaderCollection hc = new HeaderCollection("n1")

        then:
        hc != null
        hc.size() == 1
        hc[0].name == "n1"
        hc[0].value == ""

    }

    def "string argument, three parts"() {

        when:
        HeaderCollection hc = new HeaderCollection("n1: v1: v2")

        then:
        hc != null
        hc.size() == 1
        hc[0].name == "n1"
        hc[0].value == "v1: v2"

    }

    def "object argument"() {

        when:
        HeaderCollection hc = new HeaderCollection(new DummyObject(id: 3))

        then:
        hc != null
        hc.size() == 1
        hc[0].name == "dummy object, id = 3"
        hc[0].value == ""

    }

    def "empty list"() {

        when:
        HeaderCollection hc = new HeaderCollection([ ]);

        then:
        hc != null
        hc.size() == 0

    }


    def "list with string element"() {

        when:
        HeaderCollection hc = new HeaderCollection([ "n1: v1" ]);

        then:
        hc != null
        hc.size() == 1
        hc[0].name == "n1"
        hc[0].value == "v1"

    }

    def "list with non-header, non-string element"() {

        when:
        HeaderCollection hc = new HeaderCollection([ new DummyObject(id: 5) ]);

        then:
        hc != null
        hc.size() == 1
        hc[0].name == "dummy object, id = 5"
        hc[0].value == ""

    }

    def "list with null element"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                new Header("n1", "v1"),
                null,
                new Header("n2", "v2")
        ]);

        then:
        hc != null
        hc.size() == 2
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"

    }

    def "list of headers argument"() {

        when: "call the constructor with a list of headers"
        HeaderCollection hc = new HeaderCollection([ new Header("name1", "value1"), new Header("name2", "value2") ])

        then: "should be valid collection with the given headers in the given order"
        hc != null
        hc instanceof HeaderCollection
        hc.size() == 2
        hc[0].name == "name1"
        hc[0].value == "value1"
        hc[1].name == "name2"
        hc[1].value == "value2"
    }

    def "list of list argument"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                [ new Header("n1", "v1"), new Header("n2", "v2") ],
                [ new Header("n3", "v3"), new Header("n4", "v4") ],
        ]);

        then:
        hc != null
        hc.size() == 4
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == "n3"
        hc[2].value == "v3"
        hc[3].name == "n4"
        hc[3].value == "v4"

    }

    def "list of array argument"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                [ new Header("n1", "v1"), new Header("n2", "v2") ] as Object[],
                [ new Header("n3", "v3"), new Header("n4", "v4") ] as Object[],
        ]);

        then:
        hc != null
        hc.size() == 4
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == "n3"
        hc[2].value == "v3"
        hc[3].name == "n4"
        hc[3].value == "v4"

    }

    def "map argument"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                n1: "v1",
                n2: "v2",
                n3: "v3",
                n4: "v4",
        ])

        then:
        hc != null
        hc.size() == 4
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == "n3"
        hc[2].value == "v3"
        hc[3].name == "n4"
        hc[3].value == "v4"

    }

    def "empty map"() {

        when:
        HeaderCollection hc = new HeaderCollection([:])

        then:
        hc != null
        hc.size() == 0

    }

    def "map with non-string key"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                n1: "v1",
                (new DummyObject(id:1)): "v2",
        ])

        then:
        hc != null
        hc.size() == 2
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "dummy object, id = 1"
        hc[1].value == "v2"

    }

    def "map with non-string value"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                n1: "v1",
                n2: (new DummyObject(id:2)),
        ])

        then:
        hc != null
        hc.size() == 2
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "dummy object, id = 2"

    }

    def "map with list key"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                n1: "v1",
                [1, 2, 3]: "v2",
        ])

        then:
        hc != null
        hc.size() == 2
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "[1, 2, 3]"
        hc[1].value == "v2"

    }

    def "map with list value"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                n1: "v1",
                n2: [1, 2, 3],
        ])

        then:
        hc != null
        hc.size() == 2
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "[1, 2, 3]"

    }

    def "map with null key"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                n1: "v1",
                (null): "v2",
        ])

        then:
        hc != null
        hc.size() == 2
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == ""
        hc[1].value == "v2"

    }

    def "map with null value"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                n1: "v1",
                n2: null,
        ])

        then:
        hc != null
        hc.size() == 2
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == ""

    }

    def "another HeaderCollection as argument"() {

        given:
        def hc1 = new HeaderCollection()
        hc1.add("n1", "v1")
        hc1.add("n2", "v2")

        when:
        HeaderCollection hc = new HeaderCollection(hc1)

        then:
        hc != null
        hc.size() == 2
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
    }

    def "empty array"() {

        when:
        HeaderCollection hc = new HeaderCollection([] as Object[]);

        then:
        hc != null
        hc.size() == 0

    }

    def "array with string element"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                "n1: v1",
        ] as Object[]);

        then:
        hc != null
        hc.size() == 1
        hc[0].name == "n1"
        hc[0].value == "v1"

    }

    def "array with non-header, non-string element"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                new DummyObject(id: 5),
        ] as Object[]);

        then:
        hc != null
        hc.size() == 1
        hc[0].name == "dummy object, id = 5"
        hc[0].value == ""

    }

    def "array with null element"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                new Header("n1", "v1"),
                null,
                new Header("n2", "v2")
        ] as Object[]);

        then:
        hc != null
        hc.size() == 2
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"

    }

    def "array of headers argument"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                new Header("n1", "v1"),
                new Header("n2", "v2")
        ] as Object[]);

        then:
        hc != null
        hc.size() == 2
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"

    }

    def "array of list argument"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                        [ new Header("n1", "v1"), new Header("n2", "v2") ],
                        [ new Header("n3", "v3"), new Header("n4", "v4") ],
                ] as Object[]);

        then:
        hc != null
        hc.size() == 4
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == "n3"
        hc[2].value == "v3"
        hc[3].name == "n4"
        hc[3].value == "v4"
    }

    def "array of array argument"() {

        when:
        HeaderCollection hc = new HeaderCollection([
                [ new Header("n1", "v1"), new Header("n2", "v2") ] as Object[],
                [ new Header("n3", "v3"), new Header("n4", "v4") ] as Object[],
        ] as Object[]);

        then:
        hc != null
        hc.size() == 4
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == "n3"
        hc[2].value == "v3"
        hc[3].name == "n4"
        hc[3].value == "v4"

    }

    def "multiple null arguments"() {

        when:
        HeaderCollection hc = new HeaderCollection(null, null, null)

        then:
        hc != null
        hc.size() == 0

    }

    def "multiple string arguments"() {

        when:
        HeaderCollection hc = new HeaderCollection(
                "n1: v1",
                "n2: v2",
                "n3: v3")

        then:
        hc != null
        hc.size() == 3
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == "n3"
        hc[2].value == "v3"

    }

    def "multiple object arguments"() {

        when:
        HeaderCollection hc = new HeaderCollection(
                new DummyObject(id: 6),
                new DummyObject(id: 7),
                new DummyObject(id: 8))

        then:
        hc != null
        hc.size() == 3
        hc[0].name == "dummy object, id = 6"
        hc[0].value == ""
        hc[1].name == "dummy object, id = 7"
        hc[1].value == ""
        hc[2].name == "dummy object, id = 8"
        hc[2].value == ""

    }

    def "multiple map arguments"() {

        when:
        HeaderCollection hc = new HeaderCollection(
                [ n1: "v1", n2: "v2" ],
                [ n3: "v3", n4: "v4" ])

        then:
        hc != null
        hc.size() == 4
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == "n3"
        hc[2].value == "v3"
        hc[3].name == "n4"
        hc[3].value == "v4"

    }

    def "multiple list arguments"() {

        when:
        HeaderCollection hc = new HeaderCollection(
                [ new Header("n1", "v1"), new Header("n2", "v2") ],
                [ new Header("n3", "v3"), new Header("n4", "v4") ],
        )

        then:
        hc != null
        hc.size() == 4
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == "n3"
        hc[2].value == "v3"
        hc[3].name == "n4"
        hc[3].value == "v4"

    }

    def "multiple HeaderCollection arguments"() {

        given:
        def hc1 = new HeaderCollection()
        hc1.add("n1", "v1")
        hc1.add("n2", "v2")
        def hc2 = new HeaderCollection()
        hc2.add("n3", "v3")

        when:
        HeaderCollection hc = new HeaderCollection(hc1, hc2)

        then:
        hc != null
        hc.size() == 3
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == "n3"
        hc[2].value == "v3"

    }

    def "multiple mixed arguments"() {

        given:
        def hc1 = new HeaderCollection()
        hc1.add("n1", "v1")
        hc1.add("n2", "v2")

        when:
        HeaderCollection hc = new HeaderCollection(
                [ new Header("n3", "v3"), new Header("n4", "v4"), "n5: v5" ],
                [ n6: "v6", n7: "v7" ],
                [
                        [
                                [ n8: "v8"],
                                hc1,
                        ],
                        [ n9: "v9" ]
                ])

        then:
        hc != null
        hc.size() == 9
        hc[0].name == "n3"
        hc[0].value == "v3"
        hc[1].name == "n4"
        hc[1].value == "v4"
        hc[2].name == "n5"
        hc[2].value == "v5"
        hc[3].name == "n6"
        hc[3].value == "v6"
        hc[4].name == "n7"
        hc[4].value == "v7"
        hc[5].name == "n8"
        hc[5].value == "v8"
        hc[6].name == "n1"
        hc[6].value == "v1"
        hc[7].name == "n2"
        hc[7].value == "v2"
        hc[8].name == "n9"
        hc[8].value == "v9"
    }

    def "re-use list argument"() {

        given:
        def list = [ new Header("n1", "v1"), new Header("n2", "v2") ]

        when:
        HeaderCollection hc = new HeaderCollection(list, "n3: v3", list)

        then:
        hc != null
        hc.size() == 5
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == "n3"
        hc[2].value == "v3"
        hc[3].name == "n1"
        hc[3].value == "v1"
        hc[4].name == "n2"
        hc[4].value == "v2"

    }

    def "cycle list argument"() {

        given:
        def list = [ new Header("n1", "v1"), new Header("n2", "v2") ]
        list.add(list)

        when:
        HeaderCollection hc = new HeaderCollection(list)

        then:
        hc != null
        hc.size() == 2
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"

    }

    def "re-use map argument"() {

        given:
        def map = [ n1: "v1", n2: "v2" ]

        when:
        HeaderCollection hc = new HeaderCollection(map, "n3: v3", map)

        then:
        hc != null
        hc.size() == 5
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == "n3"
        hc[2].value == "v3"
        hc[3].name == "n1"
        hc[3].value == "v1"
        hc[4].name == "n2"
        hc[4].value == "v2"

    }

    def "cycle map argument as name"() {

        given:
        def map = [ n1: "v1", n2: "v2" ]
        def extras = [ (map): "v3" ]
        map.putAll(extras)

        when:
        HeaderCollection hc = new HeaderCollection(map)

        then:
        hc != null
        hc.size() == 3
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == ""
        hc[2].value == "v3"

    }

    def "cycle map argument as value"() {

        given:
        def map = [ n1: "v1", n2: "v2", n3: "v3" ]
        map["n3"] = map

        when:
        HeaderCollection hc = new HeaderCollection(map)

        then:
        hc != null
        hc.size() == 3
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == "n3"
        hc[2].value == map.toString()

    }

    def "re-use array argument"() {

        given:
        Object[] arr = [ new Header("n1", "v1"), new Header("n2", "v2") ]

        when:
        HeaderCollection hc = new HeaderCollection(arr, "n3: v3", arr)

        then:
        hc != null
        hc.size() == 5
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
        hc[2].name == "n3"
        hc[2].value == "v3"
        hc[3].name == "n1"
        hc[3].value == "v1"
        hc[4].name == "n2"
        hc[4].value == "v2"

    }

    def "cycle array argument"() {

        given:
        Object[] arr = [ new Header("n1", "v1"), new Header("n2", "v2"), null ]
        arr[2] = arr

        when:
        HeaderCollection hc = new HeaderCollection(arr)

        then:
        hc != null
        hc.size() == 2
        hc[0].name == "n1"
        hc[0].value == "v1"
        hc[1].name == "n2"
        hc[1].value == "v2"
    }
}
