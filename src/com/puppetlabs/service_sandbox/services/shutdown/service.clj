(ns com.puppetlabs.service-sandbox.services.shutdown.service
  (:require [com.puppetlabs.service-sandbox.services.shutdown.core :as core])
  (:use [plumbing.core :only [fnk]]))

(defn service-graph
  []
  {:shutdown-service (fnk [[:config-service config] [:logging-service log]]
                       (let [options (config :shutdown)]
                         (core/initialize log)
                         {:wait-for-shutdown (partial core/wait-for-shutdown log options)}))})