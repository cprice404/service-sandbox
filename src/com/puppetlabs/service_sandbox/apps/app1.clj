(ns com.puppetlabs.service-sandbox.apps.app1
  (:require [com.puppetlabs.service-sandbox.services.bootstrap.service :as bootstrap]
            [com.puppetlabs.service-sandbox.services.webservice1.service :as ws1]
            [com.puppetlabs.service-sandbox.services.webservice2.service :as ws2]
            [com.puppetlabs.service-sandbox.services.plugin.service :as plugin]
            [com.puppetlabs.service-sandbox.services.db.service :as db]
            [com.puppetlabs.service-sandbox.services.config.service :as config]
            [com.puppetlabs.service-sandbox.services.logging.service :as logging]
            [com.puppetlabs.service-sandbox.services.http.service :as http]
            [com.puppetlabs.service-sandbox.services.shutdown.service :as shutdown]
            [plumbing.graph :as graph]))

(def app-graph
  (merge
    (bootstrap/service-graph)
    (ws1/service-graph "/foo")
    (ws2/service-graph "/bar")
    (plugin/service-graph)
    (logging/service-graph)
    (db/service-graph)
    (http/service-graph)
    (shutdown/service-graph)
    (config/service-graph)))

(println "app graph:" app-graph)
(println "type: " (type app-graph))

(bootstrap/initialize {} app-graph)

(defn -main
  [& args]
  (let [g
;          ((graph/lazy-compile app-graph) {})
          ((graph/eager-compile app-graph) {})
;          ((graph/par-compile app-graph) {})

        shutdown-service  (g :shutdown-service)
        wait-for-shutdown (shutdown-service :wait-for-shutdown)]
    (wait-for-shutdown)))

