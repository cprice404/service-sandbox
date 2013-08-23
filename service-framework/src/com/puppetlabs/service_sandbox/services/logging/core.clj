(ns com.puppetlabs.service-sandbox.services.logging.core
  (:require [clojure.string :as s]))

(defn initialize
  [config]
  ;; TODO set up clojure logging / log4j / whatevs
  )

(defn log
  ([msg]
    (log :info [msg]))
  ([level & msgs]
    ;; TODO: hook in a real logger here, just using println for now.
    (println level (s/join " " msgs))))
