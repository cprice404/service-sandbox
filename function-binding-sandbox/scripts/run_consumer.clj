(ns foo
  (:use [consumer-service :only [consumer-service-graph]]
        [config-service :only [config-service-graph]]
        [plumbing.graph :as graph]))

(def services-graph
  (merge
    (consumer-service-graph)
    (config-service-graph)))

(println "IN RUNNER SCRIPT; ABOUT TO COMPILE GRAPH.")
(let [compiled-graph ((graph/eager-compile services-graph) {})
      hello (get-in compiled-graph [:consumer-service :hello])]
  (println "GRAPH COMPILED; ABOUT TO CALL HELLO FROM CONSUMER SERVICE")
  (hello))
