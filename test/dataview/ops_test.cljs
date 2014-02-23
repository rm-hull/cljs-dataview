(ns dataview.ops-test
  (:use-macros
    [cljs-test.macros :only [deftest is= is is-thrown?]])
  (:require
    [cljs-test.core :as test]
    [dataview.test-helpers :refer [create-dataview set-float-data! set-binary-data!]]
    [dataview.ops :as op]))

(deftest reading-floats
  (let [n 16
        dataview (create-dataview n)
        reader (op/create-reader dataview)
        data [4.0 17.5 16.5 43.0]]

    (set-float-data! dataview 0 data)

    (is= (op/byte-length dataview) n "Byte-length check")
    (is= (op/can-read? dataview 0 4) true "Can-read? first 4 bytes")
    (is= (op/can-read? dataview 4 4) true "Can-read? 4 bytes, offset 4")
    (is= (op/can-read? dataview 8 4) true "Can-read? 4 bytes, offset 8")
    (is= (op/can-read? dataview 12 4) true "Can-read? 4 bytes, offset 12")
    (is= (op/can-read? dataview 16 4) false "Can-read? 4 bytes, offset 16")
    (is= (op/tell reader) 0 "Reader should be at start")
    (is= (op/eod? reader) false "Should not be EOD")
    (is= (doall (repeatedly (count data) #(op/read-float32-le reader))) data "Read four 32-bit floats")
    (is= (op/tell reader) n "Reader should be at end")
    (is= (op/eod? reader) true "Should be EOD")
    (is= (op/read-float32-le reader) nil "Cannot read past end of the data")))

(deftest reading-strings
  (let [data (str
               "Is this all there was?\n"
               "What was all the fuss?\n"
               "Why did I bother?")
        dataview (create-dataview (count data))
        reader (op/create-reader dataview)]

    (set-binary-data! dataview 0 (seq data))

    (is= (op/read-fixed-string reader 10) "Is this al" "Single fixed string")
    (is= (op/read-fixed-string reader 13) "l there was?\n" "Next single fixed string")
    (is= (op/eod? reader) false "Should not be EOD")
    (is= (op/read-utf8-string reader #{\newline}) "What was all the fuss?\n" "Following single delimited string")
    (is= (op/read-utf8-string reader #{\newline}) "Why did I bother?" "Next single delimited string")
    (is= (op/eod? reader) true "Should be EOD")
    (is= (op/read-utf8-string reader #{\newline}) nil "Cannot read past end of the data")))

(deftest triangle-binary-data
  (let [data (list
               0x20 0x1c 0x00 0x00
               0xbd 0x4c 0x7f 0xbf
               0x39 0x13 0x56 0xbd
               0x39 0x13 0x56 0x3d
               0x00 0x00 0x80 0x40
               0x00 0x00 0x00 0x00
               0x00 0x00 0x00 0x00
               0x3f 0xa6 0x7f 0x40
               0x05 0x13 0xd6 0x3d
               0x00 0x00 0x00 0x00
               0xba 0x3f 0x7e 0x40
               0x05 0x13 0xd6 0x3d
               0xf7 0xc7 0xd5 0xbe
               0x00 0x00)
        dataview (create-dataview (count data))
        reader (op/create-reader dataview)]

    (set-binary-data! dataview 0 data)

    (is= (op/read-uint32-le reader) 7200 "Num triangles")

    (is= (op/read-float32-le reader) -0.9972646832466125 "Normal X")
    (is= (op/read-float32-le reader) -0.05226442590355873 "Normal Y")
    (is= (op/read-float32-le reader)  0.05226442590355873 "Normal Z")

    (is= (op/read-float32-le reader)  4.0 "Point 1 X")
    (is= (op/read-float32-le reader)  0.0 "Point 1 Y")
    (is= (op/read-float32-le reader)  0.0 "Point 1 Z")

    (is= (op/read-float32-le reader)  3.9945218563079834 "Point 2 X")
    (is= (op/read-float32-le reader)  0.10452846437692642 "Point 2 Y")
    (is= (op/read-float32-le reader)  0.0 "Point 2 Z")

    (is= (op/read-float32-le reader)  3.972639560699463 "Point 3 X")
    (is= (op/read-float32-le reader)  0.10452846437692642 "Point 3 Y")
    (is= (op/read-float32-le reader) -0.4175412356853485 "Point 3 Z")

    (is= (op/read-uint16-le reader)  0.0 "Attributes")))


(deftest read-utf8
  (let [data (list
               0xe2 0x94 0x8f
               0xe2 0x94 0x81
               0xe2 0x94 0x93
               \newline
               0xc3 0x83
               0xc3 0x84)
        dataview (create-dataview (count data))
        reader (op/create-reader dataview)]

    (set-binary-data! dataview 0 data)

    (is= (op/read-utf8-string reader #{\newline}) "┏━┓\n" "3-part box characters: ┏━┓")
    (is= (op/read-utf8-string reader #{\newline}) "ÃÄ"     "2-part accents: ÃÄ")))

(deftest create-reader-from-string
  (let [reader (op/create-reader (str
                      "She came from Greece. She had a thirst for knowledge.\n"
                      "She studied sculpture at Saint Martin's College.\n"))]

    (is= (op/read-utf8-string reader #{\space}) "She " "Check reading a word from a string reader")
    (is= (op/read-byte reader) 99 "Check reading a byte from a string reader")
    (is= (op/tell reader) 5 "Verify position before anticipated failure")
    (is-thrown? (op/read-uint16-le reader) "Check reading a uint16 from a string reader throws an exception")

    (is= (op/tell reader) 5 "Verify position after anticipated failure")

    (op/rewind! reader)

    (is= (op/read-utf8-string reader #{\newline})
         "She came from Greece. She had a thirst for knowledge.\n"
         "Check line after rewind")

    ))
