(ns dataview.protocols)

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
  (rewind! [this])
  (find! [this term]))

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
