(ns com.puppetlabs.service-sandbox.apps.app1
  (:require [com.puppetlabs.service-sandbox.services.webservice1.service :as ws1]
            [com.puppetlabs.service-sandbox.services.webservice2.service :as ws2]
            [com.puppetlabs.service-sandbox.services.db.service :as db]
            [com.puppetlabs.service-sandbox.services.config.service :as config]
            [com.puppetlabs.service-sandbox.services.logging.service :as logging]
            [com.puppetlabs.service-sandbox.services.http.service :as http]
            [com.puppetlabs.service-sandbox.services.shutdown.service :as shutdown]
            [plumbing.graph :as graph]))

(def app-graph
  (merge
    (ws1/service-graph "/foo")
    (ws2/service-graph "/bar")
    (logging/service-graph)
    (db/service-graph)
    (http/service-graph)
    (shutdown/service-graph)
    (config/service-graph)))

(defn -main
  [& args]
  (let [wrapped-graph   (-> app-graph
                          ;; TODO should be able to generalize this
                          ;; into a generic "pre-compile" phase that
                          ;; any service can hook into by simply
                          ;; providing a ":precompile" fnk on the
                          ;; service itself.
                          (shutdown/register-hooks))
        compiled-graph
              ;          ((graph/lazy-compile wrapped-graph) {})
                        ((graph/eager-compile wrapped-graph) {})
              ;          ((graph/par-compile wrapped-graph) {})

        shutdown-service  (compiled-graph   :shutdown-service)
        wait-for-shutdown (shutdown-service :wait-for-shutdown)]
    (wait-for-shutdown)))

