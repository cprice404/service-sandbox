(ns com.puppetlabs.service-sandbox.services
  (:require [com.puppetlabs.service-sandbox.services.config.service :as config]
            [com.puppetlabs.service-sandbox.services.logging.service :as logging]
            [com.puppetlabs.service-sandbox.services.shutdown.service :as shutdown]
            [com.puppetlabs.service-sandbox.services.plugin.service :as plugin]
            [plumbing.graph :as graph])
  (:use [com.puppetlabs.utils :refer [pprint-to-string]]))

(defn register-plugins
  [graph app log config]
  (if (:plugins app)
    (plugin/register-plugins graph log config)
    graph))

(defn register-shutdown-hooks
  [graph app log config]
  (shutdown/register-hooks graph log))

(defn run-app
  ;; Default implementation provides a logging service,
  ;; config service, shutdown service, and plugin registration.
  ;; The logging and config implementations can be overridden by
  ;; passing in values for :logger or :config
  ;; in the app map.
  [app args]
  (let [logger          (get app :logging-service
                          (logging/bootstrap))
        config          (get app :config-service
                          (config/bootstrap logger args))
        _               ((:initialize logger) config)
        log             (:log logger)
        app-graph       (merge
                          (:service-graph app)
                          (:service-graph logger)
                          (:service-graph config))
        wrapped-graph   (-> app-graph
                          (register-plugins app log config)
                          (register-shutdown-hooks app log config))
        compiled-graph
                        ;((graph/lazy-compile wrapped-graph) {})
                        ((graph/eager-compile wrapped-graph) {})
                        ;((graph/par-compile wrapped-graph) {})

        shutdown-service  (compiled-graph   :shutdown-service)
        wait-for-shutdown (shutdown-service :wait-for-shutdown)]
      (wait-for-shutdown)))