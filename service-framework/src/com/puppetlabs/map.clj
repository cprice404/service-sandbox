(ns com.puppetlabs.map
  (:require [plumbing.map :as map]))

(defn walk-leaves
  [f m]
  (map/map-leaves f m))

(defn walk-leaves-and-path
  [f m]
  (map/map-leaves-and-path f m))

(defn map-leaves-and-path
  ;; TODO docs
  ([f m] (when m (map-leaves-and-path f [] m)))
  ([f ks m]
    (if-not (map? m)
      (list (f ks m))
      (apply concat
        (for [[k v] m]
          (map-leaves-and-path f (conj ks k) v))))))

(defn deep-merge
  "Recursively merges maps. If vals are not maps, the last value wins."
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))
