(ns dataview.boyer-moore)

; ported directly from the Java version at wikipedia:
; http://en.wikipedia.org/wiki/Boyer_moore#Implementations

(defn- char= [needle i j]
  (= (.charCodeAt needle i) (.charCodeAt needle j)))

(defn- prefix?
  "Is needle[p:end] a prefix of needle?"
  [needle p]
  (let [len (count needle)]
    (loop [i p
           j 0]
      (cond
        (= i len) true
        (not (char= needle (inc i) (inc j))) false
        :else (recur (inc i) (inc j))))))

(defn- suffix-length
  "Returns the maximum length of the substring ends at p and is a suffix"
  [needle p]
  (loop [i p
         j (dec (count needle))
         len 0]
    (if (and (> i 0) (char= needle i j))
      (recur
        (dec i)
        (dec j)
        (inc len))
      len)))

(defn- make-char-table
  "Makes the jump table based on the mismatched character information"
  [needle]
  (let [len (count needle)]
    (loop [i 0
           table (vec (repeat 256 len))]
      (if-not (< i (dec len))
        table
        (recur
          (inc i)
          (assoc
            table
            (.charCodeAt needle i)
            (- len 1 i)))))))

(defn- calc-prefixes [needle]
  (let [len (count needle)]
    (loop [i (dec len)
           last-posn len
           table (vec (repeat len 0))]
      (if-not (>= i 0)
        table
        (let [last-posn (if (prefix? needle i) i last-posn)]
          (recur
            (dec i)
            last-posn
            (assoc
              table
              (- len 1 i)
              (+ last-posn (- i) len -1))))))))

(defn- make-offset-table
  "Makes the jump table based on the scan offset which mismatch occurs"
  [needle]
  (let [len (count needle)]
    (loop [i 0
           table (calc-prefixes needle)]
      (if-not (<  i (dec len))
        table
        (let [slen (suffix-length needle i)]
          (recur
            (inc i)
            (assoc table slen (+ len -1 (- i) slen))))))))

(defn index-of
  "Returns the index with the string of the first occurrence of the
   specified substring. If it is not a substring, return nil.

   haystack - the string to be scanned
   needle   - the target string to search"
  ([haystack needle] (index-of haystack needle 0))
  ([haystack needle offset]
    (let [len (count needle)
          m1 (dec len)]
      (if (zero? len)
        offset
        (let [char-table (make-char-table needle)
              offset-table (make-offset-table needle)
              calc-offset (fn [i j] (+ offset i
                                       (Math/max
                                         (get offset-table (- m1 j))
                                         (get char-table (.charCodeAt haystack i)))))]
          (loop [i (+ offset m1)
                 j m1]
            (cond
              (>= i (count haystack)) nil
              (zero? j) i
              (= (.charCodeAt needle j) (.charCodeAt haystack i)) (recur (dec i) (dec j))
              :else (recur (calc-offset i j) m1))))))))
