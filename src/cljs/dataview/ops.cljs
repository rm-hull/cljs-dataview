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
    (fn [data-view byte-offset & {:keys [delimiters length encoding] :as opts}]
      (when (and delimiters length)
        (throw (js/Error. "Cannot support :length and :delimiters at the same time")))
      (let [take-fn (fn [cs] (if length
                               (take length cs)
                               (take-while #(not (delimiters %)) cs)))
            s (->>
                (iterate inc byte-offset)
                (map (comp char (partial get-uint8 data-view)))
                (take-fn)
                (apply str))]
        (if (utf? (keyword encoding))
          (-> s js/escape js/decodeURIComponent)
          s)))))

(defprotocol IReader
  (read-delimited-string [this delimiters encoding])
  (read-fixed-string [this length encoding])
  (read-uint8 [this])
  (read-uint16-le [this])
  (read-uint32-le [this])
  (read-float32-le [this]))

(defprotocol IRandomAccess
  (tell [this])
  (advance! [this delta])   ; cf. get-and-add
  (seek! [this new-offset])
  (rewind! [this]))

(defn- create-seeker [initial-offset]
  (let [index (atom initial-offset)]
    (reify
      IRandomAccess
      (tell [this]
        (deref index))

      (advance! [this delta]
        (- (swap! index + delta) delta))

      (seek! [this new-offset]
        (reset! index new-offset))

      (rewind! [this]
        (seek! this 0)))))

(defn create-reader [data-view]
  (let [seeker (create-seeker 0)]
    (reify
      IReader

      (read-delimited-string [this delimiters encoding]
        (let [offset (tell seeker)
              data   (get-string data-view offset :delimiters delimiters :encoding encoding)]
          (advance! seeker (inc (count data))) ; cater for single-character delimiters only
          data))

      (read-fixed-string [this length encoding]
        (let [offset (advance! seeker length)]
          (get-string data-view offset :length length :encoding encoding)))

      (read-uint8 [this]
        (let [offset (advance! this 1)]
          (get-uint8 data-view offset)))

      (read-uint16-le [this]
        (let [offset (advance! this 2)]
          (get-uint16-le data-view offset)))

      (read-uint32-le [this]
        (let [offset (advance! this 4)]
          (get-uint32-le data-view offset)))

      (read-float32-le [this]
        (let [offset (advance! this 4)]
          (get-float32-le data-view offset)))

      IRandomAccess
      (tell [this]
        (tell seeker))

      (advance! [this delta]
        (advance! seeker delta))

      (seek! [this new-offset]
        (seek! seeker new-offset))

      (rewind! [this]
        (rewind! seeker)))))
