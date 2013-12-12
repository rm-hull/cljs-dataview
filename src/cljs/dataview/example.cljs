(ns cljs.dataview.example
  (:require [cljs.dataview.loader :refer [fetch-blob]]
            [cljs.dataview.ops :refer [get-string]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

; Note: CORS -- http://www.w3.org/wiki/CORS#XMLHttpRequest_.28XHR.29
(def remote-url "https://github.com/rm-hull/wireframes/raw/master/doc/gallery/torus.stl")
(def local-url "http://localhost/~rhu/test/torus.stl")

(go
  (.log js/console
    (->
      (<! (fetch-blob local-url))
      (get-string 0 80 :utf-8))))

