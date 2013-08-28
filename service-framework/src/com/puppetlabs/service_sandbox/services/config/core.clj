(ns com.puppetlabs.service-sandbox.services.config.core
  (:require [overtone.at-at :as at-at]))

(def at-at-pool (atom (at-at/mk-pool)))
(def update-checker-task (atom nil))

;; as an example, we could use an atom to store the config so that
;; it could be updated periodically.  Starting  with some hard-coded
;; values that we'd more likely read from a file, etc.
(def config-map (atom {:shutdown
                          {:shutdown-delay 30}
                       :http
                          {:port          9000
;                           :ssl-port      9001
;                           :keystore      "/home/cprice/work/puppet/puppetdb/conf/ssl.old2/keystore.jks"
;                           :key-password  "ZZ7JKjtljuy3iRZBrtXuJo83x"
                           }
                       :plugins-dir "plugins"
                       }))

(defn bootstrap
  [log args]
  (log :debug "Loading initial configuration."))

(defn initialize
  [log]
  ;; config service could poll filesystem for updates, etc., and update
  ;; the atom.
  (log :info "Initializing config service.")
  (reset! update-checker-task
    (at-at/every 5000
      #(log :info "Config service checking for updates.")
      @at-at-pool)))

(defn config
  [k]
  (get @config-map k))

(defn shutdown
  [log]
  (log :info "SHUTTING DOWN CONFIG SERVICE")
  (at-at/stop @update-checker-task)
  (reset! update-checker-task nil)
  (at-at/stop @at-at-pool)
  (reset! at-at-pool nil)
  (log :info "CONFIG SERVICE SHUTDOWN COMPLETE"))
