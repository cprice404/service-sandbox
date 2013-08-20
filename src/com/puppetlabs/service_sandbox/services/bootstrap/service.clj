(ns com.puppetlabs.service-sandbox.services.bootstrap.service
  (:require [com.puppetlabs.service-sandbox.services.bootstrap.core :as core])
  (:use [plumbing.core :only [fnk]]))

(defn initialize
  [app-options app-graph]
  (core/initialize app-options app-graph))

(defn service-graph
  []
  {:bootstrap-service
   ;; This service would be responsible for things like dealing with basic command line
   ;; arguments, initializing logging configuration, initializing whatever configuration
   ;; will be needed by the configuration service, deciding what to do with failures that
   ;; occur before logging can be initialized.  For that reason, it can't have any
   ;; dependencies on any other services.
   (fnk []
     (core/verify-initialized)
     {:app-graph    core/app-graph
      :app-options  core/app-options})})
