(ns pluginwebservice3.service
  (:require [pluginwebservice3.core :as core])
  (:use [plumbing.core :only [fnk]]))

(defn initialize
  [log http options]
  (let [add-handler (http :add-handler)
        path (get options :path "/baz")]
    (log :info "initializing webservice3")
    (add-handler
      (partial core/baz-app log)
      core/baz-routes
      path)))

(defn shutdown
  [log]
  (log :info "Pluginwebservice3 shutting down!"))

(defn service-graph
  []
  {:webservice3 (fnk [[:config-service config]
                      [:logging-service log]
                      [:http-service add-handler :as http-service]]
                  (let [options (get config :webservice3 {})]
                    (initialize log http-service options))
                  {:shutdown (partial shutdown log)})})
