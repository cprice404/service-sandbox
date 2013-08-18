(ns com.puppetlabs.service-sandbox.test-ring-app
  (:use compojure.core)
  (:require [compojure.route :as route]
            [com.puppetlabs.service-sandbox.jetty :as jetty]))

(defroutes foo-routes
  (GET "/foo" [] "hello world")
  (route/not-found "not found"))

(def app foo-routes)

(defn run-foo
  []
  (jetty/run-jetty app
    {:port          9000
     :ssl-port      9001
     :keystore      "/home/cprice/work/puppet/puppetdb/conf/ssl.old2/keystore.jks"
     :key-password  "ZZ7JKjtljuy3iRZBrtXuJo83x"}))
