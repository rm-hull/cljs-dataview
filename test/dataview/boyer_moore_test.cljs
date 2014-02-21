(ns dataview.boyer-moore-test
  (:use-macros [cljs-test.macros :only [deftest is= is]])
  (:require [cljs-test.core :as test]
            [dataview.boyer-moore :refer [index-of]]))

(deftest boyer-moore
  (is= (index-of "Hello world" "") 0 "Empty string")
  (is= (index-of "Hello world" "" 3) 3 "Empty offset")
  (is= (index-of "Hello world" "Hello") 0 "Match at zero")
  (is= (index-of "Hello world" "world") 6 "Match at pos 6")
  (is= (index-of "Hello world" "o wo") 4 "Match at pos 4")
  (is= (index-of "Hello world" "not") nil "No match")
  )
