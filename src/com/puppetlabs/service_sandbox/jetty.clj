(ns com.puppetlabs.service-sandbox.jetty
  "Adapter for the Jetty webserver."
  (:import (org.eclipse.jetty.server Handler Server Request HttpConnectionFactory HttpConfiguration)
           (org.eclipse.jetty.server.handler AbstractHandler ContextHandler
              HandlerCollection ContextHandlerCollection)
           (org.eclipse.jetty.server ServerConnector ConnectionFactory)
           (org.eclipse.jetty.util.thread QueuedThreadPool)
           (org.eclipse.jetty.util.ssl SslContextFactory)
           (javax.servlet.http HttpServletRequest HttpServletResponse)
           [java.util.concurrent Executors])
  (:require [ring.util.servlet :as servlet]
            [overtone.at-at :as at])
  (:use     [clojure.string :only (split trim)]
            [com.puppetlabs.utils :only (compare-jvm-versions acceptable-ciphers)]))

(defn- proxy-handler
  "Returns an Jetty Handler implementation for the given Ring handler."
  [handler]
  (proxy [AbstractHandler] []
    (handle [_ ^Request base-request request response]
      (let [request-map  (servlet/build-request-map request)
            response-map (handler request-map)]
        (when response-map
          (servlet/update-servlet-response response response-map)
          (.setHandled base-request true))))))

(defn- ssl-context-factory
  "Creates a new SslContextFactory instance from a map of options."
  [options]
  (let [context (SslContextFactory.)]
    (if (string? (options :keystore))
      (.setKeyStorePath context (options :keystore))
      (.setKeyStore context (options :keystore)))
    (.setKeyStorePassword context (options :key-password))
    (when (options :truststore)
      (.setTrustStore context (options :truststore)))
    (when (options :trust-password)
      (.setTrustPassword context (options :trust-password)))
    (case (options :client-auth)
      :need (.setNeedClientAuth context true)
      :want (.setWantClientAuth context true)
      nil)
    context))

(defn- connection-factory
  []
  (let [http-config (doto (HttpConfiguration.)
                      (.setSendDateHeader true))]
    (into-array ConnectionFactory
      [(HttpConnectionFactory. http-config)])))

(defn- ssl-connector
  "Creates a SslSelectChannelConnector instance."
  [server ssl-ctxt-factory options]
  (doto (ServerConnector. server ssl-ctxt-factory (connection-factory))
    (.setPort (options :ssl-port 443))
    (.setHost (options :host))))

(defn- plaintext-connector
  [server options]
  (doto (ServerConnector. server (connection-factory))
    (.setPort (options :port 80))
    (.setHost (options :host "localhost"))))

(defn- create-server
  "Construct a Jetty Server instance."
  [options]
  (let [server (Server. (QueuedThreadPool. (options :max-threads 50)))]
    (when (options :port)
      (let [connector (plaintext-connector server options)]
        (.addConnector server connector)))
    (when (or (options :ssl?) (options :ssl-port))
      (let [ssl-host          (options :ssl-host (options :host "localhost"))
            options           (assoc options :host ssl-host)
            ssl-ctxt-factory  (ssl-context-factory options)
            connector         (ssl-connector server ssl-ctxt-factory options)
            ciphers           (if-let [txt (options :cipher-suites)]
                                (map trim (split txt #","))
                                (acceptable-ciphers))
            protocols         (if-let [txt (options :ssl-protocols)]
                                (map trim (split txt #",")))]
        (when ciphers
          (.setIncludeCipherSuites ssl-ctxt-factory (into-array ciphers))
          (when protocols
            (.setIncludeProtocols ssl-ctxt-factory (into-array protocols))))
        (.addConnector server connector)))
    server))

(defn ^Server run-jetty
  "Start a Jetty webserver to serve the given handler according to the
  supplied options:

  :configurator - a function called with the Jetty Server instance
  :port         - the port to listen on (defaults to 80)
  :host         - the hostname to listen on
  :join?        - blocks the thread until server ends (defaults to true)
  :ssl?         - allow connections over HTTPS
  :ssl-port     - the SSL port to listen on (defaults to 443, implies :ssl?)
  :keystore     - the keystore to use for SSL connections
  :key-password - the password to the keystore
  :truststore   - a truststore to use for SSL connections
  :trust-password - the password to the truststore
  :max-threads  - the maximum number of threads to use (default 50)
  :client-auth  - SSL client certificate authenticate, may be set to :need,
                  :want or :none (defaults to :none)"
  [handler options]
  (let [^Server s (create-server (dissoc options :configurator))]
    (doto s
      (.setHandler (proxy-handler handler)))
    (when-let [configurator (:configurator options)]
      (configurator s))
    (.start s)
    (when (:join? options true)
      (.join s))
    s))

(defn run-dynamic-jetty
  [options]
  (let [^Server s                     (create-server (dissoc options :configurator))
        ^ContextHandlerCollection chc (ContextHandlerCollection.)
        ^HandlerCollection hc         (HandlerCollection.)]
    (.setHandlers hc (into-array Handler [chc]))
    (.setHandler s hc)
    (when-let [configurator (:configurator options)]
      (configurator s))
    (.start s)
    (when (:join? options true)
      (.join s))
    {:server   s
     :handlers chc}))

(defn add-handler
  [dynamic-jetty handler path]
  (let [handler-coll (:handlers dynamic-jetty)
        ctxt-handler (doto (ContextHandler. path)
                        (.setHandler (proxy-handler handler)))]
    (.addHandler handler-coll ctxt-handler)
    ;(.start ctxt-handler)
    ctxt-handler))

(defn join
  [dynamic-jetty]
  (.join (:server dynamic-jetty)))

(defn shutdown
  [dynamic-jetty delay-ms]
  ;; this is a hack; we shouldn't be trying to shut down the web server from
  ;; within a web request.
  (at/at (+ delay-ms (at/now)) #(.stop (:server dynamic-jetty)) (at/mk-pool)))
