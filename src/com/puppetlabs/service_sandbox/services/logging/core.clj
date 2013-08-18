(ns com.puppetlabs.service-sandbox.services.logging.core)

(defn log
  ([msg]
    (log :info [msg]))
  ([level msg]
    ;; TODO: hook in a real logger here, just using println for now.
    (println level msg)))
