(ns dataview.ops
  (:require
    [dataview.protocols :as proto]
    [dataview.boyer-moore :refer [index-of]]))

(defn get-string
  "Given a data-view, a byte offset, a length (and optionally an encoding -
   note that only ASCII is currently supported), extracts a string from the
   underlying byte buffer."
  [^proto/IByteIndexed obj byte-offset & {:keys [delimiters length] :as opts}]
  (if (and delimiters length)
    (throw (js/Error. "Cannot support :length and :delimiters at the same time"))
    (let [take-fn (fn [cs] (if length
                             (take length cs)
                             (take-while #(not (delimiters %)) cs)))]
      (->>
        (range byte-offset (proto/byte-length obj))
        (map (comp char (partial proto/get-byte obj)))
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
  (let [c (proto/read-byte reader)]
    (.fromCharCode js/String
      (cond
        (< c 128)
          (octet-nibbles c)

        (and (>= c 192) (< c 224))
          (octet-nibbles
            c (proto/read-byte reader))

        :else
          (octet-nibbles
            c (proto/read-byte reader) (proto/read-byte reader))))))

(defn can-read? [data-view offset bytes-to-read]
  (<= (+ offset bytes-to-read) (proto/byte-length data-view)))

(defn- create-seeker [initial-offset obj]
  (let [index (atom initial-offset)]
    (reify
      proto/IRandomAccess
      (tell [this]
        (deref index))

      (advance! [this delta]
        (- (swap! index + delta) delta))

      (seek! [this new-offset]
        (reset! index new-offset))

      (rewind! [this]
        (proto/seek! this 0))

      (find! [this term]
        (when-let [new-offset (index-of obj term)]
          (proto/seek! this new-offset))))))

(defn create-reader [^proto/IByteIndexed obj]
  (let [seeker (create-seeker 0 obj)
        apply-offset (fn [bytes-to-read fn]
                       (when (can-read? obj (proto/tell seeker) bytes-to-read)
                         (let [offset (proto/advance! seeker bytes-to-read)]
                           (try
                             (fn offset)
                             (catch js/Error e
                               (proto/seek! seeker offset)
                               (throw e))))))]
    (reify
      proto/IReader
      (read-utf8-string [this delimiters]
        (when-not (proto/eod? this)
          (loop [data nil
                 next-char nil]
            (if (or (proto/eod? this) (delimiters next-char))
              (str data next-char)
              (recur
                (str data next-char)
                (utf8-decode this))))))

      (read-fixed-string [this length]
        (apply-offset
          length
          #(get-string obj % :length length)))

      (read-byte [this]
        (apply-offset 1 #(proto/get-byte obj %)))

      (read-uint16-le [this]
        (apply-offset 2 #(proto/get-uint16-le obj %)))

      (read-uint32-le [this]
        (apply-offset 4 #(proto/get-uint32-le obj %)))

      (read-float32-le [this]
        (apply-offset 4 #(proto/get-float32-le obj %)))

      (eod? [this]
        (>= (proto/tell seeker) (proto/byte-length obj)))

      proto/IRandomAccess
      (tell [this]
        (proto/tell seeker))

      (advance! [this delta]
        (proto/advance! seeker delta))

      (seek! [this new-offset]
        (proto/seek! seeker new-offset))

      (rewind! [this]
        (proto/rewind! seeker))

      (find! [this term]
        (proto/find! seeker term)))))
