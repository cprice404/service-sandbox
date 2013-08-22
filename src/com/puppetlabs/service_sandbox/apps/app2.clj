(ns com.puppetlabs.service-sandbox.apps.app2
  (:require [com.puppetlabs.service-sandbox.services.webservice1.service :as ws1]
            [com.puppetlabs.service-sandbox.services.webservice2.service :as ws2]
            [com.puppetlabs.service-sandbox.services.db.service :as db]
            [com.puppetlabs.service-sandbox.services.http.service :as http]
            [com.puppetlabs.service-sandbox.services :as services]))


(def app
  {:service-graph (merge
                    (ws1/service-graph "/foo")
                    (ws2/service-graph "/bar")
                    (db/service-graph)
                    (http/service-graph))
   :plugins       true})

(defn -main
  [& args]
  (services/run-app app args))