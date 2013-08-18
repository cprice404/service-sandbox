(ns com.puppetlabs.service-sandbox.services.logging.service
  (:require [com.puppetlabs.service-sandbox.services.logging.core :as core])
  (:use [plumbing.core :only [fnk]]))

(def service-graph
  {:logging-service (fnk []
                      {:log core/log})})
