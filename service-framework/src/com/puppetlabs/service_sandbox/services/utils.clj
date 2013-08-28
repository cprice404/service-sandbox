(ns com.puppetlabs.service-sandbox.services.utils
  (:require [plumbing.fnk.pfnk :as pfnk]
            [com.puppetlabs.map :as map]))

(defn wrap-node-fn
  [new-node-fn new-schema-fn path orig-node-fn]
  (pfnk/fn->fnk
    (fn [input-map]
      (new-node-fn path input-map (orig-node-fn input-map)))
    (let [input-schema  (pfnk/input-schema orig-node-fn)
          output-schema (pfnk/output-schema orig-node-fn)]
      (new-schema-fn input-schema output-schema))))

(defn wrap-node-fns
  [g node-fn schema-fn]
  (map/walk-leaves-and-path
    (partial wrap-node-fn node-fn schema-fn)
    g))

(defn service-path->map
  [f path]
  (reduce (fn [x y] {y x}) (reverse (conj path (f path)))))

(defn service-list->input-schema
  [services]
  (apply map/deep-merge
    (map
      (partial service-path->map (fn [path] true))
      services)))

(defn add-dependencies
  [g svc-path dependencies]
  (let [new-node-fn   (fn [path input-map output-map] output-map)
        deps-schema   (service-list->input-schema dependencies)
        new-schema-fn (fn [input-schema output-schema]
                        [(merge input-schema deps-schema)
                         output-schema])]
    (update-in g svc-path
      (partial wrap-node-fn new-node-fn new-schema-fn svc-path))))

(defn filter-by-schema
  [f g]
  (map/filter-leaves-and-path
    (fn [path node-fn]
      (let [input-schema  (pfnk/input-schema  node-fn)
            output-schema (pfnk/output-schema node-fn)]
          (f path input-schema output-schema)))
    g))
