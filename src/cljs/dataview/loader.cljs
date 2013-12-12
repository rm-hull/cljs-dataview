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
