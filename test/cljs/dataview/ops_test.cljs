(ns cljs.dataview.ops-test
  (:use-macros [cljs-test.macros :only [deftest is= is]])
  (:require [cljs-test.core :as test]
            [cljs.dataview.ops :as op]))

(defn create-dataview [size]
  (->
    (js/ArrayBuffer. size)
    (js/DataView.)))

(defn set-float-data! [data-view offset data]
  (let [offsets (iterate (partial + 4) offset)
        pairs (partition 2 (interleave offsets data))]
    (doseq [[i n] pairs]
      (. data-view (setFloat32  i n true)))))

(defn tee [tee-fn x]
  (tee-fn x)
  x)

(deftest test-reader-decode-two-floats-le
  (let [dv (create-dataview 16)
        reader (op/create-reader dv)
        data [4.0 17.5 16.5 43.0]]

    (set-float-data! dv 0 data)

    (is=
      data
      (repeatedly (count data) #(tee println (op/read-float32-le reader))))))
