;(ns com.puppetlabs.service-sandbox.apps.app1
;  (:require [com.puppetlabs.service-sandbox.services.webservice1.service :as ws1]
;            [com.puppetlabs.service-sandbox.services.webservice2.service :as ws2]
;            [com.puppetlabs.service-sandbox.services.db.service :as db]
;            [com.puppetlabs.service-sandbox.services.config.service :as config]
;            [com.puppetlabs.service-sandbox.services.logging.service :as logging]
;            [com.puppetlabs.service-sandbox.services.http.service :as http]
;            [com.puppetlabs.service-sandbox.services.shutdown.service :as shutdown]
;            [com.puppetlabs.service-sandbox.services.plugin.service :as plugin]
;            [plumbing.graph :as graph]))
;
;(def app-graph
;  (merge
;    (ws1/service-graph "/foo")
;    (ws2/service-graph "/bar")
;    (logging/service-graph)
;    (db/service-graph)
;    (http/service-graph)
;    (config/service-graph)))
;
;(defn -main
;  [& args]
;  ;; TODO: move all of this compilation stuff into a services library
;  ;; so that the app definition doesn't have to handle it.
;  (let [wrapped-graph   (-> app-graph
;                          (plugin/register-plugins)
;                          (shutdown/register-hooks))
;        compiled-graph
;              ;          ((graph/lazy-compile wrapped-graph) {})
;                        ((graph/eager-compile wrapped-graph) {})
;              ;          ((graph/par-compile wrapped-graph) {})
;
;        shutdown-service  (compiled-graph   :shutdown-service)
;        wait-for-shutdown (shutdown-service :wait-for-shutdown)]
;    (wait-for-shutdown)))
;
