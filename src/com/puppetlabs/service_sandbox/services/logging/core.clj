(ns com.puppetlabs.service-sandbox.services.logging.core
  (:require [clojure.string :as s]))

(defn log
  ([msg]
    (log :info [msg]))
  ([level & msgs]
    ;; TODO: hook in a real logger here, just using println for now.
    (println level (s/join " " msgs))))
