(ns cljs.dataview.ops)

; TODO - turn into protocol & extend DataView

(defn get-uint8 [data-view byte-offset]
  (. data-view (getUint8 byte-offset)))

(defn get-uint32-le [data-view byte-offset]
  (. data-view (getUint32 byte-offset true)))

(defn get-float32-le [data-view byte-offset]
  (. data-view (getFloat32 byte-offset true)))

(defn get-string [data-view byte-offset byte-length & [encoding]]
  (->>
    (range byte-offset (+ byte-offset byte-length))
    (map (comp char (partial get-uint8 data-view)))
    (apply str)))
