(ns pluginwebservice3.core
  (:use [compojure.core :only [defroutes GET]]
        [com.puppetlabs.service-sandbox.ring.middleware :only [wrap-with-request-logger]])
  (:require [compojure.route :as route]))


(defroutes baz-routes
  (GET "/hello" [] "hello baz")
  (route/not-found "not found"))

(defn baz-app
  [log routes]
  (-> routes
    (wrap-with-request-logger log)))
