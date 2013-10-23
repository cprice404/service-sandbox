(ns config-service
  (:use [plumbing.core :only [fnk]]))

(defn config-service-graph
  []
  {:config-service
   (fnk []
     (println "IN CONFIG SERVICE FNK")
     {:config (fn [k]
                (println "IN CONFIG SERVICE CONFIG FN!")
                ({:foo "foo" :bar "bar"} k))})})
