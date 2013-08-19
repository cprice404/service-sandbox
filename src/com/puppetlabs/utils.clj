(ns com.puppetlabs.utils
  (:import [java.io StringWriter])
  (:use [clojure.string :only [split]]
        [clojure.pprint :only [pprint]]))

(defn parse-int
  "Parse a string `s` as an integer, returning nil if the string doesn't
  contain an integer."
  [s]
  {:pre  [(string? s)]
   :post [(or (integer? %) (nil? %))]}
  (try (Integer/parseInt s)
    (catch java.lang.NumberFormatException e
      nil)))

(defn pprint-to-string [x]
  (let [w (StringWriter.)]
    (pprint x w)
    (.toString w)))

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

;; Due to weird issues between JSSE and OpenSSL clients on some 1.7
;; jdks when using Diffie-Hellman exchange, we need to only enable
;; RSA-based ciphers.
;;
;; https://forums.oracle.com/forums/thread.jspa?messageID=10999587
;; https://issues.apache.org/jira/browse/APLO-287
;;
;; If not running on an affected JVM version, this is nil.
(defn acceptable-ciphers
  ([]
    (acceptable-ciphers (System/getProperty "java.version")))
  ([jvm-version]
    (let [known-good-version "1.7.0_05"]
      (if (pos? (compare-jvm-versions jvm-version known-good-version))
        ;; We're more recent than the last known-good version, and hence
        ;; are busted
        ["TLS_RSA_WITH_AES_256_CBC_SHA256"
         "TLS_RSA_WITH_AES_256_CBC_SHA"
         "TLS_RSA_WITH_AES_128_CBC_SHA256"
         "TLS_RSA_WITH_AES_128_CBC_SHA"
         "SSL_RSA_WITH_RC4_128_SHA"
         "SSL_RSA_WITH_3DES_EDE_CBC_SHA"
         "SSL_RSA_WITH_RC4_128_MD5"]))))

