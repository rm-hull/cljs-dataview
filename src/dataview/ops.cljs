(ns dataview.ops)

(defprotocol IReader
  (read-utf8-string [this delimiters])
  (read-fixed-string [this length])
  (read-byte [this])
  (read-uint16-le [this])
  (read-uint32-le [this])
  (read-float32-le [this])
  (eod? [this]))

(defprotocol IRandomAccess
  (tell [this])
  (advance! [this delta])   ; cf. get-and-add
  (seek! [this new-offset])
  (rewind! [this]))

(defprotocol IByteIndexed
  (byte-length [this])
  (get-byte [this offset]))

(defprotocol ILittleEndian
  (get-uint16-le [this offset])
  (get-uint32-le [this offset])
  (get-float32-le [this offset]))

(extend-type js/DataView
  IByteIndexed
  (byte-length [data-view]
    (.-byteLength data-view))
  (get-byte [data-view offset]
    (.getUint8 data-view offset))

  ILittleEndian
  (get-uint16-le [data-view offset]
    (.getUint16 data-view offset true))
  (get-uint32-le [data-view offset]
    (.getUint32  data-view offset true))
  (get-float32-le [data-view offset]
    (.getFloat32  data-view offset true)))

(extend-type string
  IByteIndexed
  (byte-length [string]
    (.-length string))
  (get-byte [string offset]
    (.charCodeAt string offset)))

(defn get-string
  "Given a data-view, a byte offset, a length (and optionally an encoding -
   note that only ASCII is currently supported), extracts a string from the
   underlying byte buffer."
  [^IByteIndexed obj byte-offset & {:keys [delimiters length] :as opts}]
  (if (and delimiters length)
    (throw (js/Error. "Cannot support :length and :delimiters at the same time"))
    (let [take-fn (fn [cs] (if length
                             (take length cs)
                             (take-while #(not (delimiters %)) cs)))]
      (->>
        (range byte-offset (byte-length obj))
        (map (comp char (partial get-byte obj)))
        (take-fn)
        (apply str)))))

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
  (let [c (read-byte reader)]
    (.fromCharCode js/String
      (cond
        (< c 128)
          (octet-nibbles c)

        (and (>= c 192) (< c 224))
          (octet-nibbles
            c (read-byte reader))

        :else
          (octet-nibbles
            c (read-byte reader) (read-byte reader))))))

(defn can-read? [data-view offset bytes-to-read]
  (<= (+ offset bytes-to-read) (byte-length data-view)))

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

(defn create-reader [^IByteIndexed obj]
  (let [seeker (create-seeker 0)
        apply-offset (fn [bytes-to-read fn]
                       (when (can-read? obj (tell seeker) bytes-to-read)
                         (let [offset (advance! seeker bytes-to-read)]
                           (try
                             (fn offset)
                             (catch js/Error e
                               (seek! seeker offset)
                               (throw e))))))]
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
        (apply-offset
          length
          #(get-string obj % :length length)))

      (read-byte [this]
        (apply-offset 1 #(get-byte obj %)))

      (read-uint16-le [this]
        (apply-offset 2 #(get-uint16-le obj %)))

      (read-uint32-le [this]
        (apply-offset 4 #(get-uint32-le obj %)))

      (read-float32-le [this]
        (apply-offset 4 #(get-float32-le obj %)))

      (eod? [this]
        (>= (tell seeker) (byte-length obj)))

      IRandomAccess
      (tell [this]
        (tell seeker))

      (advance! [this delta]
        (advance! seeker delta))

      (seek! [this new-offset]
        (seek! seeker new-offset))

      (rewind! [this]
        (rewind! seeker)))))
