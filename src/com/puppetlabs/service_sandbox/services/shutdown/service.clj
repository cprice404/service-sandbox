(ns com.puppetlabs.service-sandbox.services.shutdown.service
  (:require [com.puppetlabs.service-sandbox.services.shutdown.core :as core]
            [plumbing.map :as map]
            [plumbing.fnk.pfnk :as pfnk])
  (:use [plumbing.core :only [fnk]]
        [com.puppetlabs.utils :only [pprint-to-string]]))

(defn- add-shutdown-hooks-atom
  [graph]
  (assoc graph :shutdown-hooks (fnk [] (atom nil))))

(defn- maybe-add-shutdown-hook-input
  "If the original node provides :shutdown, then it has
  a dependency on the :shutdown-hooks atom that we've
  added to the graph, so we modify its input schema
  accordingly."
  [input-schema output-schema]
  (if (and
        (map? output-schema)
        (contains? output-schema :shutdown))
    (assoc input-schema :shutdown-hooks true)
    input-schema))

(defn- wrap-node-fn
  [log path node-fn]
  (pfnk/fn->fnk
    (fn [m]
      (let [result (node-fn m)]
        (when-let [shutdown (:shutdown result)]
          (log :info "Registering shutdown hook for service:" path)
          (swap! (:shutdown-hooks m) conj shutdown))
        result))
    (let [input-schema  (pfnk/input-schema node-fn)
          output-schema (pfnk/output-schema node-fn)]
      [(maybe-add-shutdown-hook-input input-schema output-schema)
        output-schema])))

(defn- wrap-node-fns
  [graph log]
  (map/map-leaves-and-path (partial wrap-node-fn log) graph))

(defn service-graph
  []
  {:shutdown-service (fnk [[:config-service config]
                           [:logging-service log]
                           shutdown-hooks]
                       (let [options (config :shutdown)]
                         (core/initialize log @shutdown-hooks)
                         {:wait-for-shutdown
                          (partial core/wait-for-shutdown log options)}))})

(defn register-hooks
  [app-graph log]
  (let [mapped-graph (-> app-graph
                       (merge (service-graph))
                       (wrap-node-fns log)
                       (add-shutdown-hooks-atom))]
    mapped-graph))
