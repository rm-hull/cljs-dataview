(ns binary-stl
  (:require
    [dataview.loader :refer [fetch-blob]]
    [dataview.ops :refer [create-reader]]
    [dataview.protocols :as proto])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

; Note: CORS -- http://www.w3.org/wiki/CORS#XMLHttpRequest_.28XHR.29
; Github slings back a 301, and corsproxy just forwards that so the
; remote-url doesnt quite work a.t.m.
(def remote-url "http://programming-enchiladas.destructuring-bind.org/proxy?url=https://raw.github.com/rm-hull/wireframes/master/doc/gallery/torus.stl")
(def local-url "http://localhost/~rhu/test/torus.stl")

;; Notice how close (in structure) this is to the similar gloss definition:
;; https://github.com/rm-hull/wireframes/blob/master/src/wireframes/shapes/stl_loader.clj#L11
(defn point-spec [reader]
  (array-map
    :x (proto/read-float32-le reader)
    :y (proto/read-float32-le reader)
    :z (proto/read-float32-le reader)))

(defn triangle-spec [reader]
  (array-map
    :normal (point-spec reader)
    :points (doall (repeatedly 3 #(point-spec reader)))
    :attributes (proto/read-uint16-le reader)))

(defn stl-spec [reader]
  (array-map
    :header (proto/read-fixed-string reader 80)
    :triangles (doall (repeatedly
                 (proto/read-uint32-le reader)
                 #(triangle-spec reader)))))

(go
  (->
    (<! (fetch-blob remote-url))
    (create-reader)
    (stl-spec)
    (time)
    (println)))
