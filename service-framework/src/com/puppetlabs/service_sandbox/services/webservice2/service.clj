(ns com.puppetlabs.service-sandbox.services.webservice2.service
  (:require [com.puppetlabs.service-sandbox.services.webservice2.core :as core])
  (:use [plumbing.core :only [fnk]]))

(defn service-graph
  [path]
  {:webservice2 (fnk ^{:output-schema {}}
                  [[:config-service config]
                   [:logging-service log]
                   [:http-service add-handler :as http-service]
                   db-service]
                  (let [options (config :webservice2)]
                      (core/initialize log http-service db-service path options)
                      {}))})