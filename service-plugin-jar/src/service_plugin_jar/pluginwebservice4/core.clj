(ns service-plugin-jar.pluginwebservice4.core
  (:use [compojure.core :only [defroutes GET]]
        [com.puppetlabs.service-sandbox.ring.middleware :only [wrap-with-request-logger]])
  (:require [compojure.route :as route]))

(defroutes bam-routes
  (GET "/hello" [] "hello bam")
  (route/not-found "not found"))

(defn bam-app
  [log routes]
  (-> routes
    (wrap-with-request-logger log)))
