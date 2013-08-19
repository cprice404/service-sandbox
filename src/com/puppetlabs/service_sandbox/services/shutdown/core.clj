(ns com.puppetlabs.service-sandbox.services.shutdown.core)

(defn initialize
  [log]
  ;; Here we might do something like open a socket to
  ;; accept shutdown requests.
  (log :info "Initializing shutdown service"))

(defn wait-for-shutdown
  [log options]
  ;; Here we'd simply block until whatever shutdown conditions
  ;; we have (such as having received a shutdown request on
  ;; the socket) are met.  Faking it for now with a sleep.
  (let [delay (get options :shutdown-delay 5)]
    (log :info (str "Shutting down in " delay " seconds"))
    (Thread/sleep (* delay 1000))

    ;; TODO: need to find a way to register things that need
    ;; to be shut down; webserver, thread pools, etc.S
    (log :info "Bye!")))
