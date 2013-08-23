(ns com.puppetlabs.service-sandbox.services.http.service
  (:require [com.puppetlabs.service-sandbox.jetty :as jetty])
  (:use [plumbing.core :only [fnk]]
        [compojure.core :only [context]]))

(def server (atom nil))

(defn initialize
  [log options]
  (log :info "Initializing webserver")
  (reset! server
    (jetty/run-dynamic-jetty
      (merge options {:join? false}))))

(defn add-handler
  [log app-fn routes path]
  (log :info "Adding handler" path)
  (let [context-routes  (context path [] routes)
        context-app     (app-fn context-routes)]
    (jetty/add-handler @server context-app path)))

(defn shutdown
  [log]
  (log :info "Shutting down web server...")
  (let [s @server]
    (jetty/shutdown s)
    (jetty/join s))
  (log :info "Web server shutdown complete."))

(defn service-graph
  []
  {:http-service
    (fnk ^{:output-schema
           {:add-handler  true
            :shutdown     true}}
      [[:config-service config] [:logging-service log]]
      (let [options (config :http)]
        (initialize log options)
        {:add-handler (partial add-handler log)
         :shutdown    (partial shutdown log)}))})


