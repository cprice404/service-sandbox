(ns com.puppetlabs.service-sandbox.services.http.core
  (:use [compojure.core :only [context]])
  (:require [com.puppetlabs.service-sandbox.jetty :as jetty]))

(def server (atom nil))

(defn initialize
  [log options]
  (log :info "Initializing webserver")
  (reset! server (jetty/run-dynamic-jetty (merge options {:join? false}))))

(defn add-handler
  [log app-fn routes path]
  (log :info "Adding handler" path)
  (let [context-routes  (context path [] routes)
        context-app     (app-fn context-routes)]
    (jetty/add-handler @server context-app path)))