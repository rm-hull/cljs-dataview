(ns cljs.dataview.ops-test
  (:use-macros [cljs-test.macros :only [deftest is= is]])
  (:require [cljs-test.core :as test]
            [cljs.dataview.ops :as op]))

(defn create-dataview [size]
  (->
    (js/ArrayBuffer. size)
    (js/DataView.)))


(defn set-binary-data! [data-view offset data]
  (let [offsets (iterate inc offset)
        pairs (partition 2 (interleave offsets data))]
    (doseq [[i n] pairs]
      (.setUint8 data-view i (if (string? n) (.charCodeAt n 0) n)))))

(defn set-float-data! [data-view offset data]
  (let [offsets (iterate (partial + 4) offset)
        pairs (partition 2 (interleave offsets data))]
    (doseq [[i n] pairs]
      (. data-view (setFloat32  i n true)))))

(defn tee [tee-fn x]
  (tee-fn x)
  x)

(deftest test-reader-decode-floats-le
  (let [dv (create-dataview 16)
        reader (op/create-reader dv)
        data [4.0 17.5 16.5 43.0]]

    (set-float-data! dv 0 data)

    (is=
      data
      (repeatedly (count data) #(op/read-float32-le reader))
      "Four 32-bit floats")))

(deftest reading-strings
  (let [dv (create-dataview 128)
        reader (op/create-reader dv)
        data (str
               "Is this all there was?\n"
               "What was all the fuss?\n"
               "Why did I bother?\n")]

    (set-binary-data! dv 0 (seq data))

    (is= (op/read-fixed-string reader 10 :ascii) "Is this al" "Single fixed string")
    (is= (op/read-fixed-string reader 13 :ascii) "l there was?\n" "Next single fixed string")
    (is= (op/read-delimited-string reader #{"\n"} :ascii) "What was all the fuss?" "Following single delimited string")
    (is= (op/read-delimited-string reader #{"\n"} :ascii) "Why did I bother?" "Next single delimited string")))

(comment ; causes PhantomJS to segfault
(deftest triangle-binary-data
  (let [data (list 0x20 0x1c 0x00 0x00
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
        dv (create-dataview 256)
        reader (op/create-reader dv)]

    (set-binary-data! dv 0 data)

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

)



