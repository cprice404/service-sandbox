(ns com.puppetlabs.service-sandbox.services.webservice2.core
  (:use [compojure.core :only [defroutes GET]]
        [com.puppetlabs.service-sandbox.ring.middleware :only [wrap-with-request-logger]]
        [com.puppetlabs.service-sandbox.fake-db-utils :only [with-db-conn query->vec]])
  (:require [compojure.route :as route]))

(defn get-value-from-db
  [db-service]
  (with-db-conn (db-service :db)
    (let [count (-> (query->vec "SELECT COUNT(*) FROM records")
                    first
                    :count)]
      (str "Fake database returned " count " records."))))

(defn wrap-with-db-service
  [handler db-service]
  (fn [req]
    (handler (assoc req :db-service db-service))))

(defroutes bar-routes
  (GET "/hello" [] "hello bar")
  (GET "/db" {db-service :db-service} (get-value-from-db db-service))
  (route/not-found "not found"))

(defn bar-app
  [log db routes]
  (-> routes
    (wrap-with-request-logger log)
    (wrap-with-db-service db)))

(defn initialize
  [log http db-service path options]
  (let [add-handler (http :add-handler)]
    (log :info "initializing webservice2")
    (add-handler (partial bar-app log db-service) bar-routes path)))

