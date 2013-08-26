(ns com.puppetlabs.service-sandbox.services.shutdown.service
  (:require [com.puppetlabs.service-sandbox.services.shutdown.core :as core]
            [plumbing.fnk.pfnk :as pfnk]
            [com.puppetlabs.map :as map]
            [com.puppetlabs.service-sandbox.services.utils :as svc-utils]
            [clojure.string :as s])
  (:use [plumbing.core :only [fnk]]
        [com.puppetlabs.utils :only [pprint-to-string]]))

(defn- add-shutdown-hooks-atom
  [graph]
  (assoc graph :shutdown-hooks (fnk ^{:output-schema {}} [] (atom nil))))

(defn- maybe-register-shutdown-hook
  [log path input-map output-map]
  (when-let [shutdown (:shutdown output-map)]
    (log :info "Registering shutdown hook for service:" path)
    (swap! (:shutdown-hooks input-map) conj shutdown))
  output-map)

(defn- maybe-add-shutdown-hook-input
  "If the original node provides :shutdown, then it has
  a dependency on the :shutdown-hooks atom that we've
  added to the graph, so we modify its input schema
  accordingly."
  [input-schema output-schema]
  [(if (and
        (map? output-schema)
        (contains? output-schema :shutdown))
      (assoc input-schema :shutdown-hooks true)
      input-schema)
    output-schema])

(defn- wrap-node-fns
  [graph log]
  (svc-utils/wrap-node-fns graph
    (partial maybe-register-shutdown-hook log)
    maybe-add-shutdown-hook-input))

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
  (map first
    (svc-utils/filter-by-schema
      (fn [path input-schema output-schema]
        (contains? input-schema :shutdown-hooks))
      g)))

(defn add-shutdown-service-graph
  [g log]
  (let [shutdown-services (services-with-hooks g)]
    (log :info "Services with shutdown hooks:" (s/join ", " shutdown-services))
    (merge g
      (svc-utils/add-dependencies
        (service-graph) [:shutdown-service] shutdown-services))))

(defn register-hooks
  [app-graph log]
  (let [mapped-graph (-> app-graph
                       (wrap-node-fns log)
                       (add-shutdown-hooks-atom)
                       (add-shutdown-service-graph log))]
    mapped-graph))
