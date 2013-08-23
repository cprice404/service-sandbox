(ns com.puppetlabs.service-sandbox.services.plugin.service
  (:require [me.raynes.fs :as fs]
            [cemerick.pomegranate :as pom]
            [clojure.string :as s])
  (:use [clojure.java.io :only [reader]]
        [com.puppetlabs.utils :only [pprint-to-string]]))

;; TODO: this belongs in some utility library
(defn- parse-manifest-line
  [m line]
  (if (empty? line)
    m
    (let [[key val] (s/split line #":" 2)]
      (assoc m key (s/trim val)))))

(defn parse-manifest
  [manifest-file]
  (with-open [r (reader manifest-file)]
    (reduce parse-manifest-line {} (line-seq r))))

;; TODO: this belongs in some utility library
(defn subdirs
  [plugins-dir]
  (filter fs/directory?
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
  [plugin-dir manifest]
  (let [services-str  (manifest "Clojure-Services")]
    (when services-str
      (let [service-fns (parse-manifest-services-str services-str)
            plugin      (fs/base-name plugin-dir)]
        (log :info "Found services for plugin" plugin)
        (pom/add-classpath plugin-dir)
        (map (fn [service-fn]
               (log :info "Loading service graph" service-fn "for plugin" plugin)
               (call-fn-by-name service-fn))
          service-fns)))))

(defn- add-service-graphs-for-plugin
  [coll plugin-dir]
  (let [manifest-file (fs/file plugin-dir "META-INF" "MANIFEST.MF")]
    (if (fs/file? manifest-file)
      (let [manifest (parse-manifest manifest-file)]
        (concat coll (get-service-graphs-for-manifest plugin-dir manifest)))
      coll)))

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
                          (subdirs plugins-dir))]
      (apply merge app-graph
        plugin-graphs))))