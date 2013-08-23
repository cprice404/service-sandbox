(ns com.puppetlabs.service-sandbox.services.webservice1.core
  (:use [compojure.core :only [defroutes GET]]
        [com.puppetlabs.service-sandbox.ring.middleware :only [wrap-with-request-logger]])
  (:require [compojure.route :as route]))


(defroutes foo-routes
  (GET "/hello" [] "hello foo")
  (route/not-found "not found"))

(defn foo-app
  [log routes]
  (-> routes
    (wrap-with-request-logger log)))

(defn initialize
  [log http path options]
  (let [add-handler (http :add-handler)]
    (log :info "initializing webservice1")
    (add-handler (partial foo-app log) foo-routes path)))

