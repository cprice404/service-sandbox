(ns com.puppetlabs.service-sandbox.services.shutdown.core)

(defn initialize
  [log]
  (log :info "Initializing shutdown service"))

(defn wait-for-shutdown
  [log options]
  (let [delay (get options :shutdown-delay 5)]
    (log :info (str "Shutting down in " delay " seconds"))
    (Thread/sleep (* delay 1000))
    (log :info "Bye!")))
