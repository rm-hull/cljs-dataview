(ns dataview.loader
  (:require [cljs.core.async :refer [>! chan close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn fetch-blob
  "Fetches the contents of a URL and returns a channel on which
   the binary data is parked (as DataView object)"
  [url]
  (let [xhr     (js/XMLHttpRequest.)
        chan    (chan 1)
        handler (fn [event]
                  (go
                    (>! chan (js/DataView. (.-response xhr)))
                    (close! chan)))]
    (. xhr (open "GET" url true))
    (set! (.-responseType xhr) "arraybuffer")
    (set! (.-onload xhr) handler)
    (.send xhr)
    chan))

(defn fetch-image
  "Fetches an image from a URL and returns a channel on which the
   the completed image is partked. Note: the image be hosted on
   the same domain unless a CORS-busting proxy is used."
  [url]
  (let [img     (js/Image.)
        chan    (chan 1)
        handler (fn []
                  (go
                    (>! chan img)
                    (close! chan)))]
    (set! (.-onload img) handler)
    (set! (.-crossOrigin img) "anonymous")
    (set! (.-src img) url)
    chan))

(defn fetch-text
  "Fetches the contents of a URL and returns a channel on which
   the text data is parked (as a string object)"
  [url]
  (let [xhr     (js/XMLHttpRequest.)
        chan    (chan 1)
        handler (fn [event]
                  (go
                    (>! chan (.-response xhr))
                    (close! chan)))]
    (. xhr (open "GET" url true))
    (set! (.-onload xhr) handler)
    (.send xhr)
    chan))
