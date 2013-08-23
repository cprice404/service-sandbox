(ns service-plugin-jar.pluginwebservice4.service
  (:require [service-plugin-jar.pluginwebservice4.core :as core])
  (:use [plumbing.core :only [fnk]]))

(defn initialize
  [log http options]
  (let [add-handler (http :add-handler)
        path (get options :path "/bam")]
    (log :info "initializing webservice4")
    (add-handler
      (partial core/bam-app log)
      core/bam-routes
      path)))

(defn shutdown
  [log]
  (log :info "Pluginwebservice4 shutting down!"))

(defn service-graph
  []
  {:webservice4 (fnk [[:config-service config]
                      [:logging-service log]
                      [:http-service add-handler :as http-service]]
                  (let [options (get config :webservice3 {})]
                    (initialize log http-service options))
                  {:shutdown (partial shutdown log)})})
