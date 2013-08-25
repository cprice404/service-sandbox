(ns com.puppetlabs.service-sandbox.services.shutdown.service
  (:require [com.puppetlabs.service-sandbox.services.shutdown.core :as core]
            [plumbing.fnk.pfnk :as pfnk]
            [com.puppetlabs.map :as map])
  (:use [plumbing.core :only [fnk]]
        [com.puppetlabs.utils :only [pprint-to-string]]))

(defn- add-shutdown-hooks-atom
  [graph]
  (assoc graph :shutdown-hooks (fnk ^{:output-schema {}} [] (atom nil))))

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
  (map/walk-leaves-and-path (partial wrap-node-fn log) graph))

(defn service-graph
  []
  {:shutdown-service (fnk ^{:output-schema {:wait-for-shutdown true}}
                       [[:config-service config]
                        [:logging-service log]
                        shutdown-hooks]
                       (let [options (config :shutdown)]
                         (core/initialize log @shutdown-hooks)
                         {:wait-for-shutdown
                          (partial core/wait-for-shutdown log options)}))})

(defn services-with-hooks
  [g]
  (let [nodes     (map/map-leaves-and-path
                    (fn [path node-fn]
                      [path node-fn])
                    g)
        filtered  (filter
                    (fn [[path node-fn]]
                      (let [input-schema (pfnk/input-schema node-fn)]
                        (contains? input-schema :shutdown-hooks)))
                    nodes)]
    (map first filtered)))

(defn update-input-schema
  [svc-graph services]
  (let [svc-schemas (apply map/deep-merge
                      (map
                        #(reduce (fn [x y] {y x}) (reverse (conj % true)))
                        services))]
    (println "Services with hooks:" svc-schemas)
    (update-in svc-graph [:shutdown-service]
      (fn [node-fn]
        (let [input-schema (pfnk/input-schema node-fn)
              output-schema (pfnk/output-schema node-fn)]
          (pfnk/fn->fnk
            (fn [m]
              (node-fn m))
            [(merge input-schema svc-schemas)
             output-schema]))))))

(defn add-shutdown-service-graph
  [g]
  (merge g
    (update-input-schema
      (service-graph)
      (services-with-hooks g))))

(defn register-hooks
  [app-graph log]
  (let [mapped-graph (-> app-graph
                       (wrap-node-fns log)
                       (add-shutdown-hooks-atom)
                       (add-shutdown-service-graph))]
    mapped-graph))
