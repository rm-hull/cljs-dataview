(ns dataview.test-helpers)

(defn create-dataview [size]
  (->
    (js/ArrayBuffer. size)
    (js/DataView.)))

(defn set-binary-data! [data-view offset data]
  (doseq [[i n] (zipmap (iterate inc offset) data)]
    (.setUint8 data-view i (if (string? n) (.charCodeAt n 0) n))))

(defn set-float-data! [data-view offset data]
  (doseq [[i n] (zipmap (iterate (partial + 4) offset) data)]
    (. data-view (setFloat32  i n true))))

(defn tee [tee-fn x]
  (tee-fn x)
  x)
