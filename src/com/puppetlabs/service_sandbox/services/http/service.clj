(ns com.puppetlabs.service-sandbox.services.http.service
  (:require [com.puppetlabs.service-sandbox.services.http.core :as core])
  (:use [plumbing.core :only [fnk]]))

(defn service-graph
  []
  {:http-service
    (fnk ^{:output-schema {:add-handler true}}
      [[:config-service config] [:logging-service log]]
      (let [options (config :http)]
        (core/initialize log options)
        {:add-handler (partial core/add-handler log)}))})


