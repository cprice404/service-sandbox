(ns com.puppetlabs.service-sandbox.services
  (:require [com.puppetlabs.service-sandbox.services.config.service :as config]
            [com.puppetlabs.service-sandbox.services.logging.service :as logging]
            [com.puppetlabs.service-sandbox.services.shutdown.service :as shutdown]
            [com.puppetlabs.service-sandbox.services.plugin.service :as plugin]
            [com.puppetlabs.map :as map]
            [plumbing.graph :as graph]
            [plumbing.fnk.pfnk :as pfnk])
  (:use [com.puppetlabs.utils :refer [pprint-to-string]]))

(defn register-plugins
  [graph app log config]
  (if (:plugins app)
    (plugin/register-plugins graph log config)
    graph))

(defn validate-output-schema!
  [path node-fn]
  (when-not (map? (pfnk/output-schema node-fn))
    (throw (IllegalStateException.
             (format
               (str "Definition for service '%s' is invalid; "
                  "unable to determine output schema.  Service "
                  "node fnk must either return a top-level map "
                  "literal, or must be explicitly annotated with "
                  "output schema metadata.  For more info, see "
                  "the docs for the `fnk` macro.")
               path))))
  node-fn)

(defn validate-output-schemas!
  [graph]
  (map/walk-leaves-and-path
    validate-output-schema!
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
        log             (:log logger)
        config-svc      (get app :config-service
                          (config/bootstrap logger args))
        config          (:config config-svc)
        _               ((:initialize logger) config)
        app-graph       (merge
                          (:service-graph app)
                          (:service-graph logger)
                          (:service-graph config-svc))
        ;; TODO: would like to make this graph manipulation
        ;; more general and dynamic, so that app-provided services
        ;; would have some ability to hook into a lifecycle
        ;; and manipulate the graph according to their own
        ;; needs.
        wrapped-graph   (-> app-graph
                          (register-plugins app log config)
                          (register-shutdown-hooks app log config)
                          (validate-output-schemas!))
        compiled-graph
                        ;((graph/lazy-compile wrapped-graph) {})
                        ((graph/eager-compile wrapped-graph) {})
                        ;((graph/par-compile wrapped-graph) {})

        shutdown-service  (compiled-graph   :shutdown-service)
        wait-for-shutdown (shutdown-service :wait-for-shutdown)]
      (wait-for-shutdown)))