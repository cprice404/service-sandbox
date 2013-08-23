(ns com.puppetlabs.service-sandbox.fake-db-utils)

(def ^{:private true :dynamic true} *db* nil)

(defn with-db-conn*
  [db f]
  (binding[*db* db]
    (f)))

(defmacro with-db-conn
  [db & body]
  `(with-db-conn* ~db (fn [] ~@body)))

(defn query->vec
  [sql]
  (:fake-value *db*))