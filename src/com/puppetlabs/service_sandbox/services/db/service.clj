(ns com.puppetlabs.service-sandbox.services.db.service
;  (:require [com.puppetlabs.service-sandbox.services.db.core :as core])
  (:use [plumbing.core :only [fnk]]))

(defn service-graph
  []
  {:db-service
    (fnk [[:logging-service log]]
      (log :info "Initializing db service")
      {:db {:fake-value [{:count 1234}]}})})
