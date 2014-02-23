(ns dataview.boyer-moore-test
  (:use-macros
    [cljs-test.macros :only [deftest is= is]])
  (:require
    [cljs-test.core :as test]
    [dataview.test-helpers :refer [create-dataview set-float-data! set-binary-data!]]
    [dataview.boyer-moore :refer [index-of]]
    [dataview.ops :as op]))

(deftest boyer-moore->string
  (is= (index-of "Hello world" "") 0 "Empty string")
  (is= (index-of "Hello world" "" 3) 3 "Empty offset")
  (is= (index-of "Hello world" "Hello") 0 "Match at zero")
  (is= (index-of "Hello world" "world") 6 "Match at pos 6")
  (is= (index-of "Hello world" "o wo") 4 "Match at pos 4")
  (is= (index-of "Hello world" "not") nil "No match"))

(deftest boyer-moore->dataview
  (let [data (str
               "Rent a flat above a shop!\n"
               "Cut your hair and get a job!\n"
               "Smoke some fags and play some pool.")
        dataview (create-dataview (count data))]

    (set-binary-data! dataview 0 (seq data))

    (is= (index-of dataview "") 0 "Empty string")
    (is= (index-of dataview "Cut your") 26 "Match at start of 2nd line")
    (is= (index-of dataview "Common People") nil "No match")
    (is= (index-of dataview "above a shop" 26) nil "No match with offset")))
