(ns cljs.dataview.example
  (:require [cljs.dataview.loader :refer [fetch-blob]]
            [cljs.dataview.ops :refer [create-reader read-string read-float32-le
                                       read-uint16-le read-uint32-le]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(set! *print-fn*
  (fn [s]
    (.log js/console s)))

; Note: CORS -- http://www.w3.org/wiki/CORS#XMLHttpRequest_.28XHR.29
; Github slings back a 301, and corsproxy just forwards that so the
; remote-url doesnt quite work a.t.m.
(def remote-url "http://www.corsproxy.com/github.com/rm-hull/wireframes/raw/master/doc/gallery/torus.stl")
(def local-url "http://localhost/~rhu/test/torus.stl")

;; Notice how close (in structure) this is to the similar gloss definition:
;; https://github.com/rm-hull/wireframes/blob/master/src/wireframes/shapes/stl_loader.clj#L11
(defn point-spec [reader]
  (array-map
    :x (read-float32-le reader)
    :y (read-float32-le reader)
    :z (read-float32-le reader)))

(defn triangle-spec [reader]
  (array-map
    :normal (point-spec reader)
    :points (repeatedly 3 #(point-spec reader))
    :attributes (read-uint16-le reader)))

(defn stl-spec [reader]
  (array-map
    :header (read-string reader 80 :ascii)
    :triangles (repeatedly
                 (read-uint32-le reader) ; <== triangle-count
                 #(triangle-spec reader))))

(go
  (->
    (<! (fetch-blob local-url))
    (create-reader)
    (stl-spec)
    (println)))
