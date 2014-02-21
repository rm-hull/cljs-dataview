(ns dataview.ops)

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

(defn byte-length
  "The size of the data view, expressed in bytes"
  [data-view]
  (.-byteLength data-view))

(defn get-string
  "Given a data-view, a byte offset, a length (and optionally an encoding -
   note that only ASCII is currently supported), extracts a string from the
   underlying byte buffer."
  [data-view byte-offset & {:keys [delimiters length] :as opts}]
  (if (and delimiters length)
    (throw (js/Error. "Cannot support :length and :delimiters at the same time"))
    (let [take-fn (fn [cs] (if length
                             (take length cs)
                             (take-while #(not (delimiters %)) cs)))]
      (->>
        (range byte-offset (byte-length data-view))
        (map (comp char (partial get-uint8 data-view)))
        (take-fn)
        (apply str)))))

(defn can-read? [data-view offset bytes-to-read]
  (<= (+ offset bytes-to-read) (byte-length data-view)))

(defprotocol IReader
  (read-utf8-string [this delimiters])
  (read-fixed-string [this length])
  (read-uint8 [this])
  (read-uint16-le [this])
  (read-uint32-le [this])
  (read-float32-le [this])
  (eod? [this]))


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

(defn- octet-nibbles
  ([c] c)
  ([c1 c2]
    (bit-or
      (bit-shift-left (bit-and c1 31) 6)
      (bit-and c2 63)))
  ([c1 c2 c3]
    (bit-or
      (bit-shift-left (bit-and c1 15) 12)
      (octet-nibbles c2 c3))))

(defn utf8-decode
  "Reads upto 3 bytes from the reader in order to reconstruct
   a single unicode character from it's UTF-8 representation.

   Does not support surrogate pairs (4-byte encodings)."
  [reader]
  (let [c (read-uint8 reader)]
    (.fromCharCode js/String
      (cond
        (< c 128)
          (octet-nibbles c)

        (and (>= c 192) (< c 224))
          (octet-nibbles
            c (read-uint8 reader))

        :else
          (octet-nibbles
            c (read-uint8 reader) (read-uint8 reader))))))

(defn create-reader [data-view]
  (let [seeker (create-seeker 0)]
    (reify
      IReader
      (read-utf8-string [this delimiters]
        (when-not (eod? this)
          (loop [data nil
                 next-char nil]
            (if (or (eod? this) (delimiters next-char))
              (str data next-char)
              (recur
                (str data next-char)
                (utf8-decode this))))))

      (read-fixed-string [this length]
        (when (can-read? data-view (tell seeker) length)
          (let [offset (advance! seeker length)]
            (get-string data-view offset :length length))))

      (read-uint8 [this]
        (when (can-read? data-view (tell seeker) 1)
          (let [offset (advance! this 1)]
            (get-uint8 data-view offset))))

      (read-uint16-le [this]
        (when (can-read? data-view (tell seeker) 2)
          (let [offset (advance! this 2)]
            (get-uint16-le data-view offset))))

      (read-uint32-le [this]
        (when (can-read? data-view (tell seeker) 4)
          (let [offset (advance! this 4)]
            (get-uint32-le data-view offset))))

      (read-float32-le [this]
        (when (can-read? data-view (tell seeker) 4)
          (let [offset (advance! this 4)]
            (get-float32-le data-view offset))))

      (eod? [this]
        (>= (tell seeker) (byte-length data-view)))

      IRandomAccess
      (tell [this]
        (tell seeker))

      (advance! [this delta]
        (advance! seeker delta))

      (seek! [this new-offset]
        (seek! seeker new-offset))

      (rewind! [this]
        (rewind! seeker)))))
