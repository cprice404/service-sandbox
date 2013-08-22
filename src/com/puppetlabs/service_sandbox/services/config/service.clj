(ns com.puppetlabs.service-sandbox.services.config.service
  (:require [com.puppetlabs.service-sandbox.services.config.core :as core])
  (:use [plumbing.core :only [fnk]]))

(defn service-graph
  []
  {:config-service (fnk [[:logging-service log]]
                     (core/initialize log)
                     {:config core/config
                      :shutdown (partial core/shutdown log)
                      })})