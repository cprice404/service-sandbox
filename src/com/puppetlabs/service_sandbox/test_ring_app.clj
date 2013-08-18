(ns com.puppetlabs.service-sandbox.test-ring-app
  (:use compojure.core
        [clojure.pprint :only (pprint)])
  (:require [compojure.route :as route]
            [com.puppetlabs.service-sandbox.jetty :as jetty]))



(defn wrap-log-request
  [handler]
  (fn [req]
    (println "Got a request:")
    (pprint req)
    (println "---------------")
    (handler req)))

(defn wrap-with-server
  [handler server]
  (fn [req]
    (handler (assoc req :server server))))

(defn app
  [routes server]
  (-> routes
    wrap-log-request
    (wrap-with-server server)))


(defn launch-jetty
  []
  (jetty/run-dynamic-jetty
    {:port          9000
     :ssl-port      9001
     :keystore      "/home/cprice/work/puppet/puppetdb/conf/ssl.old2/keystore.jks"
     :key-password  "ZZ7JKjtljuy3iRZBrtXuJo83x"
     :join?         false}))

(defn add-handler
  [server routes path]
  (let [context-routes  (context path [] routes)
        context-app     (app context-routes server)]
    (jetty/add-handler server context-app path)))



(defroutes foo
  (GET "/hello" [] "hello foo")
  (route/not-found "not found"))

(defroutes bar
  (GET "/" [] "hello bar")
  (route/not-found "not found"))

(defroutes shutdown
  (GET "/" {server :server}
    (do
      (jetty/shutdown server 5000)
      "Shutting down server in 5 seconds.")))

(defn add-shutdown-handler
  [server]
  (println "Hi, I'm going to add the shutdown handler to:" server)
  (add-handler server shutdown "/shutdown")
  "DYNAMO! You can now visit /shutdown to stop the server.")

(defroutes dynamo
  (GET "/" {server :server} (add-shutdown-handler server)))


(defn run-foo
  []
  (let [server (launch-jetty)]
    (add-handler server foo "/foo")
    (add-handler server bar "/bar")
    (add-handler server dynamo "/dynamo")
    (jetty/join server)))

