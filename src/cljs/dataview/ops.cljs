(ns cljs.dataview.ops)

; TODO - turn into protocol & extend DataView

(defn get-uint8
  "Gets the value of the 8-bit unsigned byte at the specified byte offset
   from the start of the data view. There is no alignment constraint;
   multi-byte values may be fetched from any offset."
  [data-view byte-offset]
  (. data-view (getUint8 byte-offset)))

(defn get-uint16-le
  "Gets the value of the 16-bit little-endian unsigned short integer at the
   specified byte offset from the start of the data view. There is no
   alignment constraint; multi-byte values may be fetched from any offset."
  [data-view byte-offset]
  (. data-view (getUint16 byte-offset true)))

(defn get-uint32-le
  "Gets the value of the 32-bit little-endian unsigned integer at the
   specified byte offset from the start of the data view. There is no
   alignment constraint; multi-byte values may be fetched from any offset."
  [data-view byte-offset]
  (. data-view (getUint32 byte-offset true)))

(defn get-float32-le
  "Gets the value of the 32-bit little-endian IEEE floating-point number
   at the specified byte offset from the start of the data view. There is no
   alignment constraint; multi-byte values may be fetched from any offset."
  [data-view byte-offset]
  (. data-view (getFloat32 byte-offset true)))

(def get-string
  "Given a data-view, a byte offset, a length (and optionally an encoding -
   note that only UTF-8 is currently supported), extracts a string from the
   underlying byte buffer."
  (let [utf? #{:utf8 :utf-8 :UTF8 :UTF-8}]
    (fn [data-view byte-offset byte-length & [encoding]]
      (let [s (->>
                (range byte-offset (+ byte-offset byte-length))
                (map (comp char (partial get-uint8 data-view)))
                (apply str))]
        (if (utf? (keyword encoding))
          (-> s js/escape js/decodeURIComponent)
          s)))))
