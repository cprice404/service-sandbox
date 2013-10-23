(ns consumer-service
  (:use [plumbing.core :only [fnk]]))

(defn get-config
  [& args]
  (throw (IllegalArgumentException. "get-config function should be bound during service initialization!")))

(defn hello
  []
  (println "in hello")
  (let [foo (get-config :foo)]
    (println "consumer service hello; foo config value is:" foo)))

(defn consumer-service-graph
  []
  {:consumer-service
   (fnk [[:config-service config]]
;     (with-redefs [get-config config]
       (println "IN CONSUMER SERVICE FNK; calling config")
       (config :foo)
       {:hello (fn []
                 (with-redefs [get-config config]
                   (println "calling hello")
                   (hello)))})
;     )
   })
