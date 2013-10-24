(defproject service-sandbox "0.1.0"
  :description "service sandbox"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.eclipse.jetty/jetty-server "9.0.5.v20130815"]
                 [ring/ring-servlet "1.2.0" :exclusions [javax.servlet/servlet-api]]
                 [compojure "1.1.5"]
                 [overtone/at-at "1.2.0"]
                 [clj-time "0.5.1"]
                 [prismatic/plumbing "0.1.0"]
                 [me.raynes/fs "1.4.4"]
                 [com.cemerick/pomegranate "0.2.0"]]

  :profiles {:dev {:resource-paths ["test-resources"],
                   :dependencies [[ring-mock "0.1.5"]]}}

  :jar-exclusions [#"leiningen/"]

  ;:aot [com.puppetlabs.puppetdb.core]
  :main com.puppetlabs.service-sandbox.apps.app2
)
