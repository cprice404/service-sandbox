(ns com.puppetlabs.service-sandbox.services.monitor.service
  (:use [compojure.core :only [defroutes GET]]
        [com.puppetlabs.service-sandbox.ring.middleware :only [wrap-with-request-logger]]
        [plumbing.core :only [fnk]]
        [com.puppetlabs.utils :only [pprint-to-string]]
        [clojure.walk :only [walk]])
  (:require [compojure.route :as route]
            [com.puppetlabs.map :as map]
            [com.puppetlabs.service-sandbox.services.utils :as svc-utils]))

(def app-graph (atom nil))

(defn register-final-graph
  [sorted-graph compiled-graph]
  (reset! app-graph {:sorted-graph sorted-graph :compiled-graph compiled-graph}))

(defn statuses
  [services compiled-graph]
  (reduce
    (fn [m s]
      (if-let [status-fn (get-in compiled-graph (conj s :status))]
        (assoc-in m s (status-fn))
        (assoc-in m s :unknown)))
    {}
    services))

(defn ordered-status-map
  [services statuses]
  (map (fn [s] [s (get-in statuses s)]) services))

(defn json-status-tree
  [{:keys [sorted-graph compiled-graph]}]
  ;; TODO: note, we're jumping through some hoops here in order to
  ;; keep the service status output in the same order as the graph
  ;; topology.
  (let [services (map/map-leaves-and-path
                    (fn [k v] k)
                    sorted-graph)]
    (pprint-to-string
      (ordered-status-map
        services
        (statuses services compiled-graph)))))

(defroutes monitor-routes
  (GET "/" [] {:body (json-status-tree @app-graph)})
  (route/not-found "not found"))

(defn monitor-app
  [log routes]
  (-> routes
    (wrap-with-request-logger log)))


(defn initialize
  [log http options path]
  (log :info "Initializing monitor service.")
  (let [add-handler (http :add-handler)]
    (add-handler (partial monitor-app log) monitor-routes path)))

(defn service-graph
  [path]
  {:monitor-service (fnk ^{:output-schema {}}
                      [[:config-service config]
                       [:logging-service log]
                       [:http-service add-handler :as http-service]]
                      (let [options (config :monitor)]
                        (initialize log http-service options path))
                      {})})
