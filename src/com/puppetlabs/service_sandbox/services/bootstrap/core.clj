(ns com.puppetlabs.service-sandbox.services.bootstrap.core)

(def initialized  (atom false))
(def options  (atom {}))
(def graph    (atom nil))


(defn initialize
  [app-options app-graph]
  (println "Initializing bootstrap service.")
  (reset! options app-graph)
  (reset! graph app-graph)
  (reset! initialized true))

(defn verify-initialized
  []
  (when-not @initialized
    (throw (IllegalStateException.
             (str "Bootstrap service must be initialized (via `initialize` fn) "
                "before service graph can be compiled.")))))

(defn app-options
  []
  @options)

(defn app-graph
  []
  @graph)