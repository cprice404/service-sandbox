;; TODO: currently, this isn't really a service,
;; it's just some code that manipulates the graph
;; prior to compilation.  Probably should rename
;; it accordingly.

(ns com.puppetlabs.service-sandbox.services.plugin.service
  (:require [me.raynes.fs :as fs]
            [cemerick.pomegranate :as pom]
            [clojure.string :as s]
            [com.puppetlabs.jar :as jar])
  (:use [clojure.java.io :only [reader]]
        [com.puppetlabs.utils :only [pprint-to-string]]
        [plumbing.core :only [fnk]]))

(defn plugin-candidates
  [plugins-dir]
  (filter
    #(or (fs/directory? %)
         (= ".jar" (fs/extension %)))
    (map #(fs/file plugins-dir %)
      (fs/list-dir plugins-dir))))

;; TODO: this is a stupid, temporary hack to alleviate the need
;; to pass the `log` fn from the logging service around everywhere.
;; Need to revisit the entire logging setup; not sure if the
;; headache of having a logging service is worthwhile.
(def log-fn (atom nil))
(defn log
  [& args]
  (apply @log-fn args))

(defn- call-fn-by-name
  [service-fn-name]
  (let [[fn-ns fn-name] (s/split service-fn-name #"/" 2)]
    (require (symbol fn-ns))
    ((ns-resolve (symbol fn-ns) (symbol fn-name)))))

(defn parse-manifest-services-str
  [services-str]
  ;; TODO: support multiple service separated by some kind of delimiter
  [services-str])

(defn- get-service-graphs-for-manifest
  [plugin-path manifest]
  (let [services-str  (manifest "Clojure-Services")]
    (when services-str
      (let [service-fns (parse-manifest-services-str services-str)
            plugin      (fs/base-name plugin-path)]
        (log :info "Found services for plugin" plugin)
        (pom/add-classpath plugin-path)
        (map (fn [service-fn]
               (log :info "Loading service graph" service-fn "for plugin" plugin)
               (call-fn-by-name service-fn))
          service-fns)))))

(defn- add-service-graphs-for-manifest
  [coll plugin-path manifest]
  (concat coll
    (get-service-graphs-for-manifest plugin-path manifest)))

(defn- add-service-graphs-for-src-plugin
  [coll plugin-dir]
  (log :info "Looking for manifest in src plugin:" plugin-dir)
  (let [manifest-file (fs/file plugin-dir "META-INF" "MANIFEST.MF")]
    (if (fs/file? manifest-file)
      (add-service-graphs-for-manifest coll plugin-dir
        (jar/parse-manifest-file manifest-file))
      coll)))

(defn- add-service-graphs-for-jar-plugin
  [coll jar-plugin]
  (log :info "Looking for manifest in jar plugin:" jar-plugin)
  (let [manifest (jar/get-jar-manifest jar-plugin)]
    (if manifest
      (add-service-graphs-for-manifest coll jar-plugin manifest)
      coll)))

(defn- add-service-graphs-for-plugin
  [coll plugin-path]
  (cond
    (fs/directory? plugin-path)
    (add-service-graphs-for-src-plugin coll plugin-path)

    (= ".jar" (fs/extension plugin-path))
    (add-service-graphs-for-jar-plugin coll plugin-path)

    :else coll))

(defn register-plugins
  [app-graph log-service-log-fn config]
  ; TODO: stupid hack, need to revisit logging.
  (reset! log-fn log-service-log-fn)
  (log :info "Registering plugins...")
  (let [plugins-dir (config :plugins-dir)]
    (when-not plugins-dir
      (throw (IllegalArgumentException. "Missing configuration setting :plugins-dir")))
    (when-not (fs/directory? plugins-dir)
      (throw (IllegalArgumentException.
               (format ":plugins-dir setting must be a directory (%s)" plugins-dir))))
    (let [plugin-graphs (reduce add-service-graphs-for-plugin []
                          (plugin-candidates plugins-dir))]
      (assoc app-graph :plugins (apply merge plugin-graphs)))))