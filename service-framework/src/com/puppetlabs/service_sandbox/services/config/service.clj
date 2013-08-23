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

(defn bootstrap
  [log args]
  (core/bootstrap log args)
  {:config core/config
   :service-graph (service-graph)})
