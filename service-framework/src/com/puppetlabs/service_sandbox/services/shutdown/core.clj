(ns com.puppetlabs.service-sandbox.services.shutdown.core)

(def hooks (atom nil))

(defn initialize
  [log shutdown-hooks]
  ;; Here we might do something like open a socket to
  ;; accept shutdown requests.
  (log :info "Initializing shutdown service")
  (reset! hooks shutdown-hooks)
  (log :info "Registered"
    (count shutdown-hooks) "shutdown hooks."))

(defn shutdown
  []
  (doseq [f @hooks]
    (f)))

(defn wait-for-shutdown
  [log options]
  ;; Here we'd simply block until whatever shutdown conditions
  ;; we have (such as having received a shutdown request on
  ;; the socket) are met.  Faking it for now with a sleep
  ;; and an explicit call to our shutdown function.
  (let [delay (get options :shutdown-delay 5)]
    (log :info (str "Shutting down in " delay " seconds"))
    (Thread/sleep (* delay 1000))

    (log :info (str "Shutting down now."))
    (shutdown)
    (log :info "Bye!")))
