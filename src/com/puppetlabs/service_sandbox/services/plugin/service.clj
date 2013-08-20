(ns com.puppetlabs.service-sandbox.services.plugin.service
  (:require [com.puppetlabs.service-sandbox.services.plugin.core :as core])
  (:use [plumbing.core :only [fnk]]))

(defn service-graph
  []
  {:plugin-service
    (fnk [[:bootstrap-service app-graph]
          [:config-service config]
          [:logging-service log]]
      (core/initialize log config (app-graph))
      {})})
