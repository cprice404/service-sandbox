(ns com.puppetlabs.service-sandbox.services.webservice1.service
  (:require [com.puppetlabs.service-sandbox.services.webservice1.core :as core])
  (:use [plumbing.core :only [fnk]]))

(defn service-graph
  [path]
  {:webservice1 (fnk ^{:output-schema {}}
                  [[:config-service config]
                   [:logging-service log]
                   [:http-service add-handler :as http-service]]
                  (let [options (config :webservice1)]
                    (core/initialize log http-service path options)
                    {}))})
