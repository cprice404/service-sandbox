(ns com.puppetlabs.service-sandbox.ring.middleware
  (:use [com.puppetlabs.utils :only [pprint-to-string]]))

(defn wrap-with-request-logger
  [handler log]
  (fn [req]
    (log :info "Got a request:")
    (log :info (pprint-to-string req))
    (log :info "---------------")
    (handler req)))