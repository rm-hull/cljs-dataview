(ns cljs.dataview.loader
  (:require [cljs.core.async :refer [chan]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn fetch-blob
  "Fetches the contents of a URL and returns a channel on which
   the binary data (as DataView object)"
  [url]
  (let [xhr     (js/XMLHttpRequest.)
        chan    (chan)
        handler (fn [event]
                  (go
                    (>! chan (js/DataView. (.-response xhr)))))]
    (. xhr (open "GET" url true))
    (set! (.-responseType xhr) "arraybuffer")
    (set! (.-onload xhr) handler)
    (.send xhr)
    chan))


;(comment

; Note: CORS -- http://www.w3.org/wiki/CORS#XMLHttpRequest_.28XHR.29
(def remote-url "https://github.com/rm-hull/wireframes/raw/master/doc/gallery/torus.stl")
(def local-url "http://localhost/~rhu/test/torus.stl")

(let [chan (fetch-blob local-url)]
  (go
    (->
      (<! chan)
      (cljs.dataview.ops/get-string 0 80)
      (partial .log js/console))))



;)

