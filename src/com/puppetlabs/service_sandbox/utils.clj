(ns com.puppetlabs.service-sandbox.utils
  (:use [clojure.string :only (split)]))




(defn parse-int
  "Parse a string `s` as an integer, returning nil if the string doesn't
  contain an integer."
  [s]
  {:pre  [(string? s)]
   :post [(or (integer? %) (nil? %))]}
  (try (Integer/parseInt s)
    (catch java.lang.NumberFormatException e
      nil)))

;; Comparison of JVM versions

(defn compare-jvm-versions
  "Same behavior as `compare`, but specifically for JVM version
   strings.  Because Java versions don't follow semver or anything, we
   need to do some massaging of the input first:

  http://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html"
  [a b]
  {:pre  [(string? a)
          (string? b)]
   :post [(number? %)]}
  (let [parse #(mapv parse-int (-> %
                                 (split #"-")
                                 (first)
                                 (split #"[\\._]")))]
    (compare (parse a) (parse b))))
