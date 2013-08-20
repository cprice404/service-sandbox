(ns com.puppetlabs.service-sandbox.services.config.core
  (:require [overtone.at-at :as at-at]))

;; as an example, we could use an atom to store the config so that
;; it could be updated periodically.  Starting  with some hard-coded
;; values that we'd more likely read from a file, etc.
(def config-map (atom {:shutdown
                          {:shutdown-delay 10}
                       :http
                          {:port          9000
;                           :ssl-port      9001
;                           :keystore      "/home/cprice/work/puppet/puppetdb/conf/ssl.old2/keystore.jks"
;                           :key-password  "ZZ7JKjtljuy3iRZBrtXuJo83x"
                           }}))

(defn initialize
  [log]
  (log :info "Initializing config service")
  ;; config service could poll filesystem for updates, etc., and update
  ;; the atom.
  (at-at/every 5000 #(log :info "Config service checking for updates.") (at-at/mk-pool)))

(defn config
  [k]
  (get @config-map k))
